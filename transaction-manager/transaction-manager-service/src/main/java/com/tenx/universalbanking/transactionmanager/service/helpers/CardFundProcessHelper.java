package com.tenx.universalbanking.transactionmanager.service.helpers;

import static com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes.WORLDPAY_TRANSACTION_FAILURE_KEY_CODE;
import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.SUCCESS;
import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum.AUTHORISATION_RESULT_RESPONSE;
import static com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum.LAST_PAYMENT_STATUS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAAA002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_MERCHANT_CARD_TRANSACTIONS_OTHER_ACCOUNT_FUNDING;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.api.WorldPayAdapterControllerApi;
import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.AdapterResponse;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.mapper.WorldpayTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.service.messagebuilder.CAAA002MessageBuilder;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "cardFundProcessHelper")
public class CardFundProcessHelper extends MessageServiceProcessorHelper {

  private final Logger logger = getLogger(CardFundProcessHelper.class);

  @Autowired
  private CAAA002MessageBuilder caa002MessageBuilder;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private WorldPayAdapterControllerApi worldPayAdapterControllerApi;

  @Autowired
  private WorldpayTransactionMessageMapper worldpayMapper;

  @Autowired
  private LedgerManagerClient lmClient;

  private static final String AUTHORISED = "AUTHORISED";
  private static final String FAILED = "FAILED";

  public Optional<PaymentProcessResponse> handleDecisionProcess(TransactionMessage transactionMessage,
      HttpServletRequest request) {

    PaymentProcessResponse processResponse;

    try {
      logger.debug("Waiting for pdf decision");
      //Call PDF to validate transaction
      PaymentDecisionTransactionResponse decisionResponse =
          callPaymentDecisionService(transactionMessage);

      if (!isPaymentDecisionSuccess(decisionResponse)) {

        processResponse = buildFailureResponse(decisionResponse);
        TransactionMessage caa002Msg = buildCaa002(transactionMessage, processResponse, request);
        processResponse = postAndSendToKafka(caa002Msg, transactionMessage, processResponse);
        return Optional.of(processResponse);
      }
    } catch (RuntimeException e) {
      processResponse = new PaymentProcessResponse(PaymentDecisionResponse.FAILED.name(),
          getGenericFailureReason());

      addTransactionStatus(transactionMessage, TransactionStatusValueEnum.FAILED);
      TransactionMessage caa002Msg = buildCaa002(transactionMessage, processResponse, request);
      lmClient.postTransactionToLedger(caa002Msg);
      processResponse = postAndSendToKafka(caa002Msg, transactionMessage, processResponse);
      logger.error("Downstream Service Name :{} , API Name : {}", "Payment Decision Framework", "payment-decision");
      logger.error("Input Transaction Message {} ", appendTransactionMessageRequest(transactionMessage));
      logger.error("Payment decision is failed due to Exception {}", e);
      return Optional.of(processResponse);
    }

    return Optional.empty();
  }

  private PaymentProcessResponse postAndSendToKafka(TransactionMessage caa002Msg, TransactionMessage initialTransactionMessage, PaymentProcessResponse response){
    PaymentProcessResponse processResponse = response;
    LedgerPostingResponse postingResponse = lmClient.postTransactionToLedger(caa002Msg);
    if(postingResponse.isPostingSuccess()){
      sendtoKafka(caa002Msg, CAAA002);
    } else{
      logger.debug("Posting transaction on LM returned failure. Transaction message not posted to Topic.");
      processResponse = buildLMPostingFailureResponse(postingResponse, initialTransactionMessage);
    }
    return processResponse;
  }

  private PaymentProcessResponse buildFailureResponse(
      PaymentDecisionTransactionResponse decisionResponse) {

    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();

    PaymentDecisionReasonDTO decisionReason = decisionResponse.getDecisionReason();
    ReasonDto reason = new ReasonDto(decisionReason.getCode(), decisionReason.getMessage());

    paymentProcessResponse.setReason(reason);
    paymentProcessResponse.setPaymentStatus(decisionResponse.getDecisionResponse());

    return paymentProcessResponse;
  }

  public TransactionMessage buildCaa002(TransactionMessage transactionMessage,
      PaymentProcessResponse paymentProcessResponse, HttpServletRequest request){
    logger.debug("Building CAAA.002 Message");

    TransactionMessage transactionMsg = caa002MessageBuilder
        .caaa002MessageBuilder(transactionMessage, paymentProcessResponse);

    generateTransactionAndCorrelationIds(transactionMsg);

    addTracingHeaders(transactionMsg, request);
    return transactionMsg;
  }

  public TransactionMessage fundAccount(TransactionMessage transactionMessage) {

    AdapterResponse worldpayAdapterResponse = worldPayAdapterControllerApi.worldPayPaymentSubmitOrderUsingPOST(
        worldpayMapper.toWorldPayAdapterClientTransactionMessage(transactionMessage));

    //Submit Transaction to WorldPay adaptor
    return worldpayMapper
        .clientTmToTmUtl(worldpayAdapterResponse.getTransactionMessage());
  }

  public void setCardFundTransactionCode(TransactionMessage transactionMessage) {
    Map<String, Object> caaa002message = transactionMessage.getMessages().get(0).getMessage();

    if (!Objects.isNull(caaa002message.get(AUTHORISATION_RESULT_RESPONSE.name())) && AUTHORISED
        .equalsIgnoreCase(
            caaa002message.get(AUTHORISATION_RESULT_RESPONSE.name()).toString())) {
      transactionMessage.getMessages().forEach(item -> item.getAdditionalInfo().put(
          TRANSACTION_CODE.name(),
          PAYMENTS_MERCHANT_CARD_TRANSACTIONS_OTHER_ACCOUNT_FUNDING.getValue()
      ));

      logger.debug("Setting TranscationCode Value:{}",
          PAYMENTS_MERCHANT_CARD_TRANSACTIONS_OTHER_ACCOUNT_FUNDING.getValue());
    }
  }

  public PaymentProcessResponse buildResponse(TransactionMessage transactionMessage) {

    PaymentProcessResponse processResponse = new PaymentProcessResponse();

    Map<String, Object> caaa002message = transactionMessage.getMessages().get(0).getMessage();

    if (hasAuthorisationField(transactionMessage)) {

      if (isCardFundAuthorised(transactionMessage)) {
        processResponse.setPaymentStatus(SUCCESS.name());
        processResponse.setTransactionMessage(transactionMessage);
      }
    } else {
      processResponse = buildFailureResponse(
          caaa002message.get(LAST_PAYMENT_STATUS.name()).toString());
    }

    return processResponse;
  }

  private Boolean hasAuthorisationField(TransactionMessage transactionMessage) {

    Map<String, Object> caaa002message = transactionMessage.getMessages().get(0).getMessage();

    return !Objects.isNull(caaa002message.get(AUTHORISATION_RESULT_RESPONSE.name()));
  }

  private Boolean isCardFundAuthorised(TransactionMessage transactionMessage) {
    Map<String, Object> caaa002message = transactionMessage.getMessages().get(0).getMessage();

    return AUTHORISED.equalsIgnoreCase(
        caaa002message.get(AUTHORISATION_RESULT_RESPONSE.name()).toString());
  }

  public PaymentProcessResponse handleGenericFailure(TransactionMessage transactionMessage,
      HttpServletRequest request) {
    PaymentProcessResponse processResponse;
    processResponse = new PaymentProcessResponse(FAILED, getGenericFailureReason());
    TransactionMessage caa002Msg = buildCaa002(transactionMessage, processResponse, request);
    LedgerPostingResponse lmPostingResponse = lmClient.postTransactionToLedger(caa002Msg);
    if(lmPostingResponse.isPostingSuccess()){
      sendtoKafka(caa002Msg, CAAA002);
    } else{
      logger.debug("Posting transaction on LM returned failure. Transaction message not posted to Topic.");
      processResponse = buildLMPostingFailureResponse(lmPostingResponse, transactionMessage);
    }
    return processResponse;
  }

  private PaymentProcessResponse buildFailureResponse(String paymentStatus) {
    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();
    ReasonDto reasonDto = new ReasonDto(WORLDPAY_TRANSACTION_FAILURE_KEY_CODE, paymentStatus);
    paymentProcessResponse.setReason(reasonDto);
    paymentProcessResponse.setPaymentStatus(FAILED);
    return paymentProcessResponse;
  }

  public PaymentProcessResponse buildLMPostingFailureResponse(LedgerPostingResponse response, TransactionMessage message) {
    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();
    paymentProcessResponse.setPaymentStatus(FAILED);
    paymentProcessResponse.setReason(response.getReason());
    paymentProcessResponse.setTransactionMessage(message);
    return paymentProcessResponse;
  }

}

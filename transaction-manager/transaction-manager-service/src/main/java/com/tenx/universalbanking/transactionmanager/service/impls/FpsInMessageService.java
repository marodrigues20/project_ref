package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.Pain002Enum.IS_RETURN;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.REVERSE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_CREDIT;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.messagebuilder.PACS002MessageBuilder;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FpsInMessageService implements TransactionMessageService {

  private final Logger logger = getLogger(FpsInMessageService.class);

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private PACS002MessageBuilder pacs002MessageBuilder;

  @Autowired
  private PaymentProcessResponseConverter responseConverter;

  @Override
  public TransactionMessageTypeEnum getType() {
    return TransactionMessageTypeEnum.FPS_IN;
  }

  @Autowired
  private LedgerManagerClient lmClient;

  @Autowired
  private TMExceptionBuilder exceptionBuilder;


  @Autowired
  private ReconciliationHelper reconciliationHelper;

  // this file may get either get PACS008/PACS002  and needs change.
  @Override
  public PaymentProcessResponse process(TransactionMessage transactionMessage,
      HttpServletRequest request) {

    logger.debug("In FPSIN MessageService : process function started");

    PaymentProcessResponse processResponse = new PaymentProcessResponse();

    final boolean isReverse = isReverse(transactionMessage);

    if(!isReverse){
      messageServiceProcessorHelper.generateTransactionAndCorrelationIds(transactionMessage);
    }

    PaymentDecisionTransactionResponse paymentDecisionResponse =
        messageServiceProcessorHelper.callPaymentDecisionService(transactionMessage);

    TransactionMessage pacs002Response = pacs002MessageBuilder
        .buildPacs002Response(transactionMessage, paymentDecisionResponse);
    Boolean isSuccess = messageServiceProcessorHelper
        .isPaymentDecisionSuccess(paymentDecisionResponse);
    setTransactionStatus(transactionMessage, isSuccess);


    if (isSuccess) {
      logger.debug("In FPSIN MessageService : Payment decision {}",
          isReverse ? "Reverse" : "Success");
      transactionMessage.getMessages().forEach(item -> item.getAdditionalInfo().put(
          TRANSACTION_CODE.name(), PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_CREDIT.getValue()
      ));
      logger.debug(
          "Setting TransactionCode Value:{}", PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_CREDIT
              .getValue());
      String subscriptionKey = messageServiceProcessorHelper
          .getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.PACS008);

      messageServiceProcessorHelper.addTracingHeaders(transactionMessage, request);
      processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());

      LedgerPostingResponse lmPostingResponse = lmClient
          .postTransactionToLedger(transactionMessage);
      if (lmPostingResponse.isPostingSuccess()) {
        messageSender.sendPaymentMessage(subscriptionKey, transactionMessage);
        reconciliationHelper.saveReconciliationMessage(
            buildReconciliationMessage(transactionMessage, Event.SUCCESS));
      } else {
        logger.debug(
            "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
        reconciliationHelper.saveReconciliationMessage(
            buildReconciliationMessage(transactionMessage, Event.INT_REJECT));
        return responseConverter.buildPaymentProcessResponse(lmPostingResponse, transactionMessage);
      }
    } else {
      logger.error(
          "Downstream Service Name :Payment Decision Framework , API Name : payment-decision");
      logger.error("Input Transaction Message {} ",
          appendTransactionMessageRequest(transactionMessage));
      if(null!=paymentDecisionResponse.getDecisionReason()){
        logger.error("Payment Decision Response Code {}"
                + " Payment Decision Response Message {} :",
            paymentDecisionResponse.getDecisionReason().getCode(),
            paymentDecisionResponse.getDecisionReason().getMessage());
      }
      logger.debug("In FPSIN MessageService : Payment decision Failed");
      reconciliationHelper.saveReconciliationMessage(
          buildReconciliationMessage(transactionMessage, Event.INT_REJECT));
      throw exceptionBuilder.buildFromPdfResponse(paymentDecisionResponse);
    }

    processResponse.setTransactionMessage(pacs002Response);

    return processResponse;
  }

  private void setTransactionStatus(TransactionMessage transactionMessage, Boolean isSuccess) {
    Map<String, Object> responseAdditionalInfo = transactionMessage.getAdditionalInfo();
    TransactionStatusValueEnum status = isSuccess ? SUCCESS : FAILED;
    status = (isSuccess && isReverse(transactionMessage)) ? REVERSE : status;
    responseAdditionalInfo.put(TRANSACTION_STATUS.name(), status);
    transactionMessage.setAdditionalInfo(responseAdditionalInfo);
  }

  private boolean isReverse(final TransactionMessage transactionMessage) {
    Map<String, Object> pacs0008 = transactionMessage.getMessages().get(0).getMessage();
    return nonNull(pacs0008.get(IS_RETURN.name()))
        && Boolean.valueOf(pacs0008.get(IS_RETURN.name()).toString());
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage,
      Event event) {

    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(event).
        scope(TransactionMessageTypeEnum.FPS_IN).
        serviceName(ServiceNames.TRANSACTION_MANAGER).
        build();
  }

}
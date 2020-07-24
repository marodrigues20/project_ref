package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.reconciliation.logger.model.Event.SUCCESS;
import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.COMPLETE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAAA002;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.AdapterErrorResponse;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.CardFundProcessHelper;
import com.tenx.universalbanking.transactionmanager.service.mapper.WorldpayTransactionMessageMapper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class CardFundMessageService implements TransactionMessageService {

  private final Logger logger = getLogger(CardFundMessageService.class);

  @Autowired
  private CardFundProcessHelper cardFundProcessHelper;

  @Autowired
  private WorldpayTransactionMessageMapper worldpayMapper;

  @Autowired
  private LedgerManagerClient lmClient;

  private static final String FAILED = "FAILED";

  @Override
  public TransactionMessageTypeEnum getType() {
    return TransactionMessageTypeEnum.TOP_UP_BY_CARD;
  }

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Override
  public PaymentProcessResponse process(TransactionMessage transactionMessage, HttpServletRequest request) {
    logger.debug("In Card Fund MessageService : process function started");

    TransactionMessage adapterResponseTransactionMessage = new TransactionMessage();
    PaymentProcessResponse processResponse = new PaymentProcessResponse();

    Optional<PaymentProcessResponse> optionalPaymentProcessResponse =
        cardFundProcessHelper.handleDecisionProcess(transactionMessage, request);

    if (optionalPaymentProcessResponse.isPresent()) {
      return optionalPaymentProcessResponse.get();
    }

    try {
      logger.debug("Sending to worldpay adapter");

      adapterResponseTransactionMessage = cardFundProcessHelper.fundAccount(transactionMessage);

      cardFundProcessHelper.addTransactionStatus(adapterResponseTransactionMessage, COMPLETE);

    } catch (ResourceAccessException accessExp) {
      logger.error("Downstream Service Name :{} , API Name : {}", "Worldpay Adapter",
          "submit-order");
      logger.error("Input Transaction Message : {} ", appendTransactionMessageRequest(transactionMessage));
      //client call failed, worldpay adapter may be down
      logger.error("Worldpay Adapter is failed due to Exception {}", accessExp);

      processResponse = new PaymentProcessResponse(FAILED,
          cardFundProcessHelper.getGenericFailureReason());

      cardFundProcessHelper.addTransactionStatus(transactionMessage, TransactionStatusValueEnum.FAILED);
      TransactionMessage caa002Msg = cardFundProcessHelper.buildCaa002(transactionMessage, processResponse, request);
      LedgerPostingResponse postingResponse = lmClient.postTransactionToLedger(caa002Msg);
      if(postingResponse.isPostingSuccess()){
        cardFundProcessHelper.sendtoKafka(caa002Msg, CAAA002);
      } else{
        logger.debug("Posting transaction on LM returned failure. Transaction message not posted to Topic.");
        processResponse = cardFundProcessHelper.buildLMPostingFailureResponse(postingResponse, transactionMessage);
      }
      return processResponse;

    } catch (HttpClientErrorException restException) {

      if (restException.getStatusCode() == HttpStatus.BAD_REQUEST) {
        restException.getResponseBodyAsString();
        try {
          ObjectMapper objectMapper = new ObjectMapper();
          AdapterErrorResponse worldpayAdapterErrorResponse =
              objectMapper
                  .readValue(restException.getResponseBodyAsString(), AdapterErrorResponse.class);

          adapterResponseTransactionMessage = worldpayMapper.clientTmToTmUtl(
              worldpayAdapterErrorResponse.getTransactionMessage());

          cardFundProcessHelper
              .addTransactionStatus(transactionMessage, TransactionStatusValueEnum.FAILED);

          processResponse = new PaymentProcessResponse(FAILED,
              new ReasonDto(worldpayAdapterErrorResponse.getErrors().get(0).getCode(),
                  worldpayAdapterErrorResponse.getErrors().get(0).getMessage()));

          logger.error("Downstream Service Name :{} , API EndPoint : {}", "Worldpay Adapter",
              "submit-order");
          logger.error("Input Transaction Message : {} ",
              appendTransactionMessageRequest(transactionMessage));
          logger.error("WorldPay Adapter Error Response : {} ",
              worldpayAdapterErrorResponse.getErrors());
          logger.error("Worldpay Adapter is failed with error {}", restException);
        } catch (IOException e) { //some exception happened in objectMapper
          logger.error("Input Transaction Message : {} ",
              appendTransactionMessageRequest(transactionMessage));
          logger.error("Exception in WorldPay Adapter Error Response Mapping {}", e);
          processResponse = cardFundProcessHelper
              .handleGenericFailure(transactionMessage, request);
        }
      }
    }

    cardFundProcessHelper.generateTransactionAndCorrelationIds(adapterResponseTransactionMessage);

    cardFundProcessHelper.setCardFundTransactionCode(adapterResponseTransactionMessage);

    cardFundProcessHelper.addTracingHeaders(adapterResponseTransactionMessage, request);
    LedgerPostingResponse postingResponse = lmClient.postTransactionToLedger(adapterResponseTransactionMessage);
    if(postingResponse.isPostingSuccess()){
      cardFundProcessHelper.sendtoKafka(adapterResponseTransactionMessage, CAAA002);
      reconciliationHelper.saveReconciliationMessage(buildReconciliationMessage(adapterResponseTransactionMessage));
    } else{
      logger.debug("Posting transaction on LM returned failure. Transaction message not posted to Topic.");
      processResponse = cardFundProcessHelper.buildLMPostingFailureResponse(postingResponse, transactionMessage);
    }
    //Check if httpClientErrorException already build the response
    if (!FAILED.equals(processResponse.getPaymentStatus())) {
      processResponse = cardFundProcessHelper.buildResponse(adapterResponseTransactionMessage);
    }

    return processResponse;
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage) {
    String transactionCorrelationId =  transactionMessage.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
            transactionCorrelationId(transactionCorrelationId).
            event(SUCCESS).
            scope(TransactionMessageTypeEnum.TOP_UP_BY_CARD).
            serviceName(ServiceNames.TRANSACTION_MANAGER).
            build();
  }
}

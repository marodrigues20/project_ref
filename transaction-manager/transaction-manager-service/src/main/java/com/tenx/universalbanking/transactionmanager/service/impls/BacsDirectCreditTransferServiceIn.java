package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_FOR_BACS;
import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.BACS_CT_IN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS_CREDIT;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.BacsDCPaymentStatus;
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
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BacsDirectCreditTransferServiceIn implements TransactionMessageService {

  private final Logger logger = getLogger(BacsDirectCreditTransferServiceIn.class);

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private LedgerManagerClient lmClient;

  @Autowired
  private TMExceptionBuilder exceptionBuilder;

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Autowired
  private PaymentProcessResponseConverter responseConverter;

  @Override
  public TransactionMessageTypeEnum getType() {
    return TransactionMessageTypeEnum.BACS_CT_IN;
  }

  @Override
  public PaymentProcessResponse process(TransactionMessage transactionMessage,
      HttpServletRequest request) {

    logger.debug("In BACS_CT_IN MessageService : process function started");

    PaymentProcessResponse processResponse = new PaymentProcessResponse();
    BacsDCPaymentStatus paymentStatus = BacsDCPaymentStatus
        .valueOf(
            transactionMessage.getAdditionalInfo().get(TRANSACTION_STATUS.name()).toString());

    transactionMessage.getMessages().get(0).getAdditionalInfo()
        .put(PAYMENT_METHOD_TYPE.name(), BACS_CT_IN);
    transactionMessage.getMessages().forEach(item -> item.getAdditionalInfo().put(
        TRANSACTION_CODE.name(), PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS_CREDIT.getValue()
    ));

    messageServiceProcessorHelper.generateTransactionAndCorrelationIds(transactionMessage);
    logger.info("Transaction Status Message " + paymentStatus);

    switch (paymentStatus) {
      case SUB_NOT_FOUND_NEW:
        messageSender.sendPaymentMessageBacs(transactionMessage);
        processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());
        reconciliationHelper.saveReconciliationMessage(
                buildReconciliationMessage(transactionMessage, Event.INT_REJECT));
        break;

      case NEW:
        validateAtPDF(transactionMessage, processResponse, request);
        transactionMessage.getAdditionalInfo()
            .put(TRANSACTION_STATUS.name(), TransactionStatusValueEnum.COMPLETE);

        LedgerPostingResponse lmPostingResponse = lmClient
            .postTransactionToLedger(transactionMessage);
        if (lmPostingResponse.isPostingSuccess()) {
          messageServiceProcessorHelper
              .sendtoKafka(transactionMessage, PaymentMessageTypeEnum.PACS008);
          reconciliationHelper.saveReconciliationMessage(
              buildReconciliationMessage(transactionMessage, Event.SUCCESS));
        } else {
          logger.debug(
              "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
            reconciliationHelper.saveReconciliationMessage(
                    buildReconciliationMessage(transactionMessage, Event.INT_REJECT));
          return responseConverter.buildPaymentProcessResponse(lmPostingResponse, transactionMessage);
        }

        break;

      case PROCESSOR_FAILED_NEW:
      case SUB_NOT_ACTIVE_NEW:
      case ACC_CLOSED_NEW:
        if (transactionMessage.getMessages().get(0).getAdditionalInfo()
            .get(SUBSCRIPTION_STATUS.name()) != null) {
          validateAtPDF(transactionMessage, processResponse, request);
          logger.info("In PDF call Success : Payment decision Framework Success");
          messageServiceProcessorHelper
              .sendtoKafka(transactionMessage, PaymentMessageTypeEnum.PACS008);
          if (processResponse.getPaymentStatus().equals(SUCCESS.name())) {
            reconciliationHelper.saveReconciliationMessage(
                buildReconciliationMessage(transactionMessage, Event.SUCCESS));
          } else {
              reconciliationHelper.saveReconciliationMessage(
                   buildReconciliationMessage(transactionMessage, Event.INT_REJECT));
          }
        } else {
          messageSender.sendPaymentMessageBacs(transactionMessage);
          processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());
        }
        break;

      default:
        logger.error("Http Server error exception Transaction Status Not found.");
        throw exceptionBuilder
            .buildBacsTransactionManagerException(INTERNAL_SERVER_ERROR_FOR_BACS.getStatusCode(),
                INTERNAL_SERVER_ERROR_FOR_BACS.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return processResponse;
  }


  private PaymentProcessResponse validateAtPDF(TransactionMessage transactionMessage,
      PaymentProcessResponse processResponse, HttpServletRequest request) {
    PaymentDecisionTransactionResponse paymentDecisionResponse =
        messageServiceProcessorHelper.callPaymentDecisionService(transactionMessage);
    Boolean status = messageServiceProcessorHelper
        .isPaymentDecisionSuccess(paymentDecisionResponse);
    setTransactionStatus(transactionMessage, status);

    if (status) {
      logger.debug("In BACS_CT_IN MessageService : Payment decision Success");
      messageServiceProcessorHelper.addTracingHeaders(transactionMessage, request);
      processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());

    } else {
      logger.error("Downstream Service Name :{} , API Name : {}", "Payment Decision Framework",
          "payment-decision");
      logger.error("Input Transaction Message {} ",
          appendTransactionMessageRequest(transactionMessage));
      logger.error("Payment Decision Reason {}", paymentDecisionResponse.getDecisionReason());
      logger.debug("In BACS_CT_IN MessageService : Payment decision Failed");
      processResponse.setPaymentStatus(PaymentDecisionResponse.FAILED.name());
      processResponse.setReason(messageServiceProcessorHelper.getGenericFailureReason());
    }
    logger.info("Transaction message published to kafka with TRANSACTION_CORRELATION_ID: {} ",
            transactionMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()));
    return processResponse;
  }

  private void setTransactionStatus(TransactionMessage transactionMessage, Boolean status) {
    Map<String, Object> responseAdditionalInfo = transactionMessage.getAdditionalInfo();
    responseAdditionalInfo.put(TRANSACTION_STATUS.name(), status ?
        TransactionStatusValueEnum.SUCCESS : FAILED);
    transactionMessage.setAdditionalInfo(responseAdditionalInfo);
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage,
      Event event) {

    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(event).
        scope(TransactionMessageTypeEnum.BACS_CT_IN).
        serviceName(ServiceNames.TRANSACTION_MANAGER).build();
  }
}

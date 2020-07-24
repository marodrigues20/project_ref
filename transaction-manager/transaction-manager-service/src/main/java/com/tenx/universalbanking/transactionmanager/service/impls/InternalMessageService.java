package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.COMPLETE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.RESERVE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.SUCCESS;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class InternalMessageService implements TransactionMessageService {

  private final Logger internalMessagelogger = getLogger(getClass());

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private PDFTransactionMessageMapper pdfTransactionMessageMapper;

  @Autowired
  private LedgerManagerClient lmClient;

  @Autowired
  private PaymentProcessResponseConverter responseConverter;

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Autowired
  private GeneratorUtil generatorUtil;

  @Override
  public PaymentProcessResponse process(TransactionMessage transactionMessage,
      HttpServletRequest request) {

    internalMessagelogger.debug("In MessageService : process function started");

    PaymentProcessResponse processResponse = new PaymentProcessResponse();

    messageServiceProcessorHelper.generateTransactionAndCorrelationIds(transactionMessage);

    addTransactionStatus(transactionMessage, RESERVE);

    setTransactionCode(transactionMessage);

    PaymentDecisionTransactionResponse paymentDecisionResponse =
        messageServiceProcessorHelper.callPaymentDecisionService(transactionMessage);
    String subscriptionKey = messageServiceProcessorHelper
        .getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.PACS008);

    if (messageServiceProcessorHelper.isPaymentDecisionSuccess(paymentDecisionResponse)) {
      internalMessagelogger.debug("In MessageService : Payment decision Success");
      addTransactionStatus(transactionMessage, COMPLETE);
      processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());
    } else {
      internalMessagelogger
          .error("Downstream Service Name :{} , API EndPoint : {}", "Payment Decision Framework",
              "payment-decision");
      internalMessagelogger.error("Input Transaction Message {} ",
          appendTransactionMessageRequest(transactionMessage));
      if (null != paymentDecisionResponse.getDecisionReason()) {
        internalMessagelogger.error(" Payment Decision Response Code {}"
                + " Payment Decision Response Message {} :",
            paymentDecisionResponse.getDecisionReason().getCode(),
            paymentDecisionResponse.getDecisionReason().getMessage());
      }
      internalMessagelogger.error(
          "In MessageService : Payment decision Failed, Transaction Correlation Id : {}, Reuqest Correlation Id: {}, Tenant Party Key: {}",
          transactionMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID),
          transactionMessage.getAdditionalInfo().get(REQUEST_CORRELATION_ID),
          transactionMessage.getAdditionalInfo().get(TENANT_PARTY_KEY));
      addTransactionStatus(transactionMessage, FAILED);
      processResponse.setPaymentStatus(PaymentDecisionResponse.FAILED.name());
      processResponse.setReason(
          messageServiceProcessorHelper.getFailureReason(paymentDecisionResponse));
      saveReconMessage(transactionMessage, processResponse);
      return processResponse;
    }

    messageServiceProcessorHelper.addTracingHeaders(transactionMessage, request);

    try {
      LedgerPostingResponse lmPostingResponse = lmClient
          .postTransactionToLedger(transactionMessage);
      if (lmPostingResponse.isPostingSuccess()) {
        messageSender.sendPaymentMessage(subscriptionKey, transactionMessage);
        saveReconMessage(transactionMessage, processResponse);
      } else {
        internalMessagelogger.debug(
            "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
        processResponse = responseConverter
            .buildPaymentProcessResponse(lmPostingResponse, transactionMessage);
        saveReconMessage(transactionMessage, processResponse);
      }
    } catch (FPSOutTransactionManagerException ex) {
      internalMessagelogger.debug(
          "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
      processResponse.setPaymentStatus(PaymentStatusEnum.FAILED.name());
      processResponse.setTransactionMessage(transactionMessage);
      saveReconMessage(transactionMessage, processResponse);
      throw ex;
    }
    return processResponse;
  }

  protected abstract void setTransactionCode(TransactionMessage transactionMessage);

  private void addTransactionStatus(TransactionMessage message, TransactionStatusValueEnum status) {
    message.getAdditionalInfo().put(TRANSACTION_STATUS.name(), status.name());
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage,
      Event event, TransactionMessageTypeEnum scope) {
    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(event).
        scope(scope).
        serviceName(ServiceNames.TRANSACTION_MANAGER).
        build();
  }

  private void saveReconMessage(TransactionMessage transactionMessage,
      PaymentProcessResponse processResponse) {
    String transactionType = transactionMessage.getHeader().getType();
    Boolean isSuccess = SUCCESS.name().equals(processResponse.getPaymentStatus());
    try {
      if (transactionType != null) {
        Event eventStatus = isSuccess ? Event.SUCCESS : Event.INT_REJECT;
        TransactionMessageTypeEnum transactionMessageType = transactionType
            .equals(TransactionMessageTypeEnum.ON_US.name()) ? TransactionMessageTypeEnum.ON_US
            : TransactionMessageTypeEnum.CHILD_SUBSCRIPTION_TRANSFER;
        reconciliationHelper
            .saveReconciliationMessage(buildReconciliationMessage(transactionMessage,
                eventStatus, transactionMessageType));
      }
    } catch (Exception ex) {
      internalMessagelogger
          .error("Reconciliation message not saved in recon log DB as {}", ex.getMessage());
    }
  }
}


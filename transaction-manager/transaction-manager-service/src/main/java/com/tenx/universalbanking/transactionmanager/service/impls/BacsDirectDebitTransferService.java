package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.reconciliation.logger.model.Event.SUCCESS;
import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS_REASON;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_BACS_DIRECT_DEBIT_TRANSFERS_DEBIT;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.RECEIVED_DIRECT_DEBIT_REVERSAL_DUE_TO_PAYMENT_REVERSAL;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.BacsDDStatus;
import com.tenx.universalbanking.transactionmanager.enums.BacsDDStatusReason;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
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
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BacsDirectDebitTransferService implements TransactionMessageService {

  private final Logger logger = getLogger(BacsDirectDebitTransferService.class);

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private PACS002MessageBuilder pacs002MessageBuilder;

  @Autowired
  private PaymentProcessResponseConverter responseConverter;

  @Autowired
  private TMExceptionBuilder exceptionBuilder;

  @Autowired
  private LedgerManagerClient lmClient;

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Override
  public TransactionMessageTypeEnum getType() {
    return TransactionMessageTypeEnum.BACS_DD_IN;
  }

  @Override
  public PaymentProcessResponse process(TransactionMessage transactionMessage,
      HttpServletRequest request) {

    logger.debug("In BACS_DD_IN MessageService : process function started");

    PaymentProcessResponse processResponse = new PaymentProcessResponse();

    transactionMessage.getMessages().forEach(item -> item.getAdditionalInfo().put(
        TRANSACTION_CODE.name(), PAYMENTS_ISSUED_BACS_DIRECT_DEBIT_TRANSFERS_DEBIT.getValue()
    ));
    logger.debug(
        "Setting TranscationCode Value:{}", PAYMENTS_ISSUED_BACS_DIRECT_DEBIT_TRANSFERS_DEBIT
            .getValue());

    if (BacsDDStatusReason.DD_IN_REVERSAL.name()
        .equals(transactionMessage.getAdditionalInfo().get(TRANSACTION_STATUS_REASON.name()))) {
      transactionMessage.getAdditionalInfo()
          .put(TRANSACTION_STATUS.name(), TransactionStatusValueEnum.NEW);
      transactionMessage.getAdditionalInfo().put(TRANSACTION_CODE.name(),RECEIVED_DIRECT_DEBIT_REVERSAL_DUE_TO_PAYMENT_REVERSAL);
      messageSender.sendPaymentMessageBacs(transactionMessage);
      processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());
      processResponse.setTransactionMessage(transactionMessage);
      return processResponse;

    } else if (BacsDDStatus.REJECT.name()
        .equals(transactionMessage.getAdditionalInfo().get(TRANSACTION_STATUS.name()))) {
      transactionMessage.getAdditionalInfo()
          .put(TRANSACTION_STATUS.name(), TransactionStatusValueEnum.FAILED);
      messageSender.sendPaymentMessageBacs(transactionMessage);
      processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());
      processResponse.setTransactionMessage(transactionMessage);
      return processResponse;
    }
    messageServiceProcessorHelper.generateTransactionAndCorrelationIds(transactionMessage);
    BacsDDStatus oldTransactionStatus= BacsDDStatus.valueOf(transactionMessage.getAdditionalInfo().get(TRANSACTION_STATUS.name()).toString());

    transactionMessage.getAdditionalInfo()
        .put(TRANSACTION_STATUS.name(), TransactionStatusValueEnum.RESERVE);


    PaymentDecisionTransactionResponse paymentDecisionResponse =
        messageServiceProcessorHelper.callPaymentDecisionService(transactionMessage);

    Boolean status = messageServiceProcessorHelper
        .isPaymentDecisionSuccess(paymentDecisionResponse);

    if (status) {
      logger.debug("In BACS_DD_IN MessageService : Payment decision Success");

      String subscriptionKey = messageServiceProcessorHelper
          .getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.PACS003);

      messageServiceProcessorHelper.addTracingHeaders(transactionMessage, request);

      processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());

      transactionMessage.getAdditionalInfo()
          .put(TRANSACTION_STATUS.name(), TransactionStatusValueEnum.COMPLETE);
      LedgerPostingResponse lmPostingResponse = lmClient
          .postTransactionToLedger(transactionMessage);
      if (lmPostingResponse.isPostingSuccess()) {
        messageSender.sendPaymentMessage(subscriptionKey, transactionMessage);
        reconciliationHelper.saveReconciliationMessage(
            buildReconciliationMessage(transactionMessage));
      }
      else {
        logger.debug(
            "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
        return responseConverter.buildPaymentProcessResponse(lmPostingResponse, transactionMessage);
      }

    } else {
      logger.error("Downstream Service Name :{} , API Name : {}", "Payment Decision Framework",
          "payment-decision");
      logger.error("Input Transaction Message {} ",
          appendTransactionMessageRequest(transactionMessage));
      if(null!=paymentDecisionResponse.getDecisionReason()){
        logger.error("Payment Decision Response Code {}"
                + " Payment Decision Response Message {} :",
            paymentDecisionResponse.getDecisionReason().getCode(),
            paymentDecisionResponse.getDecisionReason().getMessage());
      }
      logger.debug("In BACS_DD_IN MessageService : Payment decision Failed");

      if (BacsDDStatus.NEW.equals(oldTransactionStatus)) {
        transactionMessage.getAdditionalInfo()
            .put(TRANSACTION_STATUS.name(), TransactionStatusValueEnum.FAILED);
        transactionMessage.getAdditionalInfo().put(TRANSACTION_STATUS_REASON.name(),
            BacsDDStatusReason.FIRST_ATTEMPT_UNAVAILABLE_BALANCE);

      } else if (BacsDDStatus.FAILED.equals(oldTransactionStatus)
          && BacsDDStatusReason.FIRST_ATTEMPT_UNAVAILABLE_BALANCE.name()
          .equals(transactionMessage.getAdditionalInfo().get(TRANSACTION_STATUS_REASON.name()))) {
        transactionMessage.getAdditionalInfo().put(TRANSACTION_STATUS_REASON.name(),
            BacsDDStatusReason.SECOND_ATTEMPT_UNAVAILABLE_BALANCE);
      }
      processResponse.setPaymentStatus(PaymentDecisionResponse.FAILED.name());
      ReasonDto reasonDto = new ReasonDto();
      reasonDto.setCode(paymentDecisionResponse.getDecisionReason().getCode());
      reasonDto.setMessage(paymentDecisionResponse.getDecisionReason().getMessage());
      processResponse.setReason(reasonDto);
      messageServiceProcessorHelper.addTransactionStatus(transactionMessage,
          TransactionStatusValueEnum.FAILED);
      messageSender.sendPaymentMessageBacs(transactionMessage);
    }

    processResponse.setTransactionMessage(transactionMessage);

    return processResponse;
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage) {

    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(SUCCESS).
        scope(TransactionMessageTypeEnum.BACS_DD_IN).
        serviceName(ServiceNames.TRANSACTION_MANAGER).build();
  }
}

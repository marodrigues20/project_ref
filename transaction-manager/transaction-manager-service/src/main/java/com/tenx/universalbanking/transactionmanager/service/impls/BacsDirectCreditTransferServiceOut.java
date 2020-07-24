package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.reconciliation.logger.model.Event.SUCCESS;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_FOR_BACS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.BACS_CT_OUT;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS_DEBIT;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.enums.BacsDCPaymentStatus;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BacsDirectCreditTransferServiceOut implements TransactionMessageService {

  private final Logger logger = getLogger(BacsDirectCreditTransferServiceOut.class);

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private TMExceptionBuilder exceptionBuilder;

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Override
  public TransactionMessageTypeEnum getType() {
    return TransactionMessageTypeEnum.BACS_CT_OUT;
  }

  @Override
  public PaymentProcessResponse process(TransactionMessage transactionMessage,
      HttpServletRequest request) {

    logger.debug("In BACS_CT_OUT MessageService : process function started");

    PaymentProcessResponse processResponse = new PaymentProcessResponse();
    BacsDCPaymentStatus paymentStatus = BacsDCPaymentStatus.valueOf(transactionMessage.getAdditionalInfo().get(TRANSACTION_STATUS.name()).toString());
    transactionMessage.getMessages().get(0).getAdditionalInfo()
        .put(PAYMENT_METHOD_TYPE.name(), BACS_CT_OUT);
    transactionMessage.getMessages().forEach(item -> item.getAdditionalInfo().put(
        TRANSACTION_CODE.name(), PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS_DEBIT.getValue()
    ));

    messageServiceProcessorHelper.generateTransactionAndCorrelationIds(transactionMessage);
    logger.error("Transaction Status Message " + paymentStatus);

    switch (paymentStatus) {
      case SUB_NOT_FOUND_APPLIED:
      case PROCESSOR_FAILED_APPLIED:
      case SUB_NOT_ACTIVE_APPLIED:
      case ACC_CLOSED_APPLIED:
        messageSender.sendPaymentMessageBacs(transactionMessage);
        processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());
        reconciliationHelper.saveReconciliationMessage(
                buildReconciliationMessage(transactionMessage));
        break;
      default:
        logger.error("Http Server error exception Transaction Status Not found.");
        throw exceptionBuilder
            .buildBacsTransactionManagerException(INTERNAL_SERVER_ERROR_FOR_BACS.getStatusCode(),
                INTERNAL_SERVER_ERROR_FOR_BACS.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return processResponse;
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage) {

    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
            TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
            transactionCorrelationId(transactionCorrelationId).
            event(SUCCESS).
            scope(TransactionMessageTypeEnum.BACS_CT_OUT).
            serviceName(ServiceNames.TRANSACTION_MANAGER).build();
  }

}

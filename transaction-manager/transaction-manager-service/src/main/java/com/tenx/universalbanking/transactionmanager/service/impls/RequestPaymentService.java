package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum.PAYMENT_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.REQUEST_PAYMENT_BY_CARD;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.fundaccountmanager.client.api.PaymentControllerApi;
import com.tenx.universalbanking.fundaccountmanager.client.model.SubmitPaymentResponse;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageCorrelationIdGenerator;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageTransactionIdGenerator;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.mapper.FAMTransactionMessageMapper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestPaymentService implements TransactionMessageService {

  private final Logger logger = getLogger(RequestPaymentService.class);


  @Autowired
  private PaymentControllerApi paymentControllerApi;

  @Autowired
  private FAMTransactionMessageMapper famTransactionMessageMapper;

  @Autowired
  private TransactionMessageCorrelationIdGenerator correlationIdGenerator;

  @Autowired
  private TransactionMessageTransactionIdGenerator transactionIdGenerator;

  @Autowired
  private LedgerManagerClient ledgerManagerClient;

  @Autowired
  private MessageSender messageSender;

  private static final String AUTHORISED = "AUTHORISED";

  private static final String FAILURE = "FAILURE";

  private static final String SUCCESS = "SUCCESS";

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Override
  public TransactionMessageTypeEnum getType() {
    return REQUEST_PAYMENT_BY_CARD;
  }

  @Override
  public PaymentProcessResponse process(TransactionMessage transactionMessage,
      HttpServletRequest request) {
    Event eventStatus;

    correlationIdGenerator.addCorrelationId(transactionMessage);
    transactionMessage.getMessages().forEach(transactionIdGenerator::addTransactionId);

    PaymentProcessResponse processResponse = new PaymentProcessResponse();

    String tenantKey = transactionMessage.getAdditionalInfo()
        .get(TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY.name()).toString();
    String subscriptionKey = transactionMessage.getMessages().get(0).getAdditionalInfo()
        .get(SUBSCRIPTION_KEY.name()).toString();
    logger.debug("Calling FAM with TRANSACTION_CORRELATION_ID: {} ",
            transactionMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()));

    try {
      SubmitPaymentResponse submitPaymentResponse = paymentControllerApi
          .submitPayment(tenantKey,
              famTransactionMessageMapper.toFAMTransactionMessage(transactionMessage));

      TransactionMessage famTransactionMessage = famTransactionMessageMapper
          .toTransactionMessage(submitPaymentResponse.getTransactionMessage());

      String status = famTransactionMessage.getMessages().stream()
          .filter(item -> item.getType().equals(PaymentMessageTypeEnum.CAAA002.name())).findFirst()
          .get()
          .getMessage().get(PAYMENT_STATUS.name()).toString();

      if (status.equals(AUTHORISED)) {
        processResponse.setPaymentStatus(SUCCESS);
      } else {
        processResponse.setPaymentStatus(FAILURE);
      }

      processResponse.setTransactionMessage(famTransactionMessage);

      LedgerPostingResponse lmPostingResponse = ledgerManagerClient
          .postTransactionToLedger(famTransactionMessage);
      if (lmPostingResponse.isPostingSuccess()) {
        messageSender.sendPaymentMessage(subscriptionKey, famTransactionMessage);
        if (status.equals(AUTHORISED)) {
          reconciliationHelper.saveReconciliationMessage(
              buildReconciliationMessage(famTransactionMessage));
        }
      } else {
        logger.debug(
            "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
      }

    } catch (Exception e) {
      logger.error("There is some issue while getting the response from FAM {} ", e.getMessage());
      processResponse.setPaymentStatus(FAILURE);
    }
    return processResponse;
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage) {
    String transactionCorrelationId =  transactionMessage.getAdditionalInfo().get(
            TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
            transactionCorrelationId(transactionCorrelationId).
            event(Event.SUCCESS).
            scope(TransactionMessageTypeEnum.REQUEST_PAYMENT_BY_CARD).
            serviceName(ServiceNames.TRANSACTION_MANAGER).
            build();
  }
}

package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_ACCEPTOR_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.COMMON_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.AUTH_MATCHING_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.POSTED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN003;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN005;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.CLEARING_CUSTOMER_CARD_CROSS_BORDER_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.CLEARING_CUSTOMER_CARD_DOMESTIC_PURCHASE_TRANSACTION;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.CLEARING_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.factory.PaymentMessageServiceFactory;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.rest.responses.SettlementResponse;
import com.tenx.universalbanking.transactionmanager.service.PaymentMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.validation.MessageValidator;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchSettlementProcessor {

  private final Logger LOGGER = getLogger(BatchSettlementProcessor.class);

  @Autowired
  private PaymentMessageServiceFactory paymentMessageServiceFactory;

  @Autowired
  private MessageValidator messageValidator;

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private PaymentAuthorisations paymentAuthorisationsRepository;

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Autowired
  private GeneratorUtil generatorUtil;

  @Transactional
  public SettlementResponse process(TransactionMessage message, HttpServletRequest request) {
    messageValidator.validateAnyMessage(message, CAIN003, CAIN005);
    PaymentMessage paymentMessage = message.getMessages().get(0);
    PaymentMessageService authService = paymentMessageServiceFactory
        .getPaymentMessageService(paymentMessage);
    authService.setTransactionType(paymentMessage);
    Authorisations authorisations = authService.getAuthorisations(message);
    Map<String, Object> messageAdditionalInfo = new HashMap<>();
    if (message.getAdditionalInfo() != null) {
      messageAdditionalInfo.putAll(message.getAdditionalInfo());
    }
    message.setAdditionalInfo(messageAdditionalInfo);

    if (authorisations == null) {
      message.getAdditionalInfo().put(AUTH_MATCHING_STATUS.name(), false);
      messageServiceProcessorHelper.generateTransactionAndCorrelationIds(message);
    } else {
      paymentMessage.getMessage().put(AUTHORISATION_CODE.name(),
          authorisations.getId().getAuthorisationCode());
      message.getAdditionalInfo().put(AUTH_MATCHING_STATUS.name(), true);
      message.getMessages().get(0).getAdditionalInfo()
          .put(TRANSACTION_ID.name(), authorisations.getTransactionId());
      message.getAdditionalInfo().put(TRANSACTION_CORRELATION_ID.name(),
          authorisations.getCorrelationId());
      authorisations.setMatched(true);
      paymentAuthorisationsRepository.save(authorisations);
    }

    setTransactionCode(paymentMessage);

    setTransactionStatus(message);

    setRequestId(message);

    messageServiceProcessorHelper.addTracingHeaders(message, request);
    messageSender
        .sendPaymentMessage(
            message.getMessages().get(0).getAdditionalInfo().get(SUBSCRIPTION_KEY.name()).toString()
            , message);
    reconciliationHelper.saveReconciliationMessage(
        buildReconciliationMessage(message));
    return new SettlementResponse(SUCCESS.name());
  }

  private void setRequestId(TransactionMessage message) {
    if (message.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.REQUEST_ID.name()) == null) {
      message.getAdditionalInfo().put(TransactionMessageAdditionalInfoEnum.REQUEST_ID.name(),
          generatorUtil.generateRandomKey());
    }
  }

  private void setTransactionStatus(TransactionMessage transactionMessage) {
    transactionMessage.getAdditionalInfo().put(TRANSACTION_STATUS.name(), POSTED.name());
  }

  private void setTransactionCode(PaymentMessage paymentMessage) {
    Map<String, Object> message = paymentMessage.getMessage();
    String mCC = message.get(MERCHANT_CATEGORY_CODE.name()).toString();
    switch (mCC) {
      case "6010":
      case "6011":
      case "6012":
        if (message.get(CARD_ACCEPTOR_COUNTRY_CODE.name()).toString()
            .equalsIgnoreCase(message.get(COMMON_COUNTRY_CODE.name()).toString())) {
          paymentMessage.getAdditionalInfo().put(TRANSACTION_CODE.name(),
              CLEARING_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL.getValue());
        } else {
          paymentMessage.getAdditionalInfo().put(TRANSACTION_CODE.name(),
              CLEARING_CUSTOMER_CARD_CROSS_BORDER_CASH_WITHDRAWAL.getValue());
        }
        break;
      default:
        paymentMessage.getAdditionalInfo().put(TRANSACTION_CODE.name(),
            CLEARING_CUSTOMER_CARD_DOMESTIC_PURCHASE_TRANSACTION.getValue());
        break;
    }
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage) {
    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(Event.SUCCESS).
        scope(TransactionMessageTypeEnum.CLEARING).
        serviceName(ServiceNames.TRANSACTION_MANAGER).
        build();
  }
}


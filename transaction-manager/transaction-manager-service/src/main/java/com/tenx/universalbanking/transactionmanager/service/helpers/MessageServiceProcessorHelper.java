package com.tenx.universalbanking.transactionmanager.service.helpers;

import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.api.PaymentDecisionControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.enums.errormapping.PdfErrorMapping;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions;
import com.tenx.universalbanking.transactionmanager.enums.TransactionReason;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageCorrelationIdGenerator;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageTransactionIdGenerator;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageServiceProcessorHelper {

  private final Logger logger = getLogger(MessageServiceProcessorHelper.class);

  @Autowired
  private TransactionMessageCorrelationIdGenerator correlationIdGenerator;

  @Autowired
  private TransactionMessageTransactionIdGenerator transactionIdGenerator;

  @Autowired
  private PaymentDecisionControllerApi paymentDecisionControllerApi;

  @Autowired
  private PDFTransactionMessageMapper pdfMapper;

  @Autowired
  private TMExceptionBuilder exceptionbuilder;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private TracingHeadersFilter tracingHeadersFilter;

  public void generateTransactionAndCorrelationIds(TransactionMessage message, boolean isReversal,
      Authorisations authorisation){
    if(isReversal){
      // For reserve, get the initial card auth message's correlation id/transaction id so LM can find the
      // initial card auth message to reverse
      correlationIdGenerator.addCorrelationId(message, authorisation.getCorrelationId());
      transactionIdGenerator.addTransactionId(message.getMessages().get(0), authorisation.getTransactionId());
    } else{
      correlationIdGenerator.addCorrelationId(message);
      message.getMessages().forEach(transactionIdGenerator::addTransactionId);
    }
  }

  public void generateTransactionAndCorrelationIds(TransactionMessage message) {
    generateTransactionAndCorrelationIds(message, false, null);
  }

  public void generateCorrelationId(TransactionMessage message) {
    correlationIdGenerator.addCorrelationId(message);
  }

  public void addTransactionStatus(TransactionMessage transactionMessage,
      TransactionStatusValueEnum status) {
    transactionMessage.getAdditionalInfo().put(TRANSACTION_STATUS.name(), status.name());
  }

  public PaymentDecisionTransactionResponse callPaymentDecisionService(TransactionMessage message) {
    return paymentDecisionControllerApi
        .makePaymentDecision(pdfMapper.toClientTransactionMessage(message));
  }

  public Boolean isPaymentDecisionSuccess(PaymentDecisionTransactionResponse response) {
    return PaymentDecisionResponse.SUCCESS
        .equals(PaymentDecisionResponse.valueOf(response.getDecisionResponse()));
  }

  public String getSubscriptionKey(TransactionMessage message,
      PaymentMessageTypeEnum paymentMessageType) {

    String subscriptionKey;
    Optional<String> optSubscriptionKey;

    try {
      optSubscriptionKey = message.getMessages().stream()
          .filter(paymentMessage ->
              paymentMessageType.name().equals(paymentMessage.getType()))
          .map(paymentMessage -> paymentMessage
              .getAdditionalInfo()
              .get(PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY.name()).toString())
          .findAny();
    } catch (RuntimeException e) {
      logger.error("Exception has occured while fetching subscription details");
      logger.error("Subscription key not found {}", e);
      logger.error("Input Transaction Message {} ", appendTransactionMessageRequest(message));
      throw exceptionbuilder.buildTransactionException(
          TransactionManagerExceptions.SUBSCRIPTION_KEY_NOTFOUND);
    }

    if (optSubscriptionKey.isPresent()) {
      subscriptionKey = optSubscriptionKey.get();
    } else {
      logger.error("Subscription key not found.");
      logger.error("Input Transaction Message {} ", appendTransactionMessageRequest(message));
      throw exceptionbuilder.buildTransactionException(
          TransactionManagerExceptions.SUBSCRIPTION_KEY_NOTFOUND);
    }
    return subscriptionKey;
  }

  public String getSubscriptionStatus(TransactionMessage message,
                                   PaymentMessageTypeEnum paymentMessageType) {

    String subscriptionStatus;
    Optional<String> optSubscriptionStatus;

    try {
      optSubscriptionStatus = message.getMessages().stream()
              .filter(paymentMessage ->
                      paymentMessageType.name().equals(paymentMessage.getType()))
              .map(paymentMessage -> paymentMessage
                      .getAdditionalInfo()
                      .get(PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_STATUS.name()).toString())
              .findAny();
    } catch (RuntimeException e) {
      logger.error("Exception has occured while fetching subscription details");
      logger.error("Subscription status not found {}", e);
      logger.error("Input Transaction Message {} ", appendTransactionMessageRequest(message));
      throw exceptionbuilder.buildTransactionException(
              TransactionManagerExceptions.SUBSCRIPTION_STATUS_NOTFOUND);
    }

    if (optSubscriptionStatus.isPresent()) {
      subscriptionStatus = optSubscriptionStatus.get();
    } else {
      logger.error("Subscription status not found.");
      logger.error("Input Transaction Message {} ", appendTransactionMessageRequest(message));
      throw exceptionbuilder.buildTransactionException(
              TransactionManagerExceptions.SUBSCRIPTION_STATUS_NOTFOUND);
    }
    return subscriptionStatus;
  }

  public ReasonDto getGenericFailureReason() {
    ReasonDto reason = new ReasonDto();
    reason.setCode(TransactionReason.GENERIC_FAILURE.getFailureCode());
    reason.setMessage(TransactionReason.GENERIC_FAILURE.getFailureMessage());
    return reason;
  }

  public ReasonDto getFailureReason(PaymentDecisionTransactionResponse pdfResponse) {
    PdfErrorMapping mapping = PdfErrorMapping.getEnum(pdfResponse.getDecisionReason().getCode());

    if (!Objects.isNull(mapping)) {

      return new ReasonDto(mapping.getTransactionManagerErrorCode(),
          pdfResponse.getDecisionReason().getMessage());
    }

    return new ReasonDto(TransactionReason.GENERIC_FAILURE.getFailureCode(),
        TransactionReason.GENERIC_FAILURE.getFailureMessage());
  }

  public void addPaymentMethodType(TransactionMessage message) {
    if (!isEmpty(message.getMessages())) {
      message.getMessages().forEach(paymentMessage ->
              paymentMessage.getAdditionalInfo().put(PAYMENT_METHOD_TYPE.name(), message.getHeader().getType()));
    }
  }

  public void sendtoKafka(TransactionMessage transactionMessage, PaymentMessageTypeEnum paymentMessageTypeEnum) {
   String subscriptionKey = getSubscriptionKey(transactionMessage, paymentMessageTypeEnum);

    messageSender.sendPaymentMessage(subscriptionKey, transactionMessage);
  }

  public void addTracingHeaders(TransactionMessage transactionMessage, HttpServletRequest request) {

    transactionMessage.getAdditionalInfo().putAll(tracingHeadersFilter.filter(request));
  }
}

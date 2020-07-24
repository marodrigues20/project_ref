package com.tenx.universalbanking.transactionmanager.orchestration.factory;

import static com.tenx.universalbanking.transactionmessage.enums.InterestApplicationAdjustmentEnum.AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.InterestApplicationAdjustmentEnum.BASE_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.InterestApplicationAdjustmentEnum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.InterestApplicationAdjustmentEnum.VALUE_DATE_TIME;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.APPLICATION_ADJUSTMENTS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static java.util.Collections.singletonList;

import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.tenx.universalbanking.transactionmanager.model.ApplicationAdjustmentMessageData;
import com.tenx.universalbanking.transactionmanager.utils.DateConversionUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;

@Component
class ApplicationAdjustmentMessageFactory {

  private final GeneratorUtil generatorUtil;
  private final DateConversionUtils dateConversionUtils;
  private static final String DEFAULT_CURRENCY = "GBP";

  ApplicationAdjustmentMessageFactory(GeneratorUtil generatorUtil, DateConversionUtils dateConversionUtils) {
    this.generatorUtil = generatorUtil;
    this.dateConversionUtils = dateConversionUtils;
  }

  public TransactionMessage create(ApplicationAdjustmentMessageData applicationAdjustmentMessageData) {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setHeader(createTransactionMessageHeader());
    transactionMessage.setAdditionalInfo(createAdditionalInfo(applicationAdjustmentMessageData));
    transactionMessage.setMessages(singletonList(createPaymentMessage(applicationAdjustmentMessageData)));
    return transactionMessage;
  }

  private PaymentMessage createPaymentMessage(ApplicationAdjustmentMessageData applicationAdjustmentMessageData) {
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(name(APPLICATION_ADJUSTMENTS));
    paymentMessage.setMessage(createMessage(applicationAdjustmentMessageData));
    paymentMessage.setAdditionalInfo(createAdditionalInfoForMessage(applicationAdjustmentMessageData));
    return paymentMessage;
  }

  private Map<String, Object> createMessage(ApplicationAdjustmentMessageData applicationAdjustmentMessageData) {
    Map<String, Object> message = new HashMap<>();
    message.put(name(TRANSACTION_DATE),
        dateConversionUtils.format(applicationAdjustmentMessageData.getTransactionDate()));
    message.put(name(VALUE_DATE_TIME),
        dateConversionUtils.format(applicationAdjustmentMessageData.getTransactionValueDate()));
    message.put(name(AMOUNT), applicationAdjustmentMessageData.getAmount());
    message.put(name(BASE_CURRENCY_CODE), DEFAULT_CURRENCY);
    return message;
  }

  private Map<String, Object> createAdditionalInfoForMessage(
      ApplicationAdjustmentMessageData applicationAdjustmentMessageData) {
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put(name(TRANSACTION_ID), generatorUtil.generateRandomKey());
    additionalInfo.put(name(SUBSCRIPTION_KEY), applicationAdjustmentMessageData.getSubscriptionKey());
    return additionalInfo;
  }

  private Map<String, Object> createAdditionalInfo(ApplicationAdjustmentMessageData applicationAdjustmentMessageData) {
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put(name(TRANSACTION_CORRELATION_ID), applicationAdjustmentMessageData.getCorrelationId());
    return additionalInfo;
  }

  private TransactionMessageHeader createTransactionMessageHeader() {
    TransactionMessageHeader transactionMessageHeader = new TransactionMessageHeader();
    transactionMessageHeader.setType(name(APPLICATION_ADJUSTMENTS));
    return transactionMessageHeader;
  }

  private String name(Enum enumName) {
    return enumName.name();
  }

}

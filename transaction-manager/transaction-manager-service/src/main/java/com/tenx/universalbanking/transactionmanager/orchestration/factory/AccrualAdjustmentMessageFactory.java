package com.tenx.universalbanking.transactionmanager.orchestration.factory;


import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.ACCRUAL_ADJUSTMENTS_FOR_APPLIED_ACCRUALS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.ACCRUAL_ADJUSTMENTS_PENDING_APPLICATION;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static java.util.Collections.singletonList;

import com.tenx.universalbanking.transactionmanager.model.AccrualAdjustmentMessageData;
import com.tenx.universalbanking.transactionmanager.utils.DateConversionUtils;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.InterestAccrualAdjustmentAppliedEnum;
import com.tenx.universalbanking.transactionmessage.enums.InterestAccrualAdjustmentPendingEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
class AccrualAdjustmentMessageFactory {

  private final GeneratorUtil generatorUtil;
  private final DateConversionUtils dateConversionUtils;
  private static final String DEFAULT_CURRENCY = "GBP";

  AccrualAdjustmentMessageFactory(GeneratorUtil generatorUtil, DateConversionUtils dateConversionUtils) {
    this.generatorUtil = generatorUtil;
    this.dateConversionUtils = dateConversionUtils;
  }

  public TransactionMessage create(AccrualAdjustmentMessageData accrualAdjustmentMessageData) {

    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setHeader(createTransactionMessageHeader(getMessageType(accrualAdjustmentMessageData)));
    transactionMessage.setMessages(singletonList(createPaymentMessage(accrualAdjustmentMessageData)));
    transactionMessage.setAdditionalInfo(createAdditionalInfo(accrualAdjustmentMessageData));

    return transactionMessage;
  }

  private PaymentMessage createPaymentMessage(AccrualAdjustmentMessageData accrualAdjustmentMessageData) {
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(name(getMessageType(accrualAdjustmentMessageData)));
    paymentMessage.setMessage(createMessage(accrualAdjustmentMessageData));
    paymentMessage.setAdditionalInfo(createAdditionalInfoForMessage(accrualAdjustmentMessageData));
    return paymentMessage;
  }

  private Map<String, Object> createInterestAdjustmentAppliedMessage(
      AccrualAdjustmentMessageData accrualAdjustmentMessageData) {
    Map<String, Object> message = new HashMap<>();

    message.put(name(InterestAccrualAdjustmentAppliedEnum.TRANSACTION_DATE),
        dateConversionUtils.format(accrualAdjustmentMessageData.getTransactionDate()));
    message.put(name(InterestAccrualAdjustmentAppliedEnum.VALUE_DATE_TIME),
        dateConversionUtils.format(accrualAdjustmentMessageData.getTransactionValueDate()));
    message.put(name(InterestAccrualAdjustmentAppliedEnum.AMOUNT), accrualAdjustmentMessageData.getAmount());
    message.put(name(InterestAccrualAdjustmentAppliedEnum.BASE_CURRENCY_CODE), DEFAULT_CURRENCY);
    message.put(name(InterestAccrualAdjustmentAppliedEnum.INTEREST_ACCRUED_DATE),
        dateConversionUtils.format(accrualAdjustmentMessageData.getAccruedDate()));
    message.put(name(InterestAccrualAdjustmentAppliedEnum.INTEREST_APPLICATION_DATE),
        dateConversionUtils.format(accrualAdjustmentMessageData.getInterestApplicationDate()));
    message.put(name(InterestAccrualAdjustmentAppliedEnum.INTEREST_COMPOUND_DATE),
        dateConversionUtils.format(accrualAdjustmentMessageData.getInterestCompoundDate()));

    return message;
  }

  private Map<String, Object> createInterestAdjustmentPendingMessage(
      AccrualAdjustmentMessageData accrualAdjustmentMessageData) {
    Map<String, Object> message = new HashMap<>();

    message.put(name(InterestAccrualAdjustmentPendingEnum.TRANSACTION_DATE),
        dateConversionUtils.format(accrualAdjustmentMessageData.getTransactionDate()));
    message.put(name(InterestAccrualAdjustmentPendingEnum.VALUE_DATE_TIME),
        dateConversionUtils.format(accrualAdjustmentMessageData.getTransactionValueDate()));
    message.put(name(InterestAccrualAdjustmentPendingEnum.AMOUNT), accrualAdjustmentMessageData.getAmount());
    message.put(name(InterestAccrualAdjustmentPendingEnum.BASE_CURRENCY_CODE), DEFAULT_CURRENCY);
    message.put(name(InterestAccrualAdjustmentPendingEnum.INTEREST_ACCRUED_DATE),
        dateConversionUtils.format(accrualAdjustmentMessageData.getAccruedDate()));
    message.put(name(InterestAccrualAdjustmentPendingEnum.INTEREST_APPLICATION_DATE),
        dateConversionUtils.format(accrualAdjustmentMessageData.getInterestApplicationDate()));
    message.put(name(InterestAccrualAdjustmentPendingEnum.INTEREST_COMPOUND_DATE),
        dateConversionUtils.format(accrualAdjustmentMessageData.getInterestCompoundDate()));

    return message;
  }

  private Map<String, Object> createAdditionalInfoForMessage(
      AccrualAdjustmentMessageData accrualAdjustmentMessageData) {
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put(name(TRANSACTION_ID), generatorUtil.generateRandomKey());
    additionalInfo.put(name(SUBSCRIPTION_KEY), accrualAdjustmentMessageData.getSubscriptionKey());
    return additionalInfo;
  }

  private Enum getMessageType(AccrualAdjustmentMessageData messageData) {
    return messageData.isCreateJournal() ? ACCRUAL_ADJUSTMENTS_PENDING_APPLICATION
        : ACCRUAL_ADJUSTMENTS_FOR_APPLIED_ACCRUALS;
  }

  private TransactionMessageHeader createTransactionMessageHeader(Enum type) {
    TransactionMessageHeader transactionMessageHeader = new TransactionMessageHeader();
    transactionMessageHeader.setType(name(type));
    return transactionMessageHeader;
  }

  private String name(Enum enumName) {
    return enumName.name();
  }

  private Map<String, Object> createAdditionalInfo(AccrualAdjustmentMessageData accrualAdjustmentMessageData) {
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put(name(TRANSACTION_CORRELATION_ID), accrualAdjustmentMessageData.getCorrelationId());
    return additionalInfo;
  }

  private Map<String, Object> createMessage(AccrualAdjustmentMessageData accrualAdjustmentMessageData) {

    if (getMessageType(accrualAdjustmentMessageData).equals(ACCRUAL_ADJUSTMENTS_PENDING_APPLICATION)) {
      return createInterestAdjustmentPendingMessage(accrualAdjustmentMessageData);
    } else {
      return createInterestAdjustmentAppliedMessage(accrualAdjustmentMessageData);
    }
  }
}

package com.tenxbanking.cardrails.adapter.secondary.cardclearing.util;

import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BILLING_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BILLING_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.COMMON_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.MERCHANT_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.FEE_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.FEE_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.FEE_DESCRIPTION;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.VALUE_DATE_TIME;
import static com.tenx.universalbanking.transactionmessage.enums.TaxEnum.DESCRIPTION;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.FEES_AND_CHARGES;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.DEBIT_CREDIT_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PARENT_TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SETTLEMENT_DATE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.TAX;

import com.google.common.collect.ImmutableMap;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.Cain003Enum;
import com.tenx.universalbanking.transactionmessage.enums.TaxEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class Cain003MessageBuilder {

  public TransactionMessage create(CardClearing cardClearing) {

    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionMessageHeader messageHeader = new TransactionMessageHeader();
    messageHeader.setType(cardClearing.getCain003().getCardTransactionType().name());
    transactionMessage.setHeader(messageHeader);

    Cain003 cain003 = cardClearing.getCain003();

    final List<PaymentMessage> paymentMessageList = new ArrayList<>();

    PaymentMessage paymentMessage = new PaymentMessage();

    paymentMessage.setType(PaymentMessageTypeEnum.CAIN003.name());

    Map<String, Object> messageMap = new HashMap();

    messageMap.put(COMMON_COUNTRY_CODE.name(), cain003.getMerchant().getAddress().getCountryCode());
    messageMap.put(BILLING_AMOUNT.name(), cain003.getBillingAmount().getAmount());
    messageMap.put(CARD_ACCEPTOR_ID.name(), cain003.getBanknetReferenceNumber());
    messageMap.put(TRANSACTION_DATE.name(), cain003.getTransactionDate());
    messageMap.put(MERCHANT_CATEGORY_CODE.name(), cain003.getMerchant().getCategoryCode());
    messageMap.put(MERCHANT_NUMBER.name(), cain003.getMerchant().getAcceptorIdCode());
    messageMap.put(BILLING_CURRENCY_CODE.name(), cain003.getBillingAmount().getCurrency());
    messageMap.put(TRANSACTION_AMOUNT.name(), cain003.getTransactionAmount().getAmount());
    messageMap.put(BANKNET_REFERENCE_NUMBER.name(), cain003.getBanknetReferenceNumber());

    paymentMessage.setMessage(messageMap);

    cain003.getFee().ifPresent(
        fee -> {
          paymentMessageList.add(createFeeMessage(fee, cardClearing.getSubscriptionKey()));
          fee.getTax().ifPresent(
              tax -> paymentMessageList
                  .add(createPaymentMessageForTax(fee, tax, cardClearing.getSubscriptionKey())));
        });

    Map<String, Object> messageAdditionalMap = new HashMap();

    messageAdditionalMap.put(DEBIT_CREDIT_INDICATOR.name(), cain003.getMessageType().name());
    messageAdditionalMap.put(PAYMENT_METHOD_TYPE.name(), cain003.getPaymentMethodType().name());
    messageAdditionalMap.put(SETTLEMENT_DATE.name(), cain003.getTransactionDate());
    messageAdditionalMap.put(AUTHORISATION_CODE.name(), cain003.getAuthCode());
    messageAdditionalMap.put(SUBSCRIPTION_KEY.name(), cardClearing.getSubscriptionKey());

    transactionMessage.setAdditionalInfo(messageAdditionalMap);
    setSubscriptionKey(cardClearing, paymentMessage);
    paymentMessageList.add(paymentMessage);
    transactionMessage.setMessages(paymentMessageList);

    return transactionMessage;
  }

  private void setSubscriptionKey(CardClearing cardClearing, PaymentMessage message) {
    HashMap<String, Object> additionalInfoMap = (HashMap<String, Object>) message
        .getAdditionalInfo();
    if (additionalInfoMap != null) {
      additionalInfoMap
          .put(PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY.name(),
              cardClearing.getSubscriptionKey().toString());
      additionalInfoMap
          .put(PaymentMessageAdditionalInfoEnum.PRODUCT_KEY.name(),
              cardClearing.getProductKey());

    }
  }

  private PaymentMessage createFeeMessage(Fee fee, UUID subscriptionKey) {
    Map<String, Object> messageBody = createMessageBody(fee);
    Map<String, Object> additionalInfo = createAdditionalInfo(fee, subscriptionKey);
    PaymentMessage outputMessage = new PaymentMessage();
    outputMessage.setMessage(messageBody);
    outputMessage.setAdditionalInfo(additionalInfo);
    outputMessage.setType(FEES_AND_CHARGES.name());
    return outputMessage;
  }

  private Map<String, Object> createMessageBody(Fee fee) {
    return new ImmutableMap.Builder<String, Object>()
        .put(TRANSACTION_CODE.name(), fee.getTransactionCode())
        .put(Cain003Enum.TRANSACTION_DATE.name(), fee.getTransactionDate())
        .put(FEE_CURRENCY_CODE.name(), fee.getFeeCurrencyCode())
        .put(VALUE_DATE_TIME.name(), fee.getValueDateTime())
        .put(FEE_AMOUNT.name(), fee.getAmount())
        .put(FEE_DESCRIPTION.name(), fee.getDescription())
        .build();
  }

  private PaymentMessage createPaymentMessageForTax(Fee fee, Tax tax, UUID subscriptionKey) {
    Map<String, Object> messageBody = createMessageBodyForTax(fee, tax);
    Map<String, Object> additionalInfo = createAdditionalInfoForTax(tax, subscriptionKey);
    PaymentMessage outputMessage = new PaymentMessage();
    outputMessage.setMessage(messageBody);
    outputMessage.setAdditionalInfo(additionalInfo);
    outputMessage.setType(TAX.name());
    return outputMessage;
  }


  private Map<String, Object> createMessageBodyForTax(Fee fee, Tax tax) {
    return new ImmutableMap.Builder<String, Object>()
        .put(TaxEnum.TRANSACTION_CODE.name(), fee.getTransactionCode())
        .put(TaxEnum.TRANSACTION_DATE.name(), fee.getTransactionDate())
        .put(TaxEnum.CURRENCY_CODE.name(), fee.getFeeCurrencyCode())
        .put(DESCRIPTION.name(), tax.getStatementDescription())
        .put(TaxEnum.VALUE_DATE_TIME.name(), fee.getValueDateTime())
        .put(TaxEnum.AMOUNT.name(), tax.getTaxAmount())
        .build();
  }

  private Map<String, Object> createAdditionalInfoForTax(Tax tax, UUID subscriptionKey) {
    return ImmutableMap.of(TRANSACTION_ID.name(), tax.getTransactionId(),
        PARENT_TRANSACTION_ID.name(), tax.getParentTransactionId(),
        SUBSCRIPTION_KEY.name(), subscriptionKey.toString());
  }

  private Map<String, Object> createAdditionalInfo(Fee fee, UUID subscriptionKey) {
    return new ImmutableMap.Builder<String, Object>()
        .put(TRANSACTION_ID.name(), fee.getTransactionId())
        .put(SUBSCRIPTION_KEY.name(), subscriptionKey.toString())
        .put(TRANSACTION_CORRELATION_ID.name(), fee.getTransactionCorrelationId())
        .put(TRANSACTION_CODE.name(), fee.getTransactionCode())
        .build();
  }


}

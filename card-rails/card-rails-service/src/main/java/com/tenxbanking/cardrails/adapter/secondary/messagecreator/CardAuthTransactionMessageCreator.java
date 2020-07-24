package com.tenxbanking.cardrails.adapter.secondary.messagecreator;

import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARDHOLDER_PRESENT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_ACCEPTOR_CONDITION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_DATA_ENTRY_MODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_TOKEN;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.EXCHANGE_RATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.INITIATOR_PARTY_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TOTAL_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_AMOUNT_QUALIFIER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_TIME;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.VALUE_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.FEE_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.FEE_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.FEE_DESCRIPTION;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.VALUE_DATE_TIME;
import static com.tenx.universalbanking.transactionmessage.enums.TaxEnum.DESCRIPTION;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_TYPE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH_VIA_ADVICE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.FEES_AND_CHARGES;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.DEBIT_CREDIT_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PARENT_TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PRODUCT_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.TAX;
import static com.tenxbanking.cardrails.adapter.secondary.messagecreator.PosEntryModeEnum.convertPosEntryCode;

import com.google.common.collect.ImmutableMap;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.TaxEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardAuthTransactionMessageCreator {

  private static final String TENANT_KEY = "10000";
  private static final String TRANSACTION_ACCOUNT_QUALIFIER = "ACTL";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


  private final PaymentTransactionCodeMapper paymentTransactionCodeMapper;
  private final GeneratorUtil generatorUtil;


  private static final Map<CardTransactionType, TransactionStatusValueEnum> TRANSACTION_STATUS_MAP = ImmutableMap
      .of(CardTransactionType.REVERSAL, TransactionStatusValueEnum.REVERSE,
          CardTransactionType.ADVICE, TransactionStatusValueEnum.APPROVED,
          CardTransactionType.AUTH, TransactionStatusValueEnum.RESERVE);


  @Autowired
  public CardAuthTransactionMessageCreator(
      PaymentTransactionCodeMapper paymentTransactionCodeMapper,
      GeneratorUtil generatorUtil) {
    this.paymentTransactionCodeMapper = paymentTransactionCodeMapper;
    this.generatorUtil = generatorUtil;
  }

  private final Logger logger = LoggerFactory.getLogger(CardAuthTransactionMessageCreator.class);

  public TransactionMessage create(AuthTransaction authTransaction) {

    //TODO: finalise required fields here

    Cain001 cain001 = authTransaction.getCain001();

    final TransactionMessage cardAuthMessage = new TransactionMessage();
    final Map<String, Object> tmAdditionalInfo = new HashMap<>();

    cardAuthMessage.setHeader(createHeader(cain001));

    final List<PaymentMessage> paymentMessages = new ArrayList<>();

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN001.name());
    paymentMessage.setMessage(buildMessage(authTransaction));
    paymentMessage.setAdditionalInfo(buildPaymentAdditionalInfo(authTransaction));

    paymentMessages.add(paymentMessage);

    cain001.getFee().ifPresent(
        fee -> {
          paymentMessages.add(createFeeMessage(fee, authTransaction.getSubscriptionKey()));
          fee.getTax().ifPresent(
              tax -> paymentMessages
                  .add(createPaymentMessageForTax(fee, tax, authTransaction.getSubscriptionKey())));
        });

    cardAuthMessage.setMessages(paymentMessages);

    tmAdditionalInfo.put(TENANT_PARTY_KEY.name(), TENANT_KEY);
    String requestID = generatorUtil.generateRandomKey();
    tmAdditionalInfo.put(REQUEST_ID.name(), requestID);
    tmAdditionalInfo.put(TRANSACTION_CORRELATION_ID.name(),
        authTransaction.getCain001().getCorrelationId().toString());

    logger.info("REQUEST_ID" + requestID);

    tmAdditionalInfo.put(TRANSACTION_STATUS.name(),
        TRANSACTION_STATUS_MAP.get(authTransaction.getCain001().getCardTransactionType()).name());

    cardAuthMessage.setAdditionalInfo(tmAdditionalInfo);

    return cardAuthMessage;
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
        .put(TRANSACTION_DATE.name(), fee.getTransactionDate())
        .put(FEE_CURRENCY_CODE.name(), fee.getFeeCurrencyCode())
        .put(VALUE_DATE_TIME.name(), fee.getValueDateTime())
        .put(FEE_AMOUNT.name(), fee.getAmount())
        .put(FEE_DESCRIPTION.name(), fee.getDescription())
        .build();
  }

  private Map<String, Object> createAdditionalInfo(Fee fee, UUID subscriptionKey) {
    return new ImmutableMap.Builder<String, Object>()
        .put(TRANSACTION_ID.name(), fee.getTransactionId())
        .put(SUBSCRIPTION_KEY.name(), subscriptionKey.toString())
        .put(TRANSACTION_CORRELATION_ID.name(), fee.getTransactionCorrelationId())
        .put(TRANSACTION_CODE.name(), fee.getTransactionCode())
        .build();
  }

  private Map<String, Object> buildPaymentAdditionalInfo(AuthTransaction authTransaction) {

    Map<String, Object> additionalInfo = new HashMap<>();

    additionalInfo.put(PARTY_KEY.name(), authTransaction.getPartyKey().toString());
    additionalInfo
        .put(TRANSACTION_ID.name(), authTransaction.getCain001().getTransactionId().toString());
    additionalInfo.put(PRODUCT_KEY.name(), authTransaction.getProductKey().toString());
    additionalInfo.put(SUBSCRIPTION_KEY.name(), authTransaction.getSubscriptionKey().toString());
    additionalInfo.put(DEBIT_CREDIT_INDICATOR.name(), authTransaction.getCreditDebit().name());
    additionalInfo.put(PAYMENT_METHOD_TYPE.name(),
        authTransaction.getCain001().getPaymentMethodType().name());
    additionalInfo.put(TRANSACTION_CODE.name(),
        paymentTransactionCodeMapper.map(authTransaction.getPaymentMethodType()).getValue());
    return additionalInfo;
  }

  private Map<String, Object> buildMessage(AuthTransaction authTransaction) {

    Cain001 cain001 = authTransaction.getCain001();
    Map<String, Object> message = new HashMap<>();

    message.put(CARD_TOKEN.name(), authTransaction.getCain001().getCardId());
    if(authTransaction.getType().equals(CardTransactionType.REVERSAL)){
      //message.put(TOTAL_AMOUNT.name(), authTransaction.getCain001().getReversalAmount().get().getTransaction().getAmount().toPlainString());
      message.put(TOTAL_AMOUNT.name(), "0");
      message.put(AMOUNT.name(), authTransaction.getCain001().getReversalAmount().get().getBilling().getAmount().toPlainString());
    }else{
      message.put(TOTAL_AMOUNT.name(), authTransaction.getTransactionAmount().getAmount().toPlainString());
      message.put(AMOUNT.name(), authTransaction.getBillingAmount().getAmount().toPlainString());
    }
    message.put(TRANSACTION_TYPE.name(), cain001.getProcessingCode());
    message.put(TRANSACTION_CURRENCY_CODE.name(), cain001.getCurrencyCode());
    message.put(MERCHANT_CATEGORY_CODE.name(), cain001.getMerchantCategoryCode());
    message.put(CARD_DATA_ENTRY_MODE.name(),
        convertPosEntryCode(cain001.getPointOfServiceEntryMode()));
    message.put(TRANSACTION_DATE.name(),
        DATE_TIME_FORMATTER.format(cain001.getTransactionDate().atZone(ZoneId.of("Z"))));
    message.put(TRANSACTION_TIME.name(),
        cain001.getTransactionDate().atZone(ZoneId.of("Z")).toLocalTime().toString());
    message.put(BANKNET_REFERENCE_NUMBER.name(), cain001.getBanknetReferenceNumber());
    message
        .put(CARDHOLDER_PRESENT.name(),
            isCardHolderPresent(cain001.getPointOfServiceEntryMode()));
    message.put(TRANSACTION_AMOUNT_QUALIFIER.name(), TRANSACTION_ACCOUNT_QUALIFIER);
    message.put(VALUE_DATE.name(),
        DATE_TIME_FORMATTER.format(cain001.getTransactionDate().atZone(ZoneId.of("Z"))));
    message.put(CARD_ACCEPTOR_CONDITION_CODE.name(), cain001.getPointOfServiceConditionCode());
    message.put(EXCHANGE_RATE.name(), cain001.getConversionRate());
    message.put(INITIATOR_PARTY_ID.name(), cain001.getNetworkId());
    return message;
  }

  private boolean isCardHolderPresent(String cardDataEntryMode) {
    return (!cardDataEntryMode.equals("01") && !cardDataEntryMode.equals("08"));
  }

  private TransactionMessageHeader createHeader(Cain001 cain001) {
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(
        cain001.getCardTransactionType().equals(CardTransactionType.ADVICE) ? CARD_AUTH_VIA_ADVICE
            .name() : CARD_AUTH.name());
    return header;
  }

}

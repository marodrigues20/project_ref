package com.tenx.universalbanking.transactionmanager.service.turbine;

import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.CONTACTLESS_INPUT;
import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.CONTACTLESS_USING_TRACK_RULES;
import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.ICC_CONTACTLESS;
import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.MAG_STRIPE;
import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.MAG_STRIPE_FALL_BACK;
import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.MAG_STRIPE_TRACK;
import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.MANUAL_KEY_ENTRY;
import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.PAN_ENTRY_VIA_CONTACTLESS;
import static com.tenx.universalbanking.transactionmanager.model.PosEntryMode.PAN_VIA_ECOMMERCE;
import static com.tenx.universalbanking.transactionmanager.model.TransactionType.PURCHASE;
import static com.tenx.universalbanking.transactionmanager.model.TransactionType.WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARDHOLDER_PRESENT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_ACCEPTOR_CONDITION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_ACCEPTOR_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_ACCEPTOR_NAME;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_DATA_ENTRY_MODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_TOKEN;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.EXCHANGE_RATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.INITIATOR_PARTY_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.INITIATOR_TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TOTAL_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_AMOUNT_QUALIFIER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_FEE_AMT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_TIME;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.VALUE_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.PosEntryModeEnum.convertPosEntryCode;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_TYPE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH_VIA_ADVICE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.DEBIT_CREDIT_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PRODUCT_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.ATM_MAG_STRIPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.CONTACTLESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.DOMESTIC_POS_CHIP_AND_PIN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_POS_CHIP_AND_PIN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.MAIL_TELEPHONE_ORDER;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.ONLINE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.POS_MAG_STRIPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.UNKNOWN;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;

import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.model.CardAuth;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class CardAuthTransactionMessageBuilder {

  private static final String COUNTRY_CODE = "GBR";
  private static final String POS_EXP = "051";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  @Autowired
  private GeneratorUtil generatorUtil;

  private final Logger logger = LoggerFactory.getLogger(CardAuthTransactionMessageBuilder.class);

  public TransactionMessage create(CardAuth cardAuth, Card card, boolean advice) {

    TransactionMessage cardAuthMessage = new TransactionMessage();
    Map<String, Object> tmAdditionalInfo = new HashMap<>();

    cardAuthMessage.setHeader(createHeader(advice));

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN001.name());
    paymentMessage.setMessage(buildMessage(card.getId(), cardAuth));
    paymentMessage.setAdditionalInfo(buildPaymentAdditionalInfo(cardAuth, card));
    cardAuthMessage.setMessages(singletonList(paymentMessage));

    tmAdditionalInfo.put(TENANT_PARTY_KEY.name(), card.getTenantKey());
    String requestID = generatorUtil.generateRandomKey();
    tmAdditionalInfo.put(REQUEST_ID.name(), requestID);
    logger.info("REQUEST_ID" + requestID);
    cardAuthMessage.setAdditionalInfo(tmAdditionalInfo);

    return cardAuthMessage;
  }

  private Map<String, Object> buildPaymentAdditionalInfo(CardAuth cardAuth,
      Card debitCardResponse) {

    Map<String, Object> additionalInfo = new HashMap<>();

    additionalInfo.put(PARTY_KEY.name(), debitCardResponse.getPartyKey().toString());
    additionalInfo.put(PRODUCT_KEY.name(), debitCardResponse.getProductKey().toString());
    additionalInfo.put(SUBSCRIPTION_KEY.name(), debitCardResponse.getSubscriptionKey().toString());
    additionalInfo
        .put(SUBSCRIPTION_STATUS.name(), debitCardResponse.getSubscriptionStatus().name());

    additionalInfo.put(DEBIT_CREDIT_INDICATOR.name(), cardAuth.getCreditDebit().name());
    additionalInfo.put(PAYMENT_METHOD_TYPE.name(), buildPaymentMethodType(cardAuth).name());
    additionalInfo.put(CARD_PROCESSOR_ACCOUNT_ID.name(), debitCardResponse.getProcessorAccountId());
    return additionalInfo;
  }

  private Map<String, Object> buildMessage(String cardId, CardAuth cardAuth) {

    Map<String, Object> message = new HashMap<>();

    message.put(CARD_TOKEN.name(), cardId);
    message.put(TOTAL_AMOUNT.name(), cardAuth.getTotalAmount().toPlainString());
    message.put(AMOUNT.name(), cardAuth.getAmount().toPlainString());
    message.put(TRANSACTION_TYPE.name(), cardAuth.getTransactionType());
    message.put(TRANSACTION_CURRENCY_CODE.name(), cardAuth.getTransactionCurrencyCode());
    message.put(MERCHANT_CATEGORY_CODE.name(), cardAuth.getMerchantCategoryCode());
    message.put(CARD_DATA_ENTRY_MODE.name(), convertPosEntryCode(cardAuth.getCardDataEntryMode()));
    message.put(TRANSACTION_DATE.name(), toString(cardAuth.getTransactionDatetime()));
    message.put(BANKNET_REFERENCE_NUMBER.name(), cardAuth.getBanknetReference());
    message.put(CARDHOLDER_PRESENT.name(), isCardHolderPresent(cardAuth.getCardDataEntryMode()));
    message.put(TRANSACTION_AMOUNT_QUALIFIER.name(), "ACTL");
    message.put(TRANSACTION_TIME.name(), cardAuth.getTransactionTime());
    message.put(VALUE_DATE.name(), cardAuth.getTransactionDate().toString());
    message.put(CARD_ACCEPTOR_CONDITION_CODE.name(), cardAuth.getCardConditionCode());
    message.put(TRANSACTION_FEE_AMT.name(), cardAuth.getFeeAmount());
    message.put(EXCHANGE_RATE.name(), cardAuth.getConversionRate());
    message.put(INITIATOR_TRANSACTION_ID.name(), cardAuth.getSystemTraceNumber());
    message.put(INITIATOR_PARTY_ID.name(), cardAuth.getNetworkCode());
    message.put(CARD_ACCEPTOR_NAME.name(), cardAuth.getMerchantName());
    message.put(CARD_ACCEPTOR_ID.name(), cardAuth.getCardAcceptorIdCode());
    return message;
  }

  private boolean isCardHolderPresent(String cardDataEntryMode) {
    return (!cardDataEntryMode.equals("01") && !cardDataEntryMode.equals("08"));
  }

  private PaymentMethodTypeEnum buildPaymentMethodType(CardAuth cardAuth) {

    PaymentMethodTypeEnum paymentMethodTypeEnum;

    String transactionType = cardAuth.getTransactionType();
    String countryCode = cardAuth.getMerchantCountryCode();
    String cardDataEntryMode = cardAuth.getCardDataEntryMode();
    String conditionCode = cardAuth.getCardConditionCode();

    if (isCashWithdrawal(transactionType, cardDataEntryMode)) {
      paymentMethodTypeEnum = getCashWithdrawalType(countryCode);
    } else if (isPurchase(transactionType, cardDataEntryMode, conditionCode)) {
      paymentMethodTypeEnum = getPurchaseType(countryCode);
    } else if (isPosMagStripe(transactionType, cardDataEntryMode, conditionCode)) {
      paymentMethodTypeEnum = POS_MAG_STRIPE;
    } else if (isAtmMagStripe(transactionType, cardDataEntryMode, conditionCode)) {
      paymentMethodTypeEnum = ATM_MAG_STRIPE;
    } else if (isMailTelephoneOrder(cardDataEntryMode, conditionCode)) {
      paymentMethodTypeEnum = MAIL_TELEPHONE_ORDER;
    } else if (isOnline(cardDataEntryMode, conditionCode)) {
      paymentMethodTypeEnum = ONLINE;
    } else if (isContactless(transactionType, cardDataEntryMode)) {
      paymentMethodTypeEnum = CONTACTLESS;
    } else {
      paymentMethodTypeEnum = UNKNOWN;
    }

    return paymentMethodTypeEnum;
  }

  private boolean isCashWithdrawal(String transactionType, String cardDataEntryMode) {
    return (WITHDRAWAL.getTransactionId().equals(transactionType) && POS_EXP
        .equals(cardDataEntryMode));
  }

  private PaymentMethodTypeEnum getCashWithdrawalType(String countryCode) {
    return COUNTRY_CODE.equalsIgnoreCase(countryCode) ? DOMESTIC_CASH_WITHDRAWAL
        : INTERNATIONAL_CASH_WITHDRAWAL;
  }

  private boolean isPurchase(String transactionType, String cardDataEntryMode,
      String conditionCode) {
    return ((PURCHASE.getTransactionId().equals(transactionType) && POS_EXP
        .equals(cardDataEntryMode) && "00".equals(conditionCode)));
  }

  private PaymentMethodTypeEnum getPurchaseType(String countryCode) {
    return COUNTRY_CODE.equalsIgnoreCase(countryCode) ? DOMESTIC_POS_CHIP_AND_PIN
        : INTERNATIONAL_POS_CHIP_AND_PIN;
  }

  private boolean isPosMagStripe(String transactionType, String cardDataEntryModeExp,
      String conditionCode) {
    return (PURCHASE.getTransactionId().equals(transactionType) &&
        (MAG_STRIPE.getTisoCode().equals(cardDataEntryModeExp) ||
            MAG_STRIPE_FALL_BACK.getTisoCode().equals(cardDataEntryModeExp) ||
            MAG_STRIPE_TRACK.getTisoCode().equals(cardDataEntryModeExp)) && "00"
        .equals(conditionCode));
  }

  private boolean isAtmMagStripe(String transactionType, String cardDataEntryModeExp,
      String conditionCode) {
    return (WITHDRAWAL.getTransactionId().equals(transactionType) &&
        (MAG_STRIPE.getTisoCode().equals(cardDataEntryModeExp) ||
            MAG_STRIPE_FALL_BACK.getTisoCode().equals(cardDataEntryModeExp) ||
            MAG_STRIPE_TRACK.getTisoCode().equals(cardDataEntryModeExp)) && "00"
        .equals(conditionCode));
  }

  private boolean isMailTelephoneOrder(String cardDataEntryModeExp, String conditionCode) {
    return (MANUAL_KEY_ENTRY.getTisoCode().equals(cardDataEntryModeExp) && ((
        "01".equals(conditionCode) || "08".equals(conditionCode))));
  }

  private boolean isOnline(String cardDataEntryModeExp, String conditionCode) {
    return (PAN_VIA_ECOMMERCE.getTisoCode().equals(cardDataEntryModeExp) && ((
        "01".equals(conditionCode) || "08".equals(conditionCode))));
  }

  private boolean isContactless(String transactionType, String cardDataEntryModeExp) {
    return (PURCHASE.getTransactionId().equals(transactionType) &&
        (PAN_ENTRY_VIA_CONTACTLESS.getTisoCode().equals(cardDataEntryModeExp) ||
            CONTACTLESS_USING_TRACK_RULES.getTisoCode().equals(cardDataEntryModeExp) ||
            CONTACTLESS_INPUT.getTisoCode().equals(cardDataEntryModeExp) ||
            ICC_CONTACTLESS.getTisoCode().equals(cardDataEntryModeExp)));
  }

  private TransactionMessageHeader createHeader(boolean advice) {
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(advice ? CARD_AUTH_VIA_ADVICE.name() : CARD_AUTH.name());
    return header;
  }

  private String toString(Instant instant) {
    return OffsetDateTime.ofInstant(instant, UTC).format(DATE_TIME_FORMATTER);
  }


}

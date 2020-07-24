package com.tenxbanking.cardrails.adapter.primary.rest.mapper;

import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.ATM_MAG_STRIPE;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.CONTACTLESS;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.DOMESTIC_POS_CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.INTERNATIONAL_POS_CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.MAIL_TELEPHONE_ORDER;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.ONLINE;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.POS_MAG_STRIPE;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.UNKNOWN;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.CONTACTLESS_INPUT;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.CONTACTLESS_USING_TRACK_RULES;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.ICC_CONTACTLESS;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.MAG_STRIPE;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.MAG_STRIPE_FALL_BACK;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.MAG_STRIPE_TRACK;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.MANUAL_KEY_ENTRY;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.PAN_ENTRY_VIA_CONTACTLESS;
import static com.tenxbanking.cardrails.domain.model.PosEntryMode.PAN_VIA_ECOMMERCE;
import static com.tenxbanking.cardrails.domain.model.TransactionType.PURCHASE;
import static com.tenxbanking.cardrails.domain.model.TransactionType.WITHDRAWAL;

import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodTypeMapper {

  //TODO: fix this hard coded country code
  private static final String COUNTRY_CODE = "GBR";
  private static final String POS_EXP = "051";

  public PaymentMethodType map(
      String processingCode,
      String countryCode,
      String pointOfSaleEntryMode,
      String conditionCode) {

    processingCode = getShortProcessingCode(processingCode);

    PaymentMethodType paymentMethodTypeEnum = UNKNOWN;

    if (isCashWithdrawal(processingCode, pointOfSaleEntryMode)) {
      paymentMethodTypeEnum = getCashWithdrawalType(countryCode);
    } else if (isPurchase(processingCode, pointOfSaleEntryMode, conditionCode)) {
      paymentMethodTypeEnum = getPurchaseType(countryCode);
    } else if (isPosMagStripe(processingCode, pointOfSaleEntryMode, conditionCode)) {
      paymentMethodTypeEnum = POS_MAG_STRIPE;
    } else if (isAtmMagStripe(processingCode, pointOfSaleEntryMode, conditionCode)) {
      paymentMethodTypeEnum = ATM_MAG_STRIPE;
    } else if (isMailTelephoneOrder(pointOfSaleEntryMode, conditionCode)) {
      paymentMethodTypeEnum = MAIL_TELEPHONE_ORDER;
    } else if (isOnline(pointOfSaleEntryMode, conditionCode)) {
      paymentMethodTypeEnum = ONLINE;
    } else if (isContactless(processingCode, pointOfSaleEntryMode)) {
      paymentMethodTypeEnum = CONTACTLESS;
    }

    return paymentMethodTypeEnum;
  }

  private boolean isCashWithdrawal(String transactionType, String cardDataEntryMode) {
    return (WITHDRAWAL.getTransactionId().equals(transactionType) && POS_EXP
        .equals(cardDataEntryMode));
  }

  private PaymentMethodType getCashWithdrawalType(String countryCode) {
    return COUNTRY_CODE.equalsIgnoreCase(countryCode) ? DOMESTIC_CASH_WITHDRAWAL
        : INTERNATIONAL_CASH_WITHDRAWAL;
  }

  private boolean isPurchase(String transactionType, String cardDataEntryMode,
      String conditionCode) {
    return ((PURCHASE.getTransactionId().equals(transactionType) && POS_EXP
        .equals(cardDataEntryMode) && "00".equals(conditionCode)));
  }

  private PaymentMethodType getPurchaseType(String countryCode) {
    return COUNTRY_CODE.equalsIgnoreCase(countryCode) ? DOMESTIC_POS_CHIP_AND_PIN
        : INTERNATIONAL_POS_CHIP_AND_PIN;
  }

  private boolean isPosMagStripe(String transactionType, String cardDataEntryModeExp,
      String conditionCode) {
    return (PURCHASE.getTransactionId().equals(transactionType)
        && (MAG_STRIPE.getPosCode().equals(cardDataEntryModeExp)
        || MAG_STRIPE_FALL_BACK.getPosCode().equals(cardDataEntryModeExp)
        || MAG_STRIPE_TRACK.getPosCode().equals(cardDataEntryModeExp)) && "00"
        .equals(conditionCode));
  }

  private boolean isAtmMagStripe(String transactionType, String cardDataEntryModeExp,
      String conditionCode) {
    return (WITHDRAWAL.getTransactionId().equals(transactionType)
        && (MAG_STRIPE.getPosCode().equals(cardDataEntryModeExp)
        || MAG_STRIPE_FALL_BACK.getPosCode().equals(cardDataEntryModeExp)
        || MAG_STRIPE_TRACK.getPosCode().equals(cardDataEntryModeExp)) && "00"
        .equals(conditionCode));
  }

  private boolean isMailTelephoneOrder(String cardDataEntryModeExp, String conditionCode) {
    return (MANUAL_KEY_ENTRY.getPosCode().equals(cardDataEntryModeExp)
        && (("01".equals(conditionCode) || "08".equals(conditionCode))));
  }

  private boolean isOnline(String cardDataEntryModeExp, String conditionCode) {
    return (PAN_VIA_ECOMMERCE.getPosCode().equals(cardDataEntryModeExp)
        && (("01".equals(conditionCode) || "08".equals(conditionCode))));
  }

  private boolean isContactless(String transactionType, String cardDataEntryModeExp) {
    return (PURCHASE.getTransactionId().equals(transactionType)
        && (PAN_ENTRY_VIA_CONTACTLESS.getPosCode().equals(cardDataEntryModeExp)
        || CONTACTLESS_USING_TRACK_RULES.getPosCode().equals(cardDataEntryModeExp)
        || CONTACTLESS_INPUT.getPosCode().equals(cardDataEntryModeExp)
        || ICC_CONTACTLESS.getPosCode().equals(cardDataEntryModeExp)));
  }

  private String getShortProcessingCode(String processingCode) {
    return processingCode.length() >= 2 ? processingCode.substring(0, 2) : processingCode;
  }

}
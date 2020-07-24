package com.tenx.tsys.proxybatch.service;

import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_DATA_ENTRY_MODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.COMMON_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.CONTACTLESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.DOMESTIC_POS_CHIP_AND_PIN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_POS_CHIP_AND_PIN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.MAIL_TELEPHONE_ORDER;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.MANUAL_ENTRY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.ONLINE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.POS_MAG_STRIPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.UNKNOWN;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PaymentMethodProvider {

  private static final String DOMESTIC_COUNTRY_CODE = "GBR";

  public void setPaymentMethodType(Map<String, Object> messageMap,
      Map<String, Object> paymentAdditionalInfo) {
    String mCC = messageMap.get(MERCHANT_CATEGORY_CODE.name()).toString();
    String terminalEntry = messageMap.get(CARD_DATA_ENTRY_MODE.name()).toString();
    String commonCountry = messageMap.get(COMMON_COUNTRY_CODE.name()).toString();
    String paymentMethod = null;
    if ("6010".equals(mCC) || "6011".equals(mCC)) {
      paymentMethod = isDomesticCountry(commonCountry) ? DOMESTIC_CASH_WITHDRAWAL.name()
          : INTERNATIONAL_CASH_WITHDRAWAL.name();
    } else if ("05".equals(terminalEntry) || "95".equals(terminalEntry)) {
      paymentMethod = isDomesticCountry(commonCountry) ? DOMESTIC_POS_CHIP_AND_PIN.name()
          : INTERNATIONAL_POS_CHIP_AND_PIN.name();
    } else if ("03".equals(terminalEntry) || "04".equals(terminalEntry)
        || "07".equals(terminalEntry) || "91".equals(terminalEntry)) {
      paymentMethod = CONTACTLESS.name();
    } else if ("02".equals(terminalEntry) || "80".equals(terminalEntry) || "90"
        .equals(terminalEntry)) {
      paymentMethod = POS_MAG_STRIPE.name();
    } else if ("01".equals(terminalEntry)) {
      paymentMethod = MAIL_TELEPHONE_ORDER.name();
    } else if ("81".equals(terminalEntry)) {
      paymentMethod = ONLINE.name();
    } else if ("79".equals(terminalEntry)) {
      paymentMethod = MANUAL_ENTRY.name();
    } else {
      paymentMethod = UNKNOWN.name();
    }
    paymentAdditionalInfo.put(PAYMENT_METHOD_TYPE.name(), paymentMethod);
  }

  private boolean isDomesticCountry(String commonCountryCode) {
    return DOMESTIC_COUNTRY_CODE.equals(commonCountryCode);
  }
}

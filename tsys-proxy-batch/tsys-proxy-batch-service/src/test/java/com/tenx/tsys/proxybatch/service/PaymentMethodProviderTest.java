package com.tenx.tsys.proxybatch.service;

import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.CARD_DATA_ENTRY_MODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.COMMON_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.MERCHANT_CATEGORY_CODE;
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
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class PaymentMethodProviderTest {

  PaymentMethodProvider paymentMethodProvider = new PaymentMethodProvider();

  @Test
  public void setPaymentMethodTypeDomesticCashWithdrawal() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6010", "05", "GBR"), result);
    assertThat(DOMESTIC_CASH_WITHDRAWAL.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeDomesticCashWithdrawal1() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6011", "05", "GBR"), result);
    assertThat(DOMESTIC_CASH_WITHDRAWAL.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeDomesticChipPin() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "05", "GBR"), result);
    assertThat(DOMESTIC_POS_CHIP_AND_PIN.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeDomesticChipPin2() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "95", "GBR"), result);
    assertThat(DOMESTIC_POS_CHIP_AND_PIN.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeInternationalCashWithdrawal1n() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6010", "05", "IND"), result);
    assertThat(INTERNATIONAL_CASH_WITHDRAWAL.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeInternationalChipPin() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "05", "IND"), result);
    assertThat(INTERNATIONAL_POS_CHIP_AND_PIN.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeInternationalChipPin2() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "95", "IND"), result);
    assertThat(INTERNATIONAL_POS_CHIP_AND_PIN.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeContactless() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "03", "IND"), result);
    assertThat(CONTACTLESS.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeContactless1() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "03", "IND"), result);
    assertThat(CONTACTLESS.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeContactless2() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "04", "IND"), result);
    assertThat(CONTACTLESS.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeContactless3() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "07", "IND"), result);
    assertThat(CONTACTLESS.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeContactless4() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "91", "IND"), result);
    assertThat(CONTACTLESS.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeMagstripe() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "02", "IND"), result);
    assertThat(POS_MAG_STRIPE.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeMagstripe1() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "80", "IND"), result);
    assertThat(POS_MAG_STRIPE.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeMagstripe2() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "90", "IND"), result);
    assertThat(POS_MAG_STRIPE.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeMailTelephone() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "01", "IND"), result);
    assertThat(MAIL_TELEPHONE_ORDER.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeOnline() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "81", "IND"), result);
    assertThat(ONLINE.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeManual() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "79", "IND"), result);
    assertThat(MANUAL_ENTRY.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void setPaymentMethodTypeUnknown() {
    Map<String, Object> result = new HashMap<>();
    paymentMethodProvider.setPaymentMethodType(getMessageMap("6012", "11", "IND"), result);
    assertThat(UNKNOWN.name())
        .isEqualTo(result.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  private Map<String, Object> getMessageMap(String mcc, String entryMode, String countryCode) {
    Map<String, Object> messageMap = new HashMap<>();
    messageMap.put(MERCHANT_CATEGORY_CODE.name(), mcc);
    messageMap.put(CARD_DATA_ENTRY_MODE.name(), entryMode);
    messageMap.put(COMMON_COUNTRY_CODE.name(), countryCode);
    return messageMap;
  }
}
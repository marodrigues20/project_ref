package com.tenxbanking.cardrails.adapter.primary.rest.mapper;

import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.DOMESTIC_POS_CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.INTERNATIONAL_POS_CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.POS_MAG_STRIPE;
import static org.assertj.core.api.Assertions.assertThat;

import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import org.junit.jupiter.api.Test;

class PaymentMethodTypeMapperTest {

  private final PaymentMethodTypeMapper underTest = new PaymentMethodTypeMapper();

  @Test
  void shouldReturnWhenCashWithdrawalDomestic() {
    PaymentMethodType transactionCode = underTest.map("01", "GBR", "051", null);
    assertThat(transactionCode).isEqualTo(DOMESTIC_CASH_WITHDRAWAL);
  }

  @Test
  void shouldReturnWhenCashWithdrawalDomesticWhenGivenLongProcessingCode() {
    PaymentMethodType transactionCode = underTest.map("01asdasd13123213", "GBR", "051", null);
    assertThat(transactionCode).isEqualTo(DOMESTIC_CASH_WITHDRAWAL);
  }

  @Test
  void shouldReturnWhenCashWithdrawalInternational() {
    PaymentMethodType transactionCode = underTest.map("01", "US", "051", null);
    assertThat(transactionCode).isEqualTo(INTERNATIONAL_CASH_WITHDRAWAL);
  }

  @Test
  void shouldReturnWhenChipAndPinDomestic() {
    PaymentMethodType transactionCode = underTest.map("00", "GBR", "051", "00");
    assertThat(transactionCode)
        .isEqualTo(DOMESTIC_POS_CHIP_AND_PIN);
  }

  @Test
  void shouldReturnUnknownWhenChipAndPinInternational() {
    PaymentMethodType transactionCode = underTest.map("00", "US", "051", "00");
    assertThat(transactionCode).isEqualTo(INTERNATIONAL_POS_CHIP_AND_PIN);
  }

  @Test
  void shouldReturnPosMagStripe() {
    PaymentMethodType transactionCode = underTest.map("00", null, "02", "00");
    assertThat(transactionCode)
        .isEqualTo(POS_MAG_STRIPE);
  }

  @Test
  void shouldReturnUnknownWhenManualKeyEntryAndCode1() {
    PaymentMethodType transactionCode = underTest.map("CHWD", null, null, "q");
    assertThat(transactionCode).isEqualTo(PaymentMethodType.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownWhenManualKeyEntryAndCode8() {
    PaymentMethodType transactionCode = underTest.map("CHWD", null, null, "8");
    assertThat(transactionCode).isEqualTo(PaymentMethodType.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownWhenPanECommerceAndCode8() {
    PaymentMethodType transactionCode = underTest.map("81", null, null, "8");
    assertThat(transactionCode).isEqualTo(PaymentMethodType.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownWhenPanECommerceAndCode1() {
    PaymentMethodType transactionCode = underTest.map("81", null, null, "1");
    assertThat(transactionCode).isEqualTo(PaymentMethodType.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownWhenContactlessAnd07() {
    PaymentMethodType transactionCode = underTest.map("CRDP", null, "1", "07");
    assertThat(transactionCode).isEqualTo(PaymentMethodType.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownWhenContactlessAnd08() {
    PaymentMethodType transactionCode = underTest.map("CRDP", null, "1", "08");
    assertThat(transactionCode).isEqualTo(PaymentMethodType.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownWhenContactlessAnd91() {
    PaymentMethodType transactionCode = underTest.map("CRDP", null, "1", "91");
    assertThat(transactionCode).isEqualTo(PaymentMethodType.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownWhenContactlessAnd92() {

    PaymentMethodType transactionCode = underTest.map("CRDP", null, "1", "92");
    assertThat(transactionCode).isEqualTo(PaymentMethodType.UNKNOWN);
  }
}
package com.tenxbanking.cardrails.adapter.secondary.messagecreator;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_MAG_STRIPE_TRANSACTION_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.UNKNOWN;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.DOMESTIC_POS_CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.MAIL_TELEPHONE_ORDER;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.POS_MAG_STRIPE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum;
import org.junit.jupiter.api.Test;

class PaymentTransactionCodeMapperTest {

  private final PaymentTransactionCodeMapper underTest = new PaymentTransactionCodeMapper();

  @Test
  void domesticCashWithdrawal() {
    PaymentsTransactionCodeEnum returned = underTest.map(DOMESTIC_CASH_WITHDRAWAL);

    assertThat(returned).isEqualTo(PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL);
  }

  @Test
  void internationalCashWithdrawal() {
    PaymentsTransactionCodeEnum returned = underTest.map(INTERNATIONAL_CASH_WITHDRAWAL);

    assertThat(returned).isEqualTo(PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL);
  }

  @Test
  void domesticPosChipAndPin() {
    PaymentsTransactionCodeEnum returned = underTest.map(DOMESTIC_POS_CHIP_AND_PIN);

    assertThat(returned).isEqualTo(PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD);
  }

  @Test
  void posMagStripe() {
    PaymentsTransactionCodeEnum returned = underTest.map(POS_MAG_STRIPE);

    assertThat(returned).isEqualTo(PAYMENTS_CUSTOMER_MAG_STRIPE_TRANSACTION_CASH_WITHDRAWAL);
  }

  @Test
  void defaultToUnknown() {
    PaymentsTransactionCodeEnum returned = underTest.map(MAIL_TELEPHONE_ORDER);

    assertThat(returned).isEqualTo(UNKNOWN);
  }

}
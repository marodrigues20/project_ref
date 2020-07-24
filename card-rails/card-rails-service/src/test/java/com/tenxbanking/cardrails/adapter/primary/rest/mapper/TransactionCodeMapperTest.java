package com.tenxbanking.cardrails.adapter.primary.rest.mapper;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.UNKNOWN;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.CONTACTLESS;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.POS_MAG_STRIPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class TransactionCodeMapperTest {

  @Mock
  private Cain001 cain001;
  private TransactionCodeMapper codeMapper = new TransactionCodeMapper();

  @Test
  void domesticCashWithdrawal() {
    when(cain001.getPaymentMethodType()).thenReturn(DOMESTIC_CASH_WITHDRAWAL);
    String transactionCode = codeMapper.getTransactionCode(cain001.getPaymentMethodType());
    assertThat(transactionCode).isEqualTo(PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL.getValue());
  }

  @Test
  void posMagneticStripe() {
    when(cain001.getPaymentMethodType()).thenReturn(POS_MAG_STRIPE);
    String transactionCode = codeMapper.getTransactionCode(cain001.getPaymentMethodType());
    assertThat(transactionCode).isEqualTo(PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL.getValue());
  }

  @Test
  void internationalCashWithdrawal() {
    when(cain001.getPaymentMethodType()).thenReturn(PaymentMethodType.INTERNATIONAL_CASH_WITHDRAWAL);
    String transactionCode = codeMapper.getTransactionCode(cain001.getPaymentMethodType());
    assertThat(transactionCode).isEqualTo(PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL.getValue());
  }

  @Test
  void domesticPostChipAndPin() {
    when(cain001.getPaymentMethodType()).thenReturn(PaymentMethodType.DOMESTIC_POS_CHIP_AND_PIN);
    String transactionCode = codeMapper.getTransactionCode(cain001.getPaymentMethodType());
    assertThat(transactionCode).isEqualTo(PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD.getValue());
  }

  @Test
  void defaultToUnknown() {
    when(cain001.getPaymentMethodType()).thenReturn(CONTACTLESS);
    String transactionCode = codeMapper.getTransactionCode(cain001.getPaymentMethodType());
    assertThat(transactionCode).isEqualTo(UNKNOWN.getValue());
  }

}
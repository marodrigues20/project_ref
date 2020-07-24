package com.tenxbanking.cardrails.adapter.secondary.messagecreator;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_MAG_STRIPE_TRANSACTION_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.UNKNOWN;

import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionCodeMapper {

  public PaymentsTransactionCodeEnum map(PaymentMethodType paymentMethodType) {

    switch (paymentMethodType) {
      case DOMESTIC_CASH_WITHDRAWAL:
        return PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL;
      case INTERNATIONAL_CASH_WITHDRAWAL:
        return PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL;
      case DOMESTIC_POS_CHIP_AND_PIN:
        return PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD;
      case POS_MAG_STRIPE:
        return PAYMENTS_CUSTOMER_MAG_STRIPE_TRANSACTION_CASH_WITHDRAWAL;
      default:
        return UNKNOWN;
    }
  }


}

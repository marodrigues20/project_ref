package com.tenxbanking.cardrails.adapter.primary.rest.mapper;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.DOMESTIC_POS_CHIP_AND_PIN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.POS_MAG_STRIPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.UNKNOWN;

import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import java.util.Map;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TransactionCodeMapper {

  private static final Map<PaymentMethodTypeEnum, String> TRANSACTION_CODE_MAP = Map
      .of(DOMESTIC_CASH_WITHDRAWAL, PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL.getValue(),
          POS_MAG_STRIPE, PAYMENTS_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL.getValue(),
          INTERNATIONAL_CASH_WITHDRAWAL, PAYMENTS_CUSTOMER_CARD_TRANSACTION_CROSS_BORDER_CASH_WITHDRAWEL.getValue(),
          DOMESTIC_POS_CHIP_AND_PIN, PAYMENTS_CUSTOMER_CARD_TRANSACTION_POINT_OF_SALE_PAYMENT_DEBIT_CARD.getValue());

  public String getTransactionCode(@NonNull final PaymentMethodType paymentMethodType) {
    PaymentMethodTypeEnum paymentMethod = PaymentMethodTypeEnum.valueOf(paymentMethodType.name());
    return TRANSACTION_CODE_MAP.getOrDefault(paymentMethod, UNKNOWN.name());
  }



}
package com.tenxbanking.cardrails.domain.model.transaction;

import static com.tenxbanking.cardrails.domain.model.transaction.CreditDebit.CREDIT;
import static com.tenxbanking.cardrails.domain.model.transaction.CreditDebit.DEBIT;

import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import java.util.Optional;

public interface AuthTransaction extends CardTransaction {

  Cain001 getCain001();

  Cain002 getCain002();

  Optional<String> getAuthCode();

  AuthResponseCode getAuthResponseCode();

  default CreditDebit getCreditDebit() {
    return getCain001().getCardTransactionType() == CardTransactionType.REVERSAL ? CREDIT : DEBIT;
  }

  @Override
  default PaymentMethodType getPaymentMethodType() {
    return getCain001().getPaymentMethodType();
  }

}

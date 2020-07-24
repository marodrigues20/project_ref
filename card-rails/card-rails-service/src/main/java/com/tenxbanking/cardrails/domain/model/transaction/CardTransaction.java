package com.tenxbanking.cardrails.domain.model.transaction;

import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import java.util.UUID;

public interface CardTransaction {

  String getCardId();

  UUID getSubscriptionKey();

  UUID getPartyKey();

  UUID getProductKey();

  String getTenantKey();

  Money getTransactionAmount();

  Money getBillingAmount();

  CardTransactionType getType();

  PaymentMethodType getPaymentMethodType();

}

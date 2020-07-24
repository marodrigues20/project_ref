package com.tenxbanking.cardrails.domain.model.transaction;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.CLEARING;

import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;

import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class CardClearing implements ClearingTransaction {

  @NonNull
  private final String cardId;
  @NonNull
  private final UUID subscriptionKey;
  @NonNull
  private final UUID partyKey;
  @NonNull
  private final UUID productKey;
  @NonNull
  private final String tenantKey;
  @NonNull
  private final Cain003 cain003;

  @Override
  public CardTransactionType getType() {
    return CLEARING;
  }

  @Override
  public Money getTransactionAmount() {
    return cain003.getTransactionAmount();
  }

  @Override
  public Money getBillingAmount() {
    return cain003.getBillingAmount();
  }

  @Override
  public PaymentMethodType getPaymentMethodType() {
    return getCain003().getPaymentMethodType();
  }

  // Cain003/Cain004 ??

}

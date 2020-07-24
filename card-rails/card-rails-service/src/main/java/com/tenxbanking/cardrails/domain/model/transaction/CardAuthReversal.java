package com.tenxbanking.cardrails.domain.model.transaction;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;

import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Money;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class CardAuthReversal implements AuthTransaction {

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
  private final Cain001 cain001;
  private final Cain002 cain002;

  public CardAuthReversal(String cardId,
      UUID subscriptionKey,
      UUID partyKey,
      UUID productKey,
      String tenantKey,
      Cain001 cain001) {
    this.cardId = cardId;
    this.subscriptionKey = subscriptionKey;
    this.partyKey = partyKey;
    this.productKey = productKey;
    this.tenantKey = tenantKey;
    this.cain001 = cain001;
    this.cain002 = null;
  }

  @Override
  public CardTransactionType getType() {
    return REVERSAL;
  }

  @Override
  public Money getTransactionAmount() {
    return cain001.getReversalAmount().get().getTransaction();
  }

  @Override
  public Money getBillingAmount() {
    return cain001.getReversalAmount().get().getBilling();
  }

  @Override
  public Optional<String> getAuthCode() {
    return Optional.of(cain001.getAuthCode());
  }

  @Override
  public AuthResponseCode getAuthResponseCode() {
    return cain001.getAuthResponseCode();
  }

}

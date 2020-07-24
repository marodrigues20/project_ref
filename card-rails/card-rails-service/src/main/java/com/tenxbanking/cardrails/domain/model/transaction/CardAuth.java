package com.tenxbanking.cardrails.domain.model.transaction;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static java.util.Optional.ofNullable;

import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import java.util.Optional;
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
public class CardAuth implements AuthTransaction {

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

  public CardAuth(
      String cardId,
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
    return AUTH;
  }

  @Override
  public Money getTransactionAmount() {
    return cain001.getTransactionAmount();
  }

  @Override
  public Money getBillingAmount() {
    return cain001.getBillingAmount();
  }

  @Override
  public Optional<String> getAuthCode() {
    return ofNullable(cain002)
        .map(Cain002::getAuthCode);
  }

  @Override
  public AuthResponseCode getAuthResponseCode() {
    return ofNullable(cain002)
        .map(Cain002::getAuthResponseCode).orElse(null);
  }

  public CardAuth withFee(Fee fee) {
    return this.toBuilder().cain001(cain001.addFee(fee)).build();
  }
}

package com.tenxbanking.cardrails.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor
public class Cain001 {

  @NonNull
  private final Money transactionAmount;
  @NonNull
  private final Money billingAmount;
  private final Money settlementAmount;
  private final ReversalAmount reversalAmount;
  @NonNull
  private final String merchantCategoryCode;
  @NonNull
  private final Instant transactionDate;
  @NonNull
  private final String accountQualifier;
  @NonNull
  private final String cardId;
  @NonNull
  private final String processingCode;
  private final BigDecimal conversionRate;
  private final String cardExpiryDate;
  @NonNull
  private final String pointOfServiceEntryMode;
  @NonNull
  private final String pointOfServiceConditionCode;
  @NonNull
  private final String networkId;
  @NonNull
  private final String cardAcceptorCountryCode;
  private final Fee fee;
  private final UUID transactionId;
  private final UUID correlationId;
  @NonNull
  private final String banknetReferenceNumber;
  @NonNull
  private final CardTransactionType cardTransactionType;
  private final String retrievalReferenceNumber;
  private final String authCode;
  private final AuthResponseCode authResponseCode;
  @NonNull
  private final PaymentMethodType paymentMethodType;

  public String getCurrencyCode() {
    return transactionAmount.getCurrency().getCurrencyCode();
  }

  public Currency getCurrency() {
    return transactionAmount.getCurrency();
  }

  public Cain001 addFee(Fee fee) {
    return this.toBuilder().fee(fee).build();
  }

  public Cain001 addTransactionIds(UUID transactionId, UUID correlationId) {
    return this.toBuilder().transactionId(transactionId).correlationId(correlationId).build();
  }

  public Optional<Fee> getFee() {
    return Optional.ofNullable(fee);
  }

  public Optional<ReversalAmount> getReversalAmount() {
    return Optional.ofNullable(reversalAmount);
  }

  public boolean isThereSettlement() {
    return getReversalAmount().isPresent() && getReversalAmount().map(s -> s.getSettlement() != null).get();
  }

  public boolean isThereTransaction() {
    return getReversalAmount().isPresent() && getReversalAmount().map(s -> s.getTransaction() != null).get();
  }

}

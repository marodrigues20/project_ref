package com.tenxbanking.cardrails.domain.model;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.CreditDebitEnum;
import com.tenxbanking.cardrails.domain.model.card.Merchant;
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
import lombok.Value;
import lombok.experimental.Wither;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class Cain003 {

  private static final String MESSAGE_TYPE = "CAIN003";

  @NonNull
  private final String cardId;
  private final UUID transactionId;
  private final UUID correlationId;
  @NonNull
  private final Instant createdDate;

  private final String authCode;
  @NonNull
  private final String banknetReferenceNumber;
  @NonNull
  private final CardTransactionType cardTransactionType;
  @NonNull
  private final Instant transactionDate;

  private final String retrievalReferenceNumber;
  @NonNull
  private final Money transactionAmount;
  @NonNull
  private final Money billingAmount;

  private final Money settlementAmount;
  @NonNull
  private final String merchantCategoryCode;
  @NonNull
  private final String accountQualifier;
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

  private final Money updatedBalance;
  @Wither
  private final Fee fee;
  @NonNull
  private final PaymentMethodType paymentMethodType;
  @NonNull
  private final Enum<CreditDebitEnum> messageType;
  @NonNull
  private final Merchant merchant;
  @NonNull
  private final String transactionLifeCycleID;


  public String getCurrencyCode() {
    return transactionAmount.getCurrency().getCurrencyCode();
  }

  public Currency getCurrency() {
    return transactionAmount.getCurrency();
  }

  public Cain003 addFee(Fee fee) {
    return this.toBuilder().fee(fee).build();
  }

  public Cain003 addTransactionIds(UUID transactionId, UUID correlationId) {
    return this.toBuilder().transactionId(transactionId).correlationId(correlationId).build();
  }

  public Optional<Fee> getFee() {
    return Optional.ofNullable(fee);
  }


}

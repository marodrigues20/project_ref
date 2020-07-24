package com.tenxbanking.cardrails.domain.model.subscription;

import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.TRANSACTION;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public final class TransactionLimit {

  @NonNull
  private final TransactionNameEnums transactionName;
  private final BigDecimal minimumAmount;
  private final BigDecimal maximumAmount;

  @NonNull
  private final ResetPeriodEnums resetPeriod;

  public Optional<BigDecimal> getMinimumAmount() {
    return ofNullable(minimumAmount);
  }

  public Optional<BigDecimal> getMaximumAmount() {
    return ofNullable(maximumAmount);
  }

  public ResetPeriodEnums getResetPeriod() {
    return isNull(resetPeriod) ? TRANSACTION : resetPeriod;
  }
}

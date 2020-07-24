package com.tenxbanking.cardrails.domain.validator.rule;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;

import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionLimitCockroachRepository;
import com.tenxbanking.cardrails.domain.exception.LimitConstraintException;
import com.tenxbanking.cardrails.domain.model.limits.TimeInterval;
import com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums;
import com.tenxbanking.cardrails.domain.service.TimeIntervalService;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
class AtmWithdrawalLimitsValidationRuleProcessor {

  private final TimeIntervalService timeIntervalService;
  private final TransactionLimitCockroachRepository transactionLimitCockroachRepository;

  Optional<ValidationFailure> verifyMaximumAmountForNonTransactionResetPeriod(
      @NonNull final BigDecimal transactionAmount,
      @NonNull final UUID subscriptionKey,
      @NonNull final ResetPeriodEnums resetPeriod,
      @NonNull final BigDecimal limitMaxAmount) {

    final TimeInterval timeInterval = timeIntervalService.getCheckPeriod(resetPeriod);

    final Optional<BigDecimal> amountSum = transactionLimitCockroachRepository
        .getCurrentAtmWithdrawal(subscriptionKey,
            timeInterval.getStartInstant(),
            timeInterval.getEndInstant());

    final BigDecimal totalAfterTransaction = amountSum.orElse(ZERO).add(transactionAmount);

    if (totalAfterTransaction.compareTo(limitMaxAmount) > 0) {

      final String msg = format(
          "Transaction with amount %s for subscription %s does not comply with maximum %s amount %s (total after transaction: %s)",
          transactionAmount,
          subscriptionKey,
          resetPeriod,
          limitMaxAmount,
          totalAfterTransaction);

      return Optional.of(ValidationFailure.of(msg));
    }
    return Optional.empty();
  }

  Optional<ValidationFailure> verifyMinimumAmountForTransactionResetPeriod(
      @NonNull final BigDecimal transactionAmount,
      @NonNull final UUID subscriptionKey,
      @NonNull final BigDecimal limitMinAmount) {

    if (transactionAmount.compareTo(limitMinAmount) < 0) {

      final String msg = format(
          "Transaction with amount %s for subscription %s does not comply with transaction minimum amount %s",
          transactionAmount,
          subscriptionKey,
          limitMinAmount);
      return Optional.of(ValidationFailure.of(msg));
    }
    return Optional.empty();
  }

  Optional<ValidationFailure> verifyMaximumAmountForTransactionResetPeriod(
      @NonNull final BigDecimal transactionAmount,
      @NonNull final UUID subscriptionKey,
      @NonNull final BigDecimal limitMinAmount) {

    if (transactionAmount.compareTo(limitMinAmount) > 0) {

      final String msg = format(
          "Transaction with amount %s for subscription %s does not comply with transaction maximum amount %s",
          transactionAmount,
          subscriptionKey,
          limitMinAmount);

      return Optional.of(ValidationFailure.of(msg));
    }
    return Optional.empty();
  }

}

package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.DAY;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.MONTH;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatCode;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionLimitCockroachRepository;
import com.tenxbanking.cardrails.domain.exception.LimitConstraintException;
import com.tenxbanking.cardrails.domain.model.limits.TimeInterval;
import com.tenxbanking.cardrails.domain.service.TimeIntervalService;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AtmWithdrawalLimitsValidationRuleProcessorTest {

  private static final UUID SUBSCRIPTION_KEY = UUID
      .fromString("0008622e-5017-4c3b-8afa-6fa6e9daa3c9");

  private static final Instant START_INSTANT = Instant.now().minus(Duration.ofDays(1));
  private static final Instant END_INSTANT = Instant.now().plus(Duration.ofDays(1));

  @Mock
  private TimeIntervalService timeIntervalService;

  @Mock
  private TransactionLimitCockroachRepository transactionLimitCockroachRepository;

  @InjectMocks
  private AtmWithdrawalLimitsValidationRuleProcessor unit;

  @Test
  void returnValidationFailureWhenMaximumDailyAmountIsNotMet() {

    final BigDecimal transactionAmount = new BigDecimal("10.01");
    final BigDecimal limitMaximumAmount = new BigDecimal("500");
    final BigDecimal dailyTotalAmount = new BigDecimal("490");
    final TimeInterval timeInterval = getTimeInterval();
    final String expectedMsg =
        "Transaction with amount 10.01 for subscription 0008622e-5017-4c3b-8afa-6fa6e9daa3c9 "
            + "does not comply with maximum DAY amount 500 (total after transaction: 500.01)";

    when(timeIntervalService.getCheckPeriod(DAY)).thenReturn(timeInterval);
    when(transactionLimitCockroachRepository
        .getCurrentAtmWithdrawal(SUBSCRIPTION_KEY, timeInterval.getStartInstant(),
            timeInterval.getEndInstant())).thenReturn(of(dailyTotalAmount));

    Optional<ValidationFailure> validationFailure = unit.verifyMaximumAmountForNonTransactionResetPeriod(transactionAmount, SUBSCRIPTION_KEY, DAY, limitMaximumAmount);

    assertThat(validationFailure).contains(ValidationFailure.of(expectedMsg));

  }

  @Test
  void returnsNoValidationFailureWhenMaximumDailyAmountIsMet() {

    final BigDecimal transactionAmount = new BigDecimal("10.01");
    final BigDecimal limitMaximumAmount = new BigDecimal("500");
    final BigDecimal dailyTotalAmount = new BigDecimal("489.99");
    final TimeInterval timeInterval = getTimeInterval();

    when(timeIntervalService.getCheckPeriod(DAY)).thenReturn(timeInterval);
    when(transactionLimitCockroachRepository
        .getCurrentAtmWithdrawal(SUBSCRIPTION_KEY, timeInterval.getStartInstant(),
            timeInterval.getEndInstant())).thenReturn(of(dailyTotalAmount));

    Optional<ValidationFailure> validationFailure = unit.verifyMaximumAmountForNonTransactionResetPeriod(transactionAmount, SUBSCRIPTION_KEY, DAY, limitMaximumAmount);

    assertThat(validationFailure).isEmpty();
  }

  @Test
  void returnValidationFailureWhenMaximumMonthlyAmountIsNotMet() {

    final BigDecimal transactionAmount = new BigDecimal("10.01");
    final BigDecimal limitMaximumAmount = new BigDecimal("500");
    final BigDecimal dailyTotalAmount = new BigDecimal("490");
    final TimeInterval timeInterval = getTimeInterval();
    final String expectedMsg =
        "Transaction with amount 10.01 for subscription 0008622e-5017-4c3b-8afa-6fa6e9daa3c9 "
            + "does not comply with maximum MONTH amount 500 (total after transaction: 500.01)";

    when(timeIntervalService.getCheckPeriod(MONTH)).thenReturn(timeInterval);
    when(transactionLimitCockroachRepository
        .getCurrentAtmWithdrawal(SUBSCRIPTION_KEY, timeInterval.getStartInstant(),
            timeInterval.getEndInstant())).thenReturn(of(dailyTotalAmount));

    Optional<ValidationFailure> validationFailure = unit.verifyMaximumAmountForNonTransactionResetPeriod(transactionAmount, SUBSCRIPTION_KEY, MONTH, limitMaximumAmount);

    assertThat(validationFailure).contains(ValidationFailure.of(expectedMsg));
  }

  @Test
  void returnsNoValidationFailureWhenMaximumMonthlyAmountIsMet() {

    final BigDecimal transactionAmount = new BigDecimal("10.01");
    final BigDecimal limitMaximumAmount = new BigDecimal("500");
    final BigDecimal dailyTotalAmount = new BigDecimal("489.99");
    final TimeInterval timeInterval = getTimeInterval();

    when(timeIntervalService.getCheckPeriod(MONTH)).thenReturn(timeInterval);
    when(transactionLimitCockroachRepository
        .getCurrentAtmWithdrawal(SUBSCRIPTION_KEY, timeInterval.getStartInstant(),
            timeInterval.getEndInstant())).thenReturn(of(dailyTotalAmount));

    Optional<ValidationFailure> validationFailure = unit.verifyMaximumAmountForNonTransactionResetPeriod(transactionAmount, SUBSCRIPTION_KEY, MONTH, limitMaximumAmount);

    assertThat(validationFailure).isEmpty();
  }

  @Test
  void returnValidationFailureWhenMaximumAmountForTransactionIsNotMet() {

    final BigDecimal transactionAmount = new BigDecimal("500.01");
    final BigDecimal limitMaxAmount = new BigDecimal("500");
    final String expectedMsg =
        "Transaction with amount 500.01 for subscription 0008622e-5017-4c3b-8afa-6fa6e9daa3c9 "
            + "does not comply with transaction maximum amount 500";

    Optional<ValidationFailure> validationFailure = unit.verifyMaximumAmountForTransactionResetPeriod(transactionAmount, SUBSCRIPTION_KEY, limitMaxAmount);

    assertThat(validationFailure).contains(ValidationFailure.of(expectedMsg));
  }

  @Test
  void returnsNoValidationFailureWhenMaximumAmountForTransactionIsMet() {

    final BigDecimal transactionAmount = new BigDecimal("500");
    final BigDecimal limitMaxAmount = new BigDecimal("500.01");

    Optional<ValidationFailure> validationFailure = unit.verifyMaximumAmountForTransactionResetPeriod(transactionAmount, SUBSCRIPTION_KEY, limitMaxAmount);

    assertThat(validationFailure).isEmpty();
  }

  @Test
  void returnValidationFailureWhenMinimumAmountForTransactionIsNotMet() {

    final BigDecimal transactionAmount = new BigDecimal("9.9999");
    final BigDecimal limitMinAmount = new BigDecimal("10.00");
    final String expectedMsg =
        "Transaction with amount 9.9999 for subscription 0008622e-5017-4c3b-8afa-6fa6e9daa3c9 "
            + "does not comply with transaction minimum amount 10.00";

    Optional<ValidationFailure> validationFailure = unit.verifyMinimumAmountForTransactionResetPeriod(transactionAmount, SUBSCRIPTION_KEY, limitMinAmount);

    assertThat(validationFailure).contains(ValidationFailure.of(expectedMsg));
  }

  @Test
  void returnsNoValidationFailureWhenMinimumAmountForTransactionIsMet() {

    final BigDecimal transactionAmount = new BigDecimal("10.00");
    final BigDecimal limitMinAmount = new BigDecimal("9.9999");

    Optional<ValidationFailure> validationFailure = unit.verifyMinimumAmountForTransactionResetPeriod(transactionAmount, SUBSCRIPTION_KEY, limitMinAmount);

    assertThat(validationFailure).isEmpty();
  }

  private TimeInterval getTimeInterval() {

    return TimeInterval.builder()
        .startInstant(START_INSTANT)
        .endInstant(END_INSTANT)
        .build();
  }
}
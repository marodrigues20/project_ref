package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD;
import static com.tenxbanking.cardrails.domain.TestConstant.GBP;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.DAY;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.MONTH;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.TRANSACTION;
import static com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums.ATMWITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums.TRANSFERIN;
import static com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums.TRANSFEROUT;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.ATM_WITHDRAWAL_LIMITS;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionLimit;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AtmWithdrawalLimitsValidationRuleTest {

  @Mock
  private AtmWithdrawalLimitsValidationRuleProcessor limitsCheckProcessor;
  @Mock
  private TimeService timeService;

  @InjectMocks
  private AtmWithdrawalLimitsValidationRule unit;

  @Mock
  private Subscription subscription;
  @Mock
  private CardTransaction cardTransaction;
  @Mock
  private CardSettings cardSettings;

  @Test
  void shouldNotCheckTransactionLimitsThatAreNotAtmWithdrawals() {
    when(timeService.now()).thenReturn(Instant.now());
    TransactionLimit transferIn = getTransactionLimit(DAY, TRANSFERIN, ONE, ZERO);
    TransactionLimit transferOut = getTransactionLimit(DAY, TRANSFEROUT, ONE, TEN);
    SubscriptionSettings settings = getSubscriptionSettings(transferIn, transferOut);
    when(subscription.getSubscriptionSettings(any())).thenReturn(settings);

    unit.validate(cardTransaction, CARD, subscription, cardSettings);

    verifyZeroInteractions(limitsCheckProcessor);
  }

  @Test
  void shouldCheckAtmWithdrawalMinimumLimitWhenItIsDefinedForTransactionResetPeriod() {
    when(timeService.now()).thenReturn(Instant.now());
    when(cardTransaction.getTransactionAmount()).thenReturn(Money.of(TEN, GBP));
    TransactionLimit atmWithdrawal = getTransactionLimit(TRANSACTION, ATMWITHDRAWAL, ZERO, TEN);
    SubscriptionSettings settings = getSubscriptionSettings(atmWithdrawal);
    when(subscription.getSubscriptionSettings(any())).thenReturn(settings);

    unit.validate(cardTransaction, CARD, subscription, cardSettings);

    verify(limitsCheckProcessor)
        .verifyMinimumAmountForTransactionResetPeriod(TEN, SUBSCRIPTION_KEY, ZERO);
  }

  @Test
  void shouldCheckAtmWithdrawalMaximumLimitWhenItIsDefinedForTransactionResetPeriod() {
    when(timeService.now()).thenReturn(Instant.now());
    when(cardTransaction.getTransactionAmount()).thenReturn(Money.of(TEN, GBP));
    TransactionLimit atmWithdrawal = getTransactionLimit(TRANSACTION, ATMWITHDRAWAL, ZERO, TEN);
    SubscriptionSettings settings = getSubscriptionSettings(atmWithdrawal);
    when(subscription.getSubscriptionSettings(any())).thenReturn(settings);

    unit.validate(cardTransaction, CARD, subscription, cardSettings);

    verify(limitsCheckProcessor)
        .verifyMinimumAmountForTransactionResetPeriod(TEN, SUBSCRIPTION_KEY, ZERO);

  }

  @Test
  void shouldCheckAtmWithdrawalMaximumLimitWhenItIsDefinedForNonTransactionResetPeriod() {
    when(timeService.now()).thenReturn(Instant.now());
    when(cardTransaction.getTransactionAmount()).thenReturn(Money.of(TEN, GBP));
    TransactionLimit atmWithdrawal = getTransactionLimit(MONTH, ATMWITHDRAWAL, ZERO, ONE);
    SubscriptionSettings settings = getSubscriptionSettings(atmWithdrawal);
    when(subscription.getSubscriptionSettings(any())).thenReturn(settings);

    unit.validate(cardTransaction, CARD, subscription, cardSettings);

    verify(limitsCheckProcessor)
        .verifyMaximumAmountForNonTransactionResetPeriod(TEN, SUBSCRIPTION_KEY, MONTH, ONE);
  }

  @Test
  void shouldCheckAtmWithdrawalMinimumLimitWhenItIsNotDefinedForTransactionResetPeriod() {
    when(timeService.now()).thenReturn(Instant.now());
    when(cardTransaction.getTransactionAmount()).thenReturn(Money.of(TEN, GBP));
    TransactionLimit atmWithdrawal = getTransactionLimit(TRANSACTION, ATMWITHDRAWAL, null, null);
    SubscriptionSettings settings = getSubscriptionSettings(atmWithdrawal);
    when(subscription.getSubscriptionSettings(any())).thenReturn(settings);

    unit.validate(cardTransaction, CARD, subscription, cardSettings);

    verifyZeroInteractions(limitsCheckProcessor);
  }

  @Test
  void shouldNotCheckAtmWithdrawalLimitsWhenThoseAreNotDefinedForTransactionResetPeriod() {
    when(timeService.now()).thenReturn(Instant.now());
    when(cardTransaction.getTransactionAmount()).thenReturn(Money.of(TEN, GBP));
    TransactionLimit atmWithdrawal = getTransactionLimit(TRANSACTION, ATMWITHDRAWAL, null, null);
    SubscriptionSettings settings = getSubscriptionSettings(atmWithdrawal);
    when(subscription.getSubscriptionSettings(any())).thenReturn(settings);

    unit.validate(cardTransaction, CARD, subscription, cardSettings);

    verifyZeroInteractions(limitsCheckProcessor);
  }

  @Test
  void shouldNotCheckAtmWithdrawalLimitsWhenThoseAreNotDefinedForNonTransactionResetPeriod() {
    when(timeService.now()).thenReturn(Instant.now());
    when(cardTransaction.getTransactionAmount()).thenReturn(Money.of(TEN, GBP));
    TransactionLimit atmWithdrawal = getTransactionLimit(MONTH, ATMWITHDRAWAL, null, null);
    SubscriptionSettings settings = getSubscriptionSettings(atmWithdrawal);
    when(subscription.getSubscriptionSettings(any())).thenReturn(settings);

    unit.validate(cardTransaction, CARD, subscription, cardSettings);

    verifyZeroInteractions(limitsCheckProcessor);
  }

  @Test
  void getRule() {
    assertThat(unit.getRule()).isEqualTo(ATM_WITHDRAWAL_LIMITS);
  }

  @Test
  void getApplicableTransactionTypes() {
    assertThat(unit.getApplicableTransactionTypes()).containsExactly(AUTH);
  }

  private SubscriptionSettings getSubscriptionSettings(TransactionLimit... transactionLimits) {

    return SubscriptionSettings.builder()
        .effectiveDate(Instant.now())
        .productKey(UUID.randomUUID())
        .productVersion(2)
        .transactionLimits(asList(transactionLimits))
        .build();
  }

  private TransactionLimit getTransactionLimit(ResetPeriodEnums resetPeriod,
      TransactionNameEnums transactionName, BigDecimal minValue, BigDecimal maxValue) {

    return TransactionLimit.builder()
        .resetPeriod(resetPeriod)
        .transactionName(transactionName)
        .minimumAmount(minValue)
        .maximumAmount(maxValue)
        .build();
  }
}
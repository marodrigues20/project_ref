package com.tenxbanking.cardrails.adapter.secondary.subscription;

import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_EFFECTIVE_DATE_STR;
import static com.tenxbanking.cardrails.domain.service.TimeService.ISO8601_DATETIME_FORMATTER;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.subscription.model.Limits;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.Product;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.TransactionLimits;
import com.tenxbanking.cardrails.domain.exception.UnmappableSubscriptionException;
import com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionLimit;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionMapperTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();
  private static final UUID PRODUCT_KEY = UUID.randomUUID();
  private static final String EFFECTIVE_DATE_STR = "2018-08-19T10:12:14.000+0000";
  private static final Instant EFFECTIVE_DATE = OffsetDateTime
      .parse(DEFAULT_EFFECTIVE_DATE_STR, ISO8601_DATETIME_FORMATTER).toInstant();

  @Mock
  private TimeService timeService;

  @InjectMocks
  private SubscriptionMapper unit;

  @BeforeEach
  void before() {
  }

  @Test
  void shouldMapEventToDomainModelWhenAllFieldsArePresent() {

    when(timeService.toInstant(EFFECTIVE_DATE_STR)).thenReturn(EFFECTIVE_DATE);
    final SubscriptionProductsResponse subscriptionProducts = getSubscriptionProductsResponse(
        false, false, false);
    final Subscription expected = getExpectedSubscription(false, false, false);

    final Subscription actual = unit.toSubscription(subscriptionProducts);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldMapEventToDomainModelWhenTransactionLimitFieldsAreNull() {

    when(timeService.toInstant(EFFECTIVE_DATE_STR)).thenReturn(EFFECTIVE_DATE);
    final SubscriptionProductsResponse subscriptionProducts = getSubscriptionProductsResponse(true,
        false, false);

    final Subscription expected = getExpectedSubscription(true, false, false);

    final Subscription actual = unit.toSubscription(subscriptionProducts);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldMapEventToDomainModelWhenLimitsIsNull() {

    when(timeService.toInstant(EFFECTIVE_DATE_STR)).thenReturn(EFFECTIVE_DATE);
    final SubscriptionProductsResponse subscriptionProducts = getSubscriptionProductsResponse(true,
        true, true);

    final Subscription expected = getExpectedSubscription(true, true, true);

    final Subscription actual = unit.toSubscription(subscriptionProducts);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldMapEventToDomainModelWhenTransactionLimitsIsNull() {

    when(timeService.toInstant(EFFECTIVE_DATE_STR)).thenReturn(EFFECTIVE_DATE);
    final SubscriptionProductsResponse subscriptionProducts = getSubscriptionProductsResponse(true,
        true, false);

    final Subscription expected = getExpectedSubscription(true, true, false);

    final Subscription actual = unit.toSubscription(subscriptionProducts);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldThrowExceptionWhenMappingToDomainObjectFails() {

    assertThrows(UnmappableSubscriptionException.class,
        () -> {
          final SubscriptionProductsResponse response = getSubscriptionProductsResponse(true, true,
              false)
              .toBuilder().subscriptionStatus(null).build();
          unit.toSubscription(response);
        });

  }

  private Subscription getExpectedSubscription(boolean nullAmounts,
      boolean nullTransactionLimits, boolean nullLimits) {

    final BigDecimal minAmount = nullAmounts ? null : new BigDecimal("0.01");
    final BigDecimal maxAmount = nullAmounts ? null : new BigDecimal("1000.123");

    final TransactionLimit transactionLimit = nullTransactionLimits
        ? null
        : TransactionLimit.builder()
            .resetPeriod(ResetPeriodEnums.MONTH)
            .transactionName(TransactionNameEnums.ATMWITHDRAWAL)
            .minimumAmount(minAmount)
            .maximumAmount(maxAmount)
            .build();

    final List<TransactionLimit> transactionLimits = (nullLimits || nullTransactionLimits)
        ? emptyList()
        : singletonList(transactionLimit);

    final SubscriptionSettings settings = SubscriptionSettings.builder()
        .effectiveDate(EFFECTIVE_DATE)
        .productVersion(1)
        .productKey(PRODUCT_KEY)
        .hasFees(true)
        .transactionLimits(transactionLimits)
        .build();

    return Subscription.builder()
        .subscriptionKey(SUBSCRIPTION_KEY)
        .status(SubscriptionStatus.ACTIVE)
        .settings(singletonList(settings))
        .build();
  }

  private SubscriptionProductsResponse getSubscriptionProductsResponse(boolean nullAmounts,
      boolean nullTransactionLimits, boolean nullLimits) {

    final String minAmount = nullAmounts ? null : "0.01";
    final String maxAmount = nullAmounts ? null : "1000.123";

    final TransactionLimits transactionLimits = nullTransactionLimits
        ? null
        : TransactionLimits.builder()
            .minimumAmount(minAmount)
            .maximumAmount(maxAmount)
            .transactionName("ATMwithdrawal")
            .resetPeriod("MONTH")
            .build();

    final Limits limits = nullLimits
        ? null
        : Limits.builder()
            .transactionLimits(singletonList(transactionLimits))
            .build();

    final Product product = Product.builder()
        .effectiveDate(EFFECTIVE_DATE_STR)
        .productKey(PRODUCT_KEY)
        .productVersion(1)
        .hasFees(true)
        .limits(limits)
        .build();

    return SubscriptionProductsResponse.builder()
        .subscriptionKey(SUBSCRIPTION_KEY)
        .subscriptionStatus("ACTIVE")
        .products(singletonList(product))
        .build();
  }

  @Test
  void shouldWorkWhenResetPeriodIsNotValidEnum() {

    when(timeService.toInstant(EFFECTIVE_DATE_STR)).thenReturn(EFFECTIVE_DATE);


    when(timeService.toInstant(EFFECTIVE_DATE_STR)).thenReturn(EFFECTIVE_DATE);
    SubscriptionProductsResponse subscriptionProducts = getSubscriptionProductsResponse(
        false, false, false);

    subscriptionProducts.getProducts().get(0).getLimits().getTransactionLimits().get(0)
        .setResetPeriod(null);

    Subscription subscription = unit.toSubscription(subscriptionProducts);

    assertThat(subscription.getSettings().get(0).getTransactionLimits()
        .get(0).getResetPeriod()).isEqualTo(ResetPeriodEnums.TRANSACTION);
  }
}
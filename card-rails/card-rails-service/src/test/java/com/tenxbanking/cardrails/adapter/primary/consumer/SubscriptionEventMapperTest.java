package com.tenxbanking.cardrails.adapter.primary.consumer;

import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_EFFECTIVE_DATE;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_EFFECTIVE_DATE_STR;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_PRODUCT_VERSION;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_STATUS;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.getSubscriptionEvent;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.DAY;
import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.ACTIVE;
import static com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums.TRANSFERIN;
import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.tenx.dub.subscription.event.v1.Limits;
import com.tenx.dub.subscription.event.v1.ProductDetails;
import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import com.tenx.dub.subscription.event.v1.TransactionLimits;
import com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionLimit;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionEventMapperTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();

  @Mock
  private TimeService timeService;

  @InjectMocks
  private SubscriptionEventMapper unit;

  @BeforeEach
  public void before() {
    when(timeService.toInstant(DEFAULT_EFFECTIVE_DATE_STR)).thenReturn(DEFAULT_EFFECTIVE_DATE);
  }

  @Test
  void shouldMapEventToDomainModel() {

    final SubscriptionEvent subscriptionEvent = getSubscriptionEvent(SUBSCRIPTION_KEY);
    final ProductDetails productDetails = subscriptionEvent.getProductDetails();
    final TransactionLimits expectedTransactionLimit = subscriptionEvent.getProductDetails()
        .getLimits().getTransactionLimits().get(0);

    final Subscription subscription = unit.toSubscription(subscriptionEvent);

    assertThat(subscription.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(subscription.getStatus()).isEqualTo(ACTIVE);

    final List<SubscriptionSettings> productList = subscription.getSettings();
    assertThat(productList).size().isOne();

    final SubscriptionSettings settings = productList.get(0);

    assertThat(settings.getEffectiveDate()).isEqualTo(DEFAULT_EFFECTIVE_DATE);
    assertThat(settings.getProductKey()).isEqualTo(fromString(productDetails.getProductKey()));
    assertThat(settings.getProductVersion()).isEqualTo(productDetails.getMajorVersion());

    assertThat(settings.getTransactionLimits()).size().isOne();
    final TransactionLimit actualTransactionLimit = settings.getTransactionLimits().get(0);

    assertThat(actualTransactionLimit.getMinimumAmount().get())
        .isEqualTo(new BigDecimal(expectedTransactionLimit.getMinimumAmount()));
    assertThat(actualTransactionLimit.getMaximumAmount().get())
        .isEqualTo(new BigDecimal(expectedTransactionLimit.getMaximumAmount()));
    assertThat(actualTransactionLimit.getTransactionName())
        .isEqualTo(TransactionNameEnums.fromString(expectedTransactionLimit.getTransactionName()));
    assertThat(actualTransactionLimit.getResetPeriod())
        .isEqualTo(ResetPeriodEnums.fromString(expectedTransactionLimit.getResetPeriod()));
  }

  @Test
  void shouldMapWhenTransactionLimitsAreNull() {

    final Limits overriddenLimits = Limits.newBuilder().build();
    final SubscriptionEvent subscriptionEvent = getSubscriptionEvent(SUBSCRIPTION_KEY,
        overriddenLimits, DEFAULT_EFFECTIVE_DATE_STR, DEFAULT_PRODUCT_VERSION, DEFAULT_STATUS);

    final Subscription subscription = unit.toSubscription(subscriptionEvent);

    assertThat(subscription.getSettings()).size().isOne();

    assertThat(subscription.getSettings().get(0).getTransactionLimits()).isEmpty();
  }

  @Test
  void shouldMapWhenTransactionLimitsAreEmpty() {

    final Limits overriddenLimits = Limits.newBuilder().setTransactionLimits(emptyList()).build();
    final SubscriptionEvent subscriptionEvent = getSubscriptionEvent(SUBSCRIPTION_KEY,
        overriddenLimits, DEFAULT_EFFECTIVE_DATE_STR, DEFAULT_PRODUCT_VERSION, DEFAULT_STATUS);

    final Subscription subscription = unit.toSubscription(subscriptionEvent);

    assertThat(subscription.getSettings().get(0).getTransactionLimits()).isEmpty();
  }

  @Test
  void shouldMapWhenLimitsAreNull() {

    final SubscriptionEvent subscriptionEvent = getSubscriptionEvent(SUBSCRIPTION_KEY, null,
        DEFAULT_EFFECTIVE_DATE_STR, DEFAULT_PRODUCT_VERSION, DEFAULT_STATUS);

    final Subscription subscription = unit.toSubscription(subscriptionEvent);

    assertThat(subscription.getSettings().get(0).getTransactionLimits()).isEmpty();
  }

  @Test
  void shouldMapWhenTransactionAmountsAreNull() {

    final TransactionLimits transactionLimits = TransactionLimits.newBuilder()
        .setTransactionName(TRANSFERIN.name())
        .setResetPeriod(DAY.name())
        .build();

    final Limits limits = Limits.newBuilder()
        .setTransactionLimits(ImmutableList.of(transactionLimits))
        .build();

    final SubscriptionEvent subscriptionEvent = getSubscriptionEvent(SUBSCRIPTION_KEY, limits,
        DEFAULT_EFFECTIVE_DATE_STR, DEFAULT_PRODUCT_VERSION, DEFAULT_STATUS);

    final Subscription subscription = unit.toSubscription(subscriptionEvent);

    final List<TransactionLimit> actualTransactionLimits = subscription.getSettings().get(0)
        .getTransactionLimits();
    assertThat(actualTransactionLimits).size().isOne();

    final TransactionLimit actualTransactionLimit = actualTransactionLimits.get(0);
    assertThat(actualTransactionLimit.getMaximumAmount()).isEmpty();
    assertThat(actualTransactionLimit.getMinimumAmount()).isEmpty();
    assertThat(actualTransactionLimit.getTransactionName()).isEqualTo(TRANSFERIN);
    assertThat(actualTransactionLimit.getResetPeriod()).isEqualTo(DAY);
  }
}
package com.tenxbanking.cardrails.domain.model.subscription;

import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.ACTIVE;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

import com.tenxbanking.cardrails.domain.exception.InvalidSubscriptionStateException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SubscriptionTest {

  private static final UUID SUBSCRIPTION_KEY = UUID
      .fromString("c6b43092-150d-41f2-a054-ea397103a3fd");
  private static final UUID PRODUCT_KEY = UUID.randomUUID();

  private static final Instant INSTANT_JAN_14_2018 = LocalDateTime.of(2018, 1, 14, 0, 0)
      .toInstant(UTC);
  private static final Instant INSTANT_16_AUG_2019 = LocalDateTime.of(2019, 8, 16, 0, 0)
      .toInstant(UTC);
  private static final Instant INSTANT_20_OCT_2018 = LocalDateTime.of(2018, 10, 20, 0, 0)
      .toInstant(UTC);

  @Test
  void shouldThrowExceptionWhenThereAreNoSettings() {

    final Instant instant13Jan2018 = LocalDateTime.of(2018, 1, 13, 0, 0).toInstant(UTC);

    final Subscription subscription = getSubscription();

    assertThatThrownBy(() -> subscription.getSubscriptionSettings(instant13Jan2018))
        .isInstanceOf(InvalidSubscriptionStateException.class)
        .hasMessage(
            "Subscription c6b43092-150d-41f2-a054-ea397103a3fd does not have settings for 2018-01-13T00:00:00Z");
  }

  @Test
  void shouldReturnSubscriptionSettingsForFebruary2018() {

    final Instant instant4Feb2018 = LocalDateTime.of(2018, 2, 4, 0, 0).toInstant(UTC);
    final SubscriptionSettings expected = getSubscriptionSettings(INSTANT_JAN_14_2018);

    final Subscription subscription = getSubscription();

    final SubscriptionSettings settings = subscription.getSubscriptionSettings(instant4Feb2018);

    assertThat(settings).isEqualTo(expected);
  }

  @Test
  void shouldReturnSubscriptionSettingsForDecember2018() {

    final Instant instant25Dec2018 = LocalDateTime.of(2018, 12, 25, 1, 12).toInstant(UTC);
    final SubscriptionSettings expected = getSubscriptionSettings(INSTANT_20_OCT_2018);

    final Subscription subscription = getSubscription();

    final SubscriptionSettings settings = subscription.getSubscriptionSettings(instant25Dec2018);

    assertThat(settings).isEqualTo(expected);
  }

  @Test
  void shouldReturnSubscriptionSettingsFor16Aug2019() {

    final SubscriptionSettings expected = getSubscriptionSettings(INSTANT_16_AUG_2019);

    final Subscription subscription = getSubscription();

    final SubscriptionSettings settings = subscription.getSubscriptionSettings(INSTANT_16_AUG_2019);

    assertThat(settings).isEqualTo(expected);
  }

  @Test
  void shouldReturnSubscriptionSettingsFor16Dec2019() {

    final Instant instant = LocalDateTime.of(2019, 12, 10, 0, 0).toInstant(UTC);
    final SubscriptionSettings expected = getSubscriptionSettings(INSTANT_16_AUG_2019);

    final Subscription subscription = getSubscription();

    final SubscriptionSettings settings = subscription.getSubscriptionSettings(instant);

    assertThat(settings).isEqualTo(expected);
  }

  private Subscription getSubscription() {

    List<SubscriptionSettings> settings = asList(getSubscriptionSettings(INSTANT_JAN_14_2018),
        getSubscriptionSettings(INSTANT_16_AUG_2019),
        getSubscriptionSettings(INSTANT_20_OCT_2018));

    return Subscription.builder()
        .subscriptionKey(SUBSCRIPTION_KEY)
        .status(ACTIVE)
        .settings(settings)
        .build();
  }

  private SubscriptionSettings getSubscriptionSettings(Instant effectiveDate) {

    return SubscriptionSettings.builder()
        .productKey(PRODUCT_KEY)
        .productVersion(1)
        .effectiveDate(effectiveDate)
        .transactionLimits(emptyList())
        .build();
  }
}
package com.tenxbanking.cardrails;

import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_EFFECTIVE_DATE;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_EFFECTIVE_DATE_STR;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_STATUS;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.getAtmWithdrawalTransactionLimits;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.getLimits;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.getSubscriptionEvent;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.getTransferInTransactionLimits;
import static com.tenxbanking.cardrails.data.SubscriptionDataFactory.getSubscriptionWithAtmWithdrawal;
import static com.tenxbanking.cardrails.data.SubscriptionDataFactory.getSubscriptionWithAtmWithdrawalAndTransferIn;
import static com.tenxbanking.cardrails.data.SubscriptionDataFactory.getSubscriptionWithTransferIn;
import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.CLOSED;
import static com.tenxbanking.cardrails.domain.service.TimeService.ISO8601_DATETIME_FORMATTER;
import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.FIVE_SECONDS;

import com.tenx.dub.subscription.event.v1.Limits;
import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import com.tenx.dub.subscription.event.v1.TransactionLimits;
import com.tenxbanking.cardrails.adapter.secondary.redis.SubscriptionRedisRepository;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SubscriptionEventConsumerTest extends BaseComponentTest {

  private static final String TOPIC = "subscription-event-v3";
  private static final Instant NEW_EFFECTIVE_DATE = DEFAULT_EFFECTIVE_DATE
      .plus(Duration.ofDays(365));
  private static final String NEW_EFFECTIVE_DATE_STR = NEW_EFFECTIVE_DATE.atOffset(UTC)
      .format(ISO8601_DATETIME_FORMATTER);

  @Autowired
  private SubscriptionRedisRepository subscriptionRedisRepository;

  @Before
  public void before() {
    subscriptionRedisRepository.deleteAll();
  }

  @Test
  public void shouldSaveANewSubscriptionWhenSubscriptionDoesNotExistInCacheYet() {

    final UUID subscriptionKey = randomUUID();
    final SubscriptionEvent subscriptionEvent = getSubscriptionEvent(subscriptionKey);
    final Subscription expectedSubscription = getSubscriptionWithAtmWithdrawal(subscriptionKey);

    KAFKA_SERVICE.sendMessageWithSchema(TOPIC, subscriptionKey.toString(), subscriptionEvent);

    assertOnSavedSubscription(expectedSubscription);
  }

  @Test
  public void shouldUpdateExistingSubscriptionWithSubscriptionSettingsForANewEffectiveDate() {

    //given
    final UUID subscriptionKey = randomUUID();

    //and an existing subscription in redis
    final Subscription existingSubscription = getSubscriptionWithAtmWithdrawal(subscriptionKey);
    subscriptionRedisRepository.save(existingSubscription);

    //and a new subscription event
    final int newProductVersion = 2;
    final TransactionLimits newTransactionLimits = getTransferInTransactionLimits();
    final Limits newLimits = getLimits(newTransactionLimits);
    final SubscriptionEvent newSubscriptionEvent = getSubscriptionEvent(subscriptionKey, newLimits,
        NEW_EFFECTIVE_DATE_STR, newProductVersion, "CLOSED");

    //when
    KAFKA_SERVICE.sendMessageWithSchema(TOPIC, subscriptionKey.toString(), newSubscriptionEvent);

    //then
    Subscription expectedSubscription = getSubscriptionWithAtmWithdrawalAndTransferIn(
        subscriptionKey, NEW_EFFECTIVE_DATE, newProductVersion, CLOSED);
    assertOnSavedSubscription(expectedSubscription);
  }

  @Test
  public void shouldUpdateExistingSubscriptionWithSubscriptionSettingsForTheSameEffectiveDate() {

    //given
    final UUID subscriptionKey = randomUUID();

    //and an existing subscription in redis
    final Subscription existingSubscription = getSubscriptionWithAtmWithdrawal(subscriptionKey);
    subscriptionRedisRepository.save(existingSubscription);

    //and a new subscription event with a new product version, but same effective date
    final TransactionLimits newTransactionLimits = getTransferInTransactionLimits();
    final Limits newLimits = getLimits(newTransactionLimits);
    final SubscriptionEvent newSubscriptionEvent = getSubscriptionEvent(subscriptionKey, newLimits,
        DEFAULT_EFFECTIVE_DATE_STR, 2, "CLOSED");

    //when
    KAFKA_SERVICE.sendMessageWithSchema(TOPIC, subscriptionKey.toString(), newSubscriptionEvent);

    //then
    Subscription expectedSubscription = getSubscriptionWithTransferIn(subscriptionKey,
        DEFAULT_EFFECTIVE_DATE, 2, CLOSED);
    assertOnSavedSubscription(expectedSubscription);
  }

  @Test
  public void shouldInvalidateSubscriptionOnCacheWhenSubscriptionEventIsNull() {

    //given
    final UUID subscriptionKey = randomUUID();

    //and an existing subscription in redis
    final Subscription existingSubscription = getSubscriptionWithAtmWithdrawal(subscriptionKey);
    subscriptionRedisRepository.save(existingSubscription);

    //when new subscription event with a null record value
    KAFKA_SERVICE.sendMessageWithSchema(TOPIC, subscriptionKey.toString(), null);

    //then
    await().untilAsserted(() -> assertThat(subscriptionRedisRepository.findAll()).isEmpty());
  }

  @Test
  public void shouldUpdateSubscriptionStatusOnCacheWhenSubscriptionStateChanges() {

    //given
    final UUID subscriptionKey = randomUUID();

    //and an existing subscription in redis
    final Subscription existingSubscription = getSubscriptionWithAtmWithdrawal(subscriptionKey);
    subscriptionRedisRepository.save(existingSubscription);

    // and a new subscription event modifying just the status of the subscription
    final TransactionLimits transactionLimits = getAtmWithdrawalTransactionLimits();
    final Limits limits = getLimits(transactionLimits);
    final SubscriptionEvent newSubscriptionEvent = getSubscriptionEvent(subscriptionKey, limits,
        DEFAULT_EFFECTIVE_DATE_STR, 2, "CLOSED");

    //when
    KAFKA_SERVICE.sendMessageWithSchema(TOPIC, subscriptionKey.toString(), newSubscriptionEvent);

    //then
    await().atMost(FIVE_SECONDS).untilAsserted(() -> {
      Subscription subscription = subscriptionRedisRepository.findById(subscriptionKey).get();
      assertThat(subscription.getStatus()).isEqualTo(CLOSED);
    });
  }

  @Test
  public void shouldInvalidateSubscriptionOnCacheWhenProcessingSubscriptionEventFails() {

    //given
    final UUID subscriptionKey = randomUUID();

    final Subscription subscription = getSubscriptionWithAtmWithdrawal(subscriptionKey);
    subscriptionRedisRepository.save(subscription);

    //and a new subscription event
    final int newProductVersion = 2;
    final String newEffectiveDate = "invalid";
    final TransactionLimits newTransactionLimits = getTransferInTransactionLimits();
    final Limits newLimits = getLimits(newTransactionLimits);
    final SubscriptionEvent newSubscriptionEvent = getSubscriptionEvent(subscriptionKey, newLimits,
        newEffectiveDate, newProductVersion, DEFAULT_STATUS);

    //when
    KAFKA_SERVICE.sendMessageWithSchema(TOPIC, subscriptionKey.toString(), newSubscriptionEvent);

    //then
    await().untilAsserted(() -> assertThat(subscriptionRedisRepository.findAll()).isEmpty());
  }

  @Test
  public void shouldTolerateBadDataOnTheTopic() {

    //given
    final UUID subscriptionKey = randomUUID();

    //and
    final SubscriptionEvent subscriptionEvent = getSubscriptionEvent(subscriptionKey);
    final Subscription expectedSubscription = getSubscriptionWithAtmWithdrawal(subscriptionKey);

    KAFKA_SERVICE.sendStringMessage(TOPIC, subscriptionKey.toString(), "BAD_DATA");
    KAFKA_SERVICE.sendStringMessage(TOPIC, subscriptionKey.toString(), null);
    KAFKA_SERVICE.sendMessageWithSchema(TOPIC, subscriptionKey.toString(), null);
    KAFKA_SERVICE.sendMessageWithSchema(TOPIC, subscriptionKey.toString(), subscriptionEvent);

    assertOnSavedSubscription(expectedSubscription);
  }

  private void assertOnSavedSubscription(Subscription expectedSubscription) {

    await().atMost(FIVE_SECONDS).untilAsserted(() -> {
      Optional<Subscription> savedSubscription = subscriptionRedisRepository
          .findById(expectedSubscription.getSubscriptionKey());
      assertThat(savedSubscription).isPresent();
      assertThat(savedSubscription.get()).isEqualTo(expectedSubscription);
    });
  }

}

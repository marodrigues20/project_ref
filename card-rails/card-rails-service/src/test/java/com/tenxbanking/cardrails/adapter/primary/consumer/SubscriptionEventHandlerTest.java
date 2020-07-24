package com.tenxbanking.cardrails.adapter.primary.consumer;

import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.ACTIVE;
import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.CLOSURE_PENDING;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionEventHandlerTest {

  private static final UUID SUBSCRIPTION_KEY = randomUUID();
  private static final UUID PRODUCT_KEY = randomUUID();
  private static final int PRODUCT_VERSION = 1;
  private static final Instant EFFECTIVE_DATE_1 = Instant.now().atOffset(UTC).toInstant();
  private static final Instant EFFECTIVE_DATE_2 = EFFECTIVE_DATE_1.plus(Duration.ofDays(30));

  @Mock
  private SubscriptionService subscriptionService;

  @Mock
  private SubscriptionEventMapper subscriptionEventMapper;

  @InjectMocks
  private SubscriptionEventHandler unit;

  @Test
  void shouldSaveNewSubscriptionWhenSubscriptionIsNotInStore() {

    //given
    SubscriptionEvent subscriptionEvent = getSubscriptionEvent();
    Subscription subscription = mock(Subscription.class);
    when(subscriptionEventMapper.toSubscription(subscriptionEvent)).thenReturn(subscription);

    //when
    unit.process(subscriptionEvent);

    //then
    verify(subscriptionService).save(subscription);
  }

  @Test
  void shouldAddSubscriptionSettingForASubscriptionSettingWithDifferentEffectiveDate() {

    //given an existing subscription with effectiveDate1
    Subscription existingSubscription = getSubscription(ACTIVE, EFFECTIVE_DATE_1);
    when(subscriptionService.findById(SUBSCRIPTION_KEY))
        .thenReturn(Optional.of(existingSubscription));

    //and a new subscription event that maps to effectiveDate2
    SubscriptionEvent subscriptionEvent = getSubscriptionEvent();
    Subscription newSubscription = getSubscription(CLOSURE_PENDING, EFFECTIVE_DATE_2);
    when(subscriptionEventMapper.toSubscription(subscriptionEvent)).thenReturn(newSubscription);

    //when
    unit.process(subscriptionEvent);

    //then
    Subscription expectedSubscription = getSubscription(CLOSURE_PENDING, EFFECTIVE_DATE_1,
        EFFECTIVE_DATE_2);

    //and
    verify(subscriptionService).save(expectedSubscription);
  }

  @Test
  void shouldReplaceSubscriptionSettingForASubscriptionSettingWithSameEffectiveDate() {

    //given an existing subscription with effectiveDate1
    Subscription existingSubscription = getSubscription(ACTIVE, EFFECTIVE_DATE_1);
    when(subscriptionService.findById(SUBSCRIPTION_KEY))
        .thenReturn(Optional.of(existingSubscription));

    //and a new subscription event that maps to effectiveDate2
    SubscriptionEvent subscriptionEvent = getSubscriptionEvent();
    Subscription newSubscription = getSubscription(CLOSURE_PENDING, EFFECTIVE_DATE_1);
    when(subscriptionEventMapper.toSubscription(subscriptionEvent)).thenReturn(newSubscription);

    //when
    unit.process(subscriptionEvent);

    //then
    Subscription expectedSubscription = getSubscription(CLOSURE_PENDING, EFFECTIVE_DATE_1);
    //and
    verify(subscriptionService).save(expectedSubscription);
  }

  @Test
  void shouldRemoveSubscriptionFromStoreWhenSubscriptionKeyIsUuid() {

    unit.invalidateCache(SUBSCRIPTION_KEY.toString());

    verify(subscriptionService).remove(SUBSCRIPTION_KEY);
  }

  @Test
  void shouldSwallowExceptionWhenSubscriptionKeyIsNotUuid() {

    unit.invalidateCache("plop");

    verifyNoMoreInteractions(subscriptionService);
  }

  private SubscriptionEvent getSubscriptionEvent() {
    SubscriptionEvent subscriptionEvent = mock(SubscriptionEvent.class);
    when(subscriptionEvent.getSubscriptionKey()).thenReturn(SUBSCRIPTION_KEY.toString());
    return subscriptionEvent;
  }

  private Subscription getSubscription(SubscriptionStatus status, Instant... effectiveDates) {

    List<SubscriptionSettings> settings = stream(effectiveDates)
        .map((effectiveDate) -> SubscriptionSettings.builder()
            .productKey(PRODUCT_KEY)
            .productVersion(PRODUCT_VERSION)
            .effectiveDate(effectiveDate)
            .transactionLimits(emptyList())
            .build())
        .collect(toList());

    return Subscription.builder()
        .subscriptionKey(SUBSCRIPTION_KEY)
        .status(status)
        .settings(settings)
        .build();
  }
}
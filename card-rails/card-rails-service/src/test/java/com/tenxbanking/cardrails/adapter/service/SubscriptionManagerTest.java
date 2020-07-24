package com.tenxbanking.cardrails.adapter.service;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.redis.SubscriptionRedisRepository;
import com.tenxbanking.cardrails.adapter.secondary.subscription.SubscriptionFallbackReader;
import com.tenxbanking.cardrails.adapter.secondary.subscription.SubscriptionManager;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionManagerTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();
  private static final Subscription SUBSCRIPTION = mock(Subscription.class);

  @Mock
  private SubscriptionRedisRepository subscriptionRedisRepository;

  @Mock
  private SubscriptionFallbackReader subscriptionFallbackReader;

  @InjectMocks
  private SubscriptionManager unit;

  @Test
  void shouldSaveSubscriptionInRedis() {

    unit.save(SUBSCRIPTION);

    verify(subscriptionRedisRepository).save(SUBSCRIPTION);
    verifyZeroInteractions(subscriptionFallbackReader);
  }

  @Test
  void shouldRemoveSubscriptionFromRedis() {

    unit.remove(SUBSCRIPTION_KEY);

    verify(subscriptionRedisRepository).deleteById(SUBSCRIPTION_KEY);
    verifyZeroInteractions(subscriptionFallbackReader);
  }

  @Test
  void shouldReturnSubscriptionFromRedis() {

    when(subscriptionRedisRepository.findById(SUBSCRIPTION_KEY))
        .thenReturn(of(SUBSCRIPTION));

    Optional<Subscription> actualSubscription = unit
        .findById(SUBSCRIPTION_KEY);

    assertThat(actualSubscription).isPresent();
    assertThat(actualSubscription.get()).isEqualTo(SUBSCRIPTION);
    verifyZeroInteractions(subscriptionFallbackReader);
  }

  @Test
  void shouldReturnSubscriptionFromSubscriptionManagerAndSaveItInRedis() {

    when(subscriptionRedisRepository.findById(SUBSCRIPTION_KEY))
        .thenReturn(empty());
    when(subscriptionFallbackReader.getSubscription(SUBSCRIPTION_KEY)).thenReturn(of(SUBSCRIPTION));

    Optional<Subscription> actualSubscription = unit.findById(SUBSCRIPTION_KEY);

    assertThat(actualSubscription).isPresent();
    assertThat(actualSubscription.get()).isEqualTo(SUBSCRIPTION);

    verify(subscriptionRedisRepository, timeout(250)).save(SUBSCRIPTION);
  }

  @Test
  void shouldReturnSubscriptionFromSubscriptionManagerWhenCallToRedisFails() {

    doThrow(RuntimeException.class).when(subscriptionRedisRepository).findById(SUBSCRIPTION_KEY);
    when(subscriptionFallbackReader.getSubscription(SUBSCRIPTION_KEY)).thenReturn(of(SUBSCRIPTION));

    Optional<Subscription> actualSubscription = unit.findById(SUBSCRIPTION_KEY);

    assertThat(actualSubscription).isPresent();
    assertThat(actualSubscription.get()).isEqualTo(SUBSCRIPTION);

    verify(subscriptionRedisRepository, timeout(250)).save(SUBSCRIPTION);
  }

  @Test
  void shouldReturnEmptyOptionalWhenSubscriptionIsNotInRedisNeitherOnSubscriptionManager() {

    Optional<Subscription> actualSubscription = unit.findById(SUBSCRIPTION_KEY);

    assertThat(actualSubscription).isEmpty();
  }
}
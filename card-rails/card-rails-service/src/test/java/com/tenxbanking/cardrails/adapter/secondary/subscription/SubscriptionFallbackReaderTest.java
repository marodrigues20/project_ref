package com.tenxbanking.cardrails.adapter.secondary.subscription;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SubscriptionFallbackReaderTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();

  @Mock
  private SubscriptionMapper subscriptionMapper;

  @Mock
  private SubscriptionManagerClient subscriptionManagerClient;

  @InjectMocks
  private SubscriptionFallbackReader unit;

  @Test
  void shouldReturnDomainModelWhenCallToSubscriptionManagerAndMappingSucceeds() {

    final Subscription subscription = mock(Subscription.class);
    final SubscriptionProductsResponse response = mock(SubscriptionProductsResponse.class);
    when(subscriptionManagerClient.getSubscriptionProducts(SUBSCRIPTION_KEY)).thenReturn(
        ResponseEntity.ok(response));
    when(subscriptionMapper.toSubscription(response)).thenReturn(subscription);

    Optional<Subscription> actual = unit.getSubscription(SUBSCRIPTION_KEY);

    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(subscription);
  }

  @Test
  void shouldReturnEmptyOptionalWhenCallToSubscriptionManagerFails() {

    doThrow(RuntimeException.class).when(subscriptionManagerClient)
        .getSubscriptionProducts(SUBSCRIPTION_KEY);

    Optional<Subscription> actual = unit.getSubscription(SUBSCRIPTION_KEY);

    assertThat(actual.isPresent()).isFalse();
  }

  @Test
  void shouldReturnEmptyOptionalWhenMappingToDomainModelFails() {

    final SubscriptionProductsResponse response = mock(SubscriptionProductsResponse.class);
    when(subscriptionManagerClient.getSubscriptionProducts(SUBSCRIPTION_KEY)).thenReturn(
        ResponseEntity.ok(response));
    doThrow(RuntimeException.class).when(subscriptionMapper).toSubscription(response);

    Optional<Subscription> actual = unit.getSubscription(SUBSCRIPTION_KEY);

    assertThat(actual.isPresent()).isFalse();
  }
}
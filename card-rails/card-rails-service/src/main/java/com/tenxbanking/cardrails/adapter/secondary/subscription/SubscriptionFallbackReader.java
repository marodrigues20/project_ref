package com.tenxbanking.cardrails.adapter.secondary.subscription;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SubscriptionFallbackReader {

  private final SubscriptionMapper subscriptionMapper;
  private final SubscriptionManagerClient subscriptionManagerClient;

  public Optional<Subscription> getSubscription(@NonNull final UUID subscriptionKey) {

    try {
      final ResponseEntity<SubscriptionProductsResponse> response = subscriptionManagerClient
          .getSubscriptionProducts(subscriptionKey);
      return toSubscription(response.getBody());
    } catch (Exception e) {
      log.error("Could not get subscription {} from subscription manager", subscriptionKey, e);
      return empty();
    }
  }

  private Optional<Subscription> toSubscription(
      @NonNull final SubscriptionProductsResponse response) {

    return of(subscriptionMapper.toSubscription(response));
  }
}


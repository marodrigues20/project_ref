package com.tenxbanking.cardrails.adapter.secondary.subscription;

import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.runAsync;

import com.tenxbanking.cardrails.adapter.secondary.redis.SubscriptionRedisRepository;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SubscriptionManager implements SubscriptionService {

  private final SubscriptionRedisRepository subscriptionRedisRepository;
  private final SubscriptionFallbackReader subscriptionFallbackReader;

  @Override
  public Optional<Subscription> findById(@NonNull final UUID subscriptionKey) {
    return getSubscriptionFromRedis(subscriptionKey)
        .or(() -> fetchAndSaveSubscriptionFromSubscriptionManager(subscriptionKey));
  }

  @Override
  public void remove(@NonNull final UUID subscriptionKey) {
    subscriptionRedisRepository.deleteById(subscriptionKey);
  }

  @Override
  public void save(@NonNull final Subscription subscription) {
    try {
      subscriptionRedisRepository.save(subscription);
      log.info("Saved subscription {} to redis", subscription.getSubscriptionKey());
    } catch (Exception e) {
      log.error("Exception saving subscription {} to redis", subscription.getSubscriptionKey(), e);
    }
  }

  private Optional<Subscription> getSubscriptionFromRedis(@NonNull final UUID subscriptionKey) {
    try {
      return subscriptionRedisRepository.findById(subscriptionKey);
    } catch (Exception e) {
      log.error("Exception getting subscription {} from redis", subscriptionKey, e);
      return empty();
    }
  }

  private Optional<Subscription> fetchAndSaveSubscriptionFromSubscriptionManager(
      @NonNull final UUID subscriptionKey) {

    Optional<Subscription> subscriptionOptional = subscriptionFallbackReader
        .getSubscription(subscriptionKey);
    subscriptionOptional.ifPresent(this::saveAsyncIntoRedis);
    return subscriptionOptional;
  }

  private void saveAsyncIntoRedis(@NonNull final Subscription subscription) {
    runAsync(() -> save(subscription));
  }
}

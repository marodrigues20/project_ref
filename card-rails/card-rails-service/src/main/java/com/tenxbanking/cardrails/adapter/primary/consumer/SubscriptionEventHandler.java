package com.tenxbanking.cardrails.adapter.primary.consumer;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;

import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
class SubscriptionEventHandler {

  private final SubscriptionService subscriptionService;
  private final SubscriptionEventMapper subscriptionEventMapper;

  void process(@NonNull final SubscriptionEvent subscriptionEvent) {

    final UUID subscriptionKey = fromString(subscriptionEvent.getSubscriptionKey());

    final Subscription newSubscription = subscriptionEventMapper.toSubscription(subscriptionEvent);
    final Optional<Subscription> subscriptionOptional = subscriptionService
        .findById(subscriptionKey);

    subscriptionOptional
        .ifPresentOrElse(
            (existingSubscription) -> updateSubscription(existingSubscription, newSubscription),
            () -> saveNewSubscription(newSubscription));
  }

  public void invalidateCache(final String subscriptionKeyStr) {

    final Optional<UUID> subscriptionKeyOptional = toUuid(subscriptionKeyStr);

    subscriptionKeyOptional.ifPresentOrElse(this::removeSubscriptionFromStore, () ->
        log.error("Could not invalidate cache because subscription key {} is not an UUID",
            subscriptionKeyStr));
  }

  private void updateSubscription(@NonNull final Subscription existingSubscription,
      @NonNull final Subscription newSubscription) {

    final Map<Instant, SubscriptionSettings> settings = new TreeMap<>();
    populateMap(settings, existingSubscription.getSettings());
    populateMap(settings, newSubscription.getSettings());

    final Subscription subscription = existingSubscription.toBuilder()
        .subscriptionKey(newSubscription.getSubscriptionKey())
        .status(newSubscription.getStatus())
        .settings(new ArrayList<>(settings.values()))
        .build();

    log.info("Updated a subscription {} to {}", subscription.getSubscriptionKey(), subscription);
    subscriptionService.save(subscription);
  }

  private void saveNewSubscription(@NonNull final Subscription subscription) {
    subscriptionService.save(subscription);
    log.debug("Saved subscription {} in the near cache", subscription.getSubscriptionKey());
  }

  private Optional<UUID> toUuid(final String value) {
    try {
      return of(fromString(value));
    } catch (Exception e) {
      return empty();
    }
  }

  private void removeSubscriptionFromStore(@NonNull final UUID subscriptionKey) {
    try {
      subscriptionService.remove(subscriptionKey);
      log.info("Invalidate cache for subscription key {}", subscriptionKey);
    } catch (Exception e) {
      log.error("Could not invalidate cache for subscription key {}", subscriptionKey, e);
    }
  }

  private void populateMap(@NonNull final Map<Instant, SubscriptionSettings> map,
      @NonNull final List<SubscriptionSettings> settings) {

    settings.forEach(setting -> map.put(setting.getEffectiveDate(), setting));
  }
}

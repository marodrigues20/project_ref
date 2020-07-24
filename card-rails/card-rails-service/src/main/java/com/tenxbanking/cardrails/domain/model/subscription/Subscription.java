package com.tenxbanking.cardrails.domain.model.subscription;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.tenxbanking.cardrails.domain.exception.InvalidSubscriptionStateException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@RedisHash("Subscription")
public class Subscription {

  @Id
  @NonNull
  private final UUID subscriptionKey;
  @NonNull
  private final SubscriptionStatus status;
  @NonNull
  private final List<SubscriptionSettings> settings;

  public SubscriptionSettings getSubscriptionSettings(@NonNull final Instant instant) {

    return settings.stream()
        .sorted(Comparator.comparing(SubscriptionSettings::getEffectiveDate).reversed())
        .filter(subscriptionSettings -> (subscriptionSettings.getEffectiveDate().isBefore(instant)
            || subscriptionSettings.getEffectiveDate().equals(instant)))
            .findFirst()
            .orElseThrow(() -> invalidSubscriptionStateException(instant));
  }

  private InvalidSubscriptionStateException invalidSubscriptionStateException(
      @NonNull final Instant instant) {
    final String msg = String.format("Subscription %s does not have settings for %s",
        subscriptionKey, ISO_INSTANT.format(instant));
    return new InvalidSubscriptionStateException(msg);
  }
}

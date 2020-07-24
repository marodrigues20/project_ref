package com.tenxbanking.cardrails.domain.port;

import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;

public interface SubscriptionService {

  Optional<Subscription> findById(@NonNull final UUID subscriptionKey);

  void remove(@NonNull final UUID subscriptionKey);

  void save(@NonNull final Subscription subscription);
}

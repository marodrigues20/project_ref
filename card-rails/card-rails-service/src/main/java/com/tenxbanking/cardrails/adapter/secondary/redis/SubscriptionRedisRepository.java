package com.tenxbanking.cardrails.adapter.secondary.redis;

import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRedisRepository extends CrudRepository<Subscription, UUID> {

}

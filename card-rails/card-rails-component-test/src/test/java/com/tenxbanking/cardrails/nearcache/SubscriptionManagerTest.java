package com.tenxbanking.cardrails.nearcache;

import static com.tenxbanking.cardrails.data.SubscriptionDataFactory.getSubscriptionWithAtmWithdrawal;
import static com.tenxbanking.cardrails.data.SubscriptionManagerDataFactory.getSubscriptionProductsResponse;
import static com.tenxbanking.cardrails.stub.SubscriptionManagerWiremockStubs.stubGetSubscriptionProducts;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_SECOND;

import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.redis.SubscriptionRedisRepository;
import com.tenxbanking.cardrails.adapter.secondary.subscription.SubscriptionManager;
import com.tenxbanking.cardrails.adapter.secondary.subscription.SubscriptionManagerClient;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.data.SubscriptionDataFactory;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SubscriptionManagerTest extends BaseComponentTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();

  @Autowired
  private SubscriptionRedisRepository subscriptionRedisRepository;

  @Autowired
  private SubscriptionManagerClient subscriptionManagerClient;

  @Autowired
  private SubscriptionManager unit;

  @Before
  public void before() {
    subscriptionRedisRepository.deleteAll();
  }

  @Test
  public void shouldGetSubscriptionFromRedis() {

    Subscription subscription = getSubscriptionWithAtmWithdrawal(SUBSCRIPTION_KEY);
    subscriptionRedisRepository.save(subscription);

    Optional<Subscription> actual = unit.findById(SUBSCRIPTION_KEY);

    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(subscription);
  }

  @Test
  public void shouldGetSubscriptionFromSubscriptionManagerWhenSubscriptionNotInRedisAndCacheItAfter()
      throws Exception {

    SubscriptionProductsResponse response = getSubscriptionProductsResponse(SUBSCRIPTION_KEY);
    stubGetSubscriptionProducts(response);

    Subscription expected = SubscriptionDataFactory
        .getSubscriptionWithAtmWithdrawal(SUBSCRIPTION_KEY);

    Optional<Subscription> actual = unit.findById(SUBSCRIPTION_KEY);

    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(expected);

    await().atMost(ONE_SECOND).untilAsserted(() -> {
      Optional<Subscription> savedSubscription = subscriptionRedisRepository
          .findById(SUBSCRIPTION_KEY);
      assertThat(savedSubscription.isPresent()).isTrue();
      assertThat(savedSubscription.get()).isEqualTo(expected);
    });
  }
}

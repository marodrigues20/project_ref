package com.tenxbanking.cardrails.client;

import static com.tenxbanking.cardrails.stub.SubscriptionManagerWiremockStubs.stubGetSubscriptionProducts;
import static org.assertj.core.api.Java6Assertions.assertThat;

import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.subscription.SubscriptionManagerClient;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.data.SubscriptionManagerDataFactory;
import java.util.UUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SubscriptionFallbackReaderClientTest extends BaseComponentTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();

  @Autowired
  private SubscriptionManagerClient unit;

  @Test
  public void testSubscriptionManagerClient() throws Exception {

    SubscriptionProductsResponse response = SubscriptionManagerDataFactory
        .getSubscriptionProductsResponse(SUBSCRIPTION_KEY);

    stubGetSubscriptionProducts(response);

    ResponseEntity<SubscriptionProductsResponse> actual = unit
        .getSubscriptionProducts(SUBSCRIPTION_KEY);

    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(actual.getBody()).isEqualTo(response);
  }
}

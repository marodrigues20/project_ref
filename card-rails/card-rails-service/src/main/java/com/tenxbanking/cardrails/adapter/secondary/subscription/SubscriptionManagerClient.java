package com.tenxbanking.cardrails.adapter.secondary.subscription;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "subscriptionManagerClient", url = "${downstream.subscriptionmanager2.host}")
public interface SubscriptionManagerClient {

  @GetMapping(value = "/subscription-manager/v1/subscriptions/{subscriptionKey}/products",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<SubscriptionProductsResponse> getSubscriptionProducts(
      @PathVariable("subscriptionKey") @NonNull final UUID subscriptionKey);
}

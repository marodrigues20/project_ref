package com.tenxbanking.cardrails.adapter.secondary.subscription.model;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SubscriptionProductsResponse {

  private UUID subscriptionKey;
  private String subscriptionStatus;
  private List<Product> products;
}

package com.tenx.universalbanking.transactionmanager.model;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Card implements Serializable {

  @NonNull
  private final String id;
  @NonNull
  private final SubscriptionStatus subscriptionStatus;
  @NonNull
  private final UUID partyKey;
  @NonNull
  private final String tenantKey;
  @NonNull
  private final UUID subscriptionKey;
  private final String cardCountryCode;
  private final String cardCurrencyCode;

  private String processorAccountId;
  @NonNull
  private UUID productKey;

}

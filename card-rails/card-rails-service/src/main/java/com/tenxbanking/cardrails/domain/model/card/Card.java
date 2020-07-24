package com.tenxbanking.cardrails.domain.model.card;

import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@RedisHash("Card")
public class Card implements Serializable {

  @Id
  @NonNull
  private final String panHash;
  @NonNull
  private final Instant cardEffectiveDate;
  @NonNull
  private final Instant cardExpiryDate;
  @NonNull
  private final SubscriptionStatus subscriptionStatus;
  @NonNull
  private final CardStatus cardStatus;

  //TODO#1: do we need these in our domain? shall we validate these details against subscription details???
  private final UUID partyKey;
  private final String tenantKey;
  private final UUID subscriptionKey;

  //TODO#2: do we need these in our domain?
  private final String cardCountryCode;
  private final String cardCurrencyCode;

  //  TODO#3: check if we need these extra fields in card-rails...
  //  private String cardHolderName;
  //  private String cardProcessorName;
  //  private String panToken;
  private String processorAccountId;
  //  private String processorAppId;
  //  private String processorCustomerId;
  //  private String productKey;
  //  private String serviceCode;
  //  private String tokenExpiryDate;
  //  private String tokenProviderId;
  //  private String tokenProviderName;
}

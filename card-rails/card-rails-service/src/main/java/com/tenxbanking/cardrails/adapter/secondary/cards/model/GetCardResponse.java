package com.tenxbanking.cardrails.adapter.secondary.cards.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetCardResponse {

  private String cardCountryCode;
  private String cardCurrencyCode;
  private String cardEffectiveDate;
  private String cardExpiryDate;
  private String cardHolderName;
  private String cardProcessorName;
  private String cardStatus;
  private String cardSequenceNumber;
  private String panHash;
  private String panToken;
  private String partyKey;
  private String processorAccountId;
  private String processorAppId;
  private String processorCustomerId;
  private String productKey;
  private String serviceCode;
  private String subscriptionKey;
  private String subscriptionStatus;
  private String tenantKey;
  private String tokenExpiryDate;
  private String tokenProviderId;
  private String tokenProviderName;
}

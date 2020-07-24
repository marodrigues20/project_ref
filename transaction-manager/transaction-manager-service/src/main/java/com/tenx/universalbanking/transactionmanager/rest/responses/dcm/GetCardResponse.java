package com.tenx.universalbanking.transactionmanager.rest.responses.dcm;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetCardResponse {

  private String panToken;
  private String partyKey;
  private String subscriptionKey;
  private String panHash;
  private String processorAccountId;
  private String processorCustomerId;
  private LocalDate cardExpiryDate;
  private String tokenProviderId;
  private String tokenProviderName;
  private LocalDate tokenExpiryDate;
  private String cardCountryCode;
  private String cardCurrencyCode;
  private String cardProcessorName;
  private String cardHolderName;
  private LocalDate cardEffectiveDate;
  private String cardStatus;
  private String cardSequenceNumber;
  private String cardId;
  private String declineStatus;
  private String declineReason;
  private LocalDate cardOrderDate;
  private LocalDate cardActivationDate;
  private String serviceCode;
  private String processorAppId;
  private String productKey;
  private String subscriptionStatus;
  private String tenantKey;
  private String subscriptionAccountNumber;
  private String subscriptionSortCode;

}

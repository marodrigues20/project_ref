package com.tenx.universalbanking.transactionmanager.service.turbine;

import static java.lang.String.format;

import com.tenx.universalbanking.transactionmanager.exception.UnmappableCardException;
import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.model.SubscriptionStatus;
import com.tenx.universalbanking.transactionmanager.rest.responses.dcm.GetCardResponse;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
class DebitCardMapper {

  Card toCard(@NonNull final GetCardResponse response) {
    try {
      return Card.builder()
          .id(response.getCardId())
          .cardCountryCode(response.getCardCountryCode())
          .cardCurrencyCode(response.getCardCurrencyCode())
          .partyKey(UUID.fromString(response.getPartyKey()))
          .tenantKey(response.getTenantKey())
          .subscriptionKey(UUID.fromString(response.getSubscriptionKey()))
          .subscriptionStatus(SubscriptionStatus.fromString(response.getSubscriptionStatus()))
          .productKey(UUID.fromString(response.getProductKey()))
          .processorAccountId(response.getProcessorAccountId())
          .build();
    } catch (Exception e) {
      final String msg = format("Could not map debit card manager response for card %s",
          response.getPanHash());
      throw new UnmappableCardException(msg);
    }
  }

}

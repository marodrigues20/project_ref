package com.tenxbanking.cardrails.adapter.secondary.cards;

import static java.lang.String.format;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.domain.exception.UnmappableCardException;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
class DebitCardMapper {

  private final TimeService timeService;

  Card toCard(@NonNull final GetCardResponse response) {

    try {
      return doMap(response);
    } catch (Exception e) {
      log.info("Exception mapping card, response={}", response, e);
      final String msg = format("Could not map debit card manager response for card %s", response.getPanHash());
      throw new UnmappableCardException(msg, e);
    }
  }

  //TODO: check what fields are mandatory and optional:
  // - add optional for optional fields
  private Card doMap(@NonNull final GetCardResponse response) {

    return Card.builder()
        .panHash(response.getPanHash())
        .cardCountryCode(response.getCardCountryCode())
        .cardCurrencyCode(response.getCardCurrencyCode())
        .cardEffectiveDate(timeService.dateToInstant(response.getCardEffectiveDate()))
        .cardExpiryDate(timeService.dateToInstant(response.getCardExpiryDate()))
        .partyKey(UUID.fromString(response.getPartyKey()))
        .tenantKey(response.getTenantKey())
        .subscriptionKey(UUID.fromString(response.getSubscriptionKey()))
        .subscriptionStatus(SubscriptionStatus.fromString(response.getSubscriptionStatus()))
        .cardStatus(CardStatus.valueOf(response.getCardStatus()))
        .build();
  }
}

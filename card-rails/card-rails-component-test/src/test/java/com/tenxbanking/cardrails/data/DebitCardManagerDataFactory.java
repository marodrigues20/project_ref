package com.tenxbanking.cardrails.data;

import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class DebitCardManagerDataFactory {

  public static final String CARD_COUNTRY_CODE = "UK";
  public static final String CARD_CURRENCY_CODE = "GBP";
  public static final String SUBSCRIPTION_STATUS = "ACTIVE";

  private static final String CARD_EFFECTIVE_DATE_STR = "2018-08-19";
  private static final String CARD_EXPIRY_DATE_STR = "2022-08-19";
  public static final Instant CARD_EFFECTIVE_DATE = toInstant(CARD_EFFECTIVE_DATE_STR);
  public static final Instant CARD_EXPIRY_DATE = toInstant(CARD_EXPIRY_DATE_STR);

  private static Instant toInstant(String cardEffectiveDateStr) {
    return LocalDate.parse(cardEffectiveDateStr, DateTimeFormatter.ISO_DATE).atStartOfDay(ZoneId.of("Z")).toInstant();
  }

  public static GetCardRequest getCardRequest(String panHash) {
    return GetCardRequest.builder().panHash(panHash).build();
  }

  public static GetCardResponse getCardResponse(UUID subscriptionKey, String panHash) {

    GetCardResponse response = new GetCardResponse();
    response.setPanHash(panHash);
    response.setCardCountryCode(CARD_COUNTRY_CODE);
    response.setCardCurrencyCode(CARD_CURRENCY_CODE);
    response.setCardEffectiveDate(CARD_EFFECTIVE_DATE_STR);
    response.setCardExpiryDate(CARD_EXPIRY_DATE_STR);
    response.setPartyKey(PARTY_KEY.toString());
    response.setTenantKey(TENANT_KEY);
    response.setSubscriptionKey(subscriptionKey.toString());
    response.setSubscriptionStatus(SUBSCRIPTION_STATUS);
    response.setCardStatus(CardStatus.ACTIVE.name());
    return response;
  }
}

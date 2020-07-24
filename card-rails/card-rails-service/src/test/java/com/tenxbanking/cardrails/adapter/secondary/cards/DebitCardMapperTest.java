package com.tenxbanking.cardrails.adapter.secondary.cards;

import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.ACTIVE;
import static com.tenxbanking.cardrails.domain.service.TimeService.ISO8601_DATETIME_FORMATTER;
import static java.time.ZoneOffset.UTC;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.domain.exception.UnmappableCardException;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.service.TimeService;
import com.tenxbanking.cardrails.util.FileUtils;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebitCardMapperTest {

  private static final String PAN_HASH = "5c9001eeea39ff0d004c8fef4e2979eb666323a8eb4dce75a6";
  private static final UUID SUBSCRIPTION_KEY = fromString("c6bc5c48-726d-450e-b50e-9293c9e9f077");
  private static final Instant CARD_EFFECTIVE_DATE = Instant.now();
  private static final String CARD_EFFECTIVE_DATE_STR = CARD_EFFECTIVE_DATE.atOffset(UTC).toLocalDate()
      .format(DateTimeFormatter.ISO_DATE);
  private static final Instant CARD_EXPIRY_DATE = CARD_EFFECTIVE_DATE.plus(Duration.ofDays(365));
  private static final String CARD_EXPIRY_DATE_STR = CARD_EXPIRY_DATE.atOffset(UTC).toLocalDate()
      .format(DateTimeFormatter.ISO_DATE);

  @Mock
  private TimeService timeService;

  @InjectMocks
  private DebitCardMapper unit;

  @Test
  void toCardMapsAGetCardResponseToACardDomainObject() {

    GetCardResponse response = getCardResponse();
    Card expectedCard = getCard();

    when(timeService.dateToInstant(CARD_EFFECTIVE_DATE_STR)).thenReturn(CARD_EFFECTIVE_DATE);
    when(timeService.dateToInstant(CARD_EXPIRY_DATE_STR)).thenReturn(CARD_EXPIRY_DATE);

    Card card = unit.toCard(response);

    assertThat(card).isEqualTo(expectedCard);
  }

  @Test
  void shouldThrowExceptionWhenMappingToDomainObjectFails() {

    assertThrows(
        UnmappableCardException.class,
        () -> {
          GetCardResponse response = getCardResponse();
          response.setSubscriptionKey(null);
          unit.toCard(response);
        });

  }

  @Test
  void transformsJson() throws IOException {

    DebitCardMapper underTest = new DebitCardMapper(new TimeService(Clock.systemUTC()));
    String json = FileUtils.readFile("json/card-response.json");

    GetCardResponse getCardResponse = new ObjectMapper()
        .readValue(json, GetCardResponse.class);

    Card card = underTest.toCard(getCardResponse);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(card.getCardCountryCode()).isEqualTo("GB");
      soft.assertThat(card.getCardCurrencyCode()).isEqualTo("GBP");
      soft.assertThat(card.getCardExpiryDate()).isEqualTo(LocalDate.of(2022, 11, 6).atStartOfDay(ZoneId.of("Z")).toInstant());
      soft.assertThat(card.getCardEffectiveDate()).isEqualTo((LocalDate.of(2019, 11, 6).atStartOfDay(ZoneId.of("Z"))).toInstant());
      soft.assertThat(card.getPartyKey()).isEqualTo(UUID.fromString("4930f887-3def-4aef-9517-0a0dbbbc002"));
      soft.assertThat(card.getSubscriptionKey()).isEqualTo(UUID.fromString("886a2463-9b21-402e-84a2-afde330107bb"));
      soft.assertThat(card.getTenantKey()).isEqualTo("10000");
      soft.assertThat(card.getCardStatus()).isEqualTo(CardStatus.ACTIVE);
      soft.assertThat(card.getSubscriptionStatus()).isEqualTo(ACTIVE);
    });

  }

  private Card getCard() {

    return Card.builder()
        .panHash(PAN_HASH)
        .cardCountryCode("UK")
        .cardCurrencyCode("GBP")
        .cardEffectiveDate(CARD_EFFECTIVE_DATE)
        .cardExpiryDate(CARD_EXPIRY_DATE)
        .partyKey(fromString("9933ad98-31cc-48e0-8a2a-ffb45c1811b7"))
        .subscriptionKey(SUBSCRIPTION_KEY)
        .tenantKey("10000")
        .subscriptionStatus(ACTIVE)
        .cardStatus(CardStatus.ACTIVE)
        .build();
  }

  private GetCardResponse getCardResponse() {

    GetCardResponse response = new GetCardResponse();
    response.setPanHash(PAN_HASH);
    response.setCardCountryCode("UK");
    response.setCardCurrencyCode("GBP");
    response.setCardEffectiveDate(CARD_EFFECTIVE_DATE_STR);
    response.setCardExpiryDate(CARD_EXPIRY_DATE_STR);
    response.setPartyKey("9933ad98-31cc-48e0-8a2a-ffb45c1811b7");
    response.setTenantKey("10000");
    response.setSubscriptionKey(SUBSCRIPTION_KEY.toString());
    response.setSubscriptionStatus("ACTIVE");
    response.setCardStatus("ACTIVE");
    return response;
  }
}
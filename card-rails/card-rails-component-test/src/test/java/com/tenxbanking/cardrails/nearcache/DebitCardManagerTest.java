package com.tenxbanking.cardrails.nearcache;

import static com.tenxbanking.cardrails.data.DebitCardManagerDataFactory.CARD_COUNTRY_CODE;
import static com.tenxbanking.cardrails.data.DebitCardManagerDataFactory.CARD_CURRENCY_CODE;
import static com.tenxbanking.cardrails.data.DebitCardManagerDataFactory.CARD_EFFECTIVE_DATE;
import static com.tenxbanking.cardrails.data.DebitCardManagerDataFactory.CARD_EXPIRY_DATE;
import static com.tenxbanking.cardrails.data.DebitCardManagerDataFactory.SUBSCRIPTION_STATUS;
import static com.tenxbanking.cardrails.data.DebitCardManagerDataFactory.getCardRequest;
import static com.tenxbanking.cardrails.data.DebitCardManagerDataFactory.getCardResponse;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.stub.DebitCardManagerWiremockStubs.stubPostCardByPanHashSuccess;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_SECOND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.cards.DebitCardManager;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.adapter.secondary.redis.DebitCardRedisRepository;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DebitCardManagerTest extends BaseComponentTest {

  private static final UUID SUBSCRIPTION_KEY = fromString("c6bc5c48-726d-450e-b50e-9293c9e9f077");
  private static final String PAN_HASH = "5c9001eeea39ff0d004c8fef4e2979eb666323a8eb4dce75a6";

  @Autowired
  private DebitCardRedisRepository debitCardRedisRepository;

  @Autowired
  private DebitCardManager unit;

  @Before
  public void before() {
    debitCardRedisRepository.deleteAll();
  }

  @Test
  public void shouldGetCardFromRedis() {

    Card expectedCard = getCard();
    debitCardRedisRepository.save(expectedCard);

    Optional<Card> actual = unit.findByCardIdHash(PAN_HASH);

    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(expectedCard);
  }

  @Test
  public void shouldGetCardFromDebitCardManagerWhenCardNotInRedisAndCacheItAfter()
      throws JsonProcessingException {

    GetCardRequest request = getCardRequest(PAN_HASH);
    GetCardResponse response = getCardResponse(SUBSCRIPTION_KEY, PAN_HASH);
    stubPostCardByPanHashSuccess(request, response);

    Card expected = getCard();

    Optional<Card> actual = unit.findByCardIdHash(PAN_HASH);

    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(expected);

    await().atMost(ONE_SECOND).untilAsserted(() -> {
      Optional<Card> savedCard = debitCardRedisRepository.findById(PAN_HASH);
      assertThat(savedCard.isPresent()).isTrue();
      assertThat(savedCard.get()).isEqualTo(expected);
    });
  }

  private Card getCard() {

    return Card.builder()
        .panHash(PAN_HASH)
        .cardCurrencyCode(CARD_CURRENCY_CODE)
        .cardCountryCode(CARD_COUNTRY_CODE)
        .cardEffectiveDate(CARD_EFFECTIVE_DATE)
        .cardExpiryDate(CARD_EXPIRY_DATE)
        .subscriptionStatus(SubscriptionStatus.fromString(SUBSCRIPTION_STATUS))
        .partyKey(PARTY_KEY)
        .tenantKey(TENANT_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .cardStatus(CardStatus.ACTIVE)
        .build();
  }
}

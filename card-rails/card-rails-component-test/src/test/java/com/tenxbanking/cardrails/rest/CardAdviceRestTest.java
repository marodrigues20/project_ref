package com.tenxbanking.cardrails.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.stub.DebitCardManagerWiremockStubs.stubGetCardSettings;
import static com.tenxbanking.cardrails.stub.LedgerManagerWiremockStubs.stubConfirmReservation;
import static com.tenxbanking.cardrails.stub.LedgerManagerWiremockStubs.stubPostReserve;
import static com.tenxbanking.cardrails.stub.SubscriptionManagerWiremockStubs.stubGetSubscriptionProducts;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.primary.rest.model.response.SchemeMessageResponse;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.ChannelSettings;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionMessageEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionMessageCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.ReserveResponse;
import com.tenxbanking.cardrails.adapter.secondary.redis.DebitCardRedisRepository;
import com.tenxbanking.cardrails.adapter.secondary.redis.SubscriptionRedisRepository;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.data.DebitCardManagerDataFactory;
import com.tenxbanking.cardrails.data.SubscriptionManagerDataFactory;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.service.PanHashingService;
import com.tenxbanking.cardrails.stub.DebitCardManagerWiremockStubs;
import com.tenxbanking.cardrails.util.FileUtils;
import java.math.BigDecimal;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CardAdviceRestTest extends BaseComponentTest {

  private static final String ENDPOINT = "/api/v1/transactions/authorisation_advice";
  private static final String ADVICE_JSON = "/json/advice.json";

  @Autowired
  private PanHashingService panHashingService;
  @Autowired
  private CardTransactionCockroachRepository cardTransactionCockroachRepository;
  @Autowired
  private TransactionMessageCockroachRepository transactionMessageCockroachRepository;
  @Autowired
  private DebitCardRedisRepository debitCardRedisRepository;
  @Autowired
  private SubscriptionRedisRepository subscriptionRedisRepository;
  @Autowired
  private TestRestTemplate testRestTemplate;

  @Before
  public void setup() throws Exception {
    cardTransactionCockroachRepository.deleteAll();
    transactionMessageCockroachRepository.deleteAll();
    debitCardRedisRepository.deleteAll();
    subscriptionRedisRepository.deleteAll();
    final String panHash = givenACardWithPanHash();
    givenASubscriptionForCard();
    givenCardSettings(panHash);
  }

  @Test
  public void successfulCardAdvice() throws Exception {
    givenLedgerReserveResponse(true);
    givenLedgerConfirmReservationWillReturn200();

    HttpEntity<String> entity = buildEntity(FileUtils.readFile(ADVICE_JSON));

    ResponseEntity<SchemeMessageResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, SchemeMessageResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<CardTransactionEntity> cashTransactionEntities = cardTransactionCockroachRepository.findAll();

    assertThat(cashTransactionEntities)
        .extracting(CardTransactionEntity::getCardTransactionType)
        .containsExactlyInAnyOrder(ADVICE);

    List<TransactionMessageEntity> transactionMessages = transactionMessageCockroachRepository.findAll();

    assertThat(transactionMessages).hasSize(2);

    verify(postRequestedFor(urlEqualTo("/ledger-manager/v2/reserve")));
    verify(postRequestedFor(urlEqualTo("/ledger-manager/v1/processPayments")));
  }

  @Test
  public void failedCardAdvice() throws Exception {

    HttpEntity<String> entity = buildEntity(FileUtils.readFile(ADVICE_JSON).replace("\"authResponseCode\": \"00\"", "\"authResponseCode\": \"05\""));

    ResponseEntity<SchemeMessageResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, SchemeMessageResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<CardTransactionEntity> cashTransactionEntities = cardTransactionCockroachRepository.findAll();

    assertThat(cashTransactionEntities)
        .extracting(CardTransactionEntity::getCardTransactionType)
        .containsExactlyInAnyOrder(ADVICE);

    List<TransactionMessageEntity> transactionMessages = transactionMessageCockroachRepository.findAll();

    assertThat(transactionMessages).hasSize(2);

    verify(0, postRequestedFor(urlEqualTo("/ledger-manager/v2/reserve")));
    verify(0, postRequestedFor(urlEqualTo("/ledger-manager/v1/processPayments")));
  }

  private void givenCardSettings(String panHash) throws JsonProcessingException {
    stubGetCardSettings(new DebitCardSettingsResponse(
        "token",
        panHash,
        CardStatus.ACTIVE,
        new ChannelSettings(true, true, true, true, true, true)
    ));
  }

  private void givenLedgerConfirmReservationWillReturn200() {
    stubConfirmReservation();
  }

  private void givenLedgerReserveResponse(boolean success) throws JsonProcessingException {
    stubPostReserve(new ReserveResponse(BigDecimal.valueOf(1), success));
  }

  private String givenACardWithPanHash() throws JsonProcessingException {

    String cardIdHash  = panHashingService.hashCardId(CARD_ID);
    GetCardRequest request = DebitCardManagerDataFactory.getCardRequest(cardIdHash);
    GetCardResponse response = DebitCardManagerDataFactory
        .getCardResponse(SUBSCRIPTION_KEY, cardIdHash);

    DebitCardManagerWiremockStubs.stubPostCardByPanHashSuccess(request, response);

    return cardIdHash;
  }

  private HttpEntity<String> buildEntity(String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(singletonList(APPLICATION_JSON));
    headers.setContentType(APPLICATION_JSON);
    return new HttpEntity<>(json, headers);
  }

  private void givenASubscriptionForCard() throws Exception {
    SubscriptionProductsResponse response = SubscriptionManagerDataFactory
        .getSubscriptionProductsResponse(SUBSCRIPTION_KEY);

    stubGetSubscriptionProducts(response);
  }

}

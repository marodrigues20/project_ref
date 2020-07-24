package com.tenxbanking.cardrails.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.tenxbanking.cardrails.domain.TestConstant.BANKNET_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.CHIP_PIN;
import static com.tenxbanking.cardrails.stub.DebitCardManagerWiremockStubs.stubGetCardSettings;
import static com.tenxbanking.cardrails.stub.LedgerManagerWiremockStubs.stubPostReverse;
import static com.tenxbanking.cardrails.stub.SubscriptionManagerWiremockStubs.stubGetSubscriptionProducts;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.primary.rest.model.response.ErrorResponse;
import com.tenxbanking.cardrails.adapter.primary.rest.model.response.SchemeMessageResponse;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.ChannelSettings;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionMessageEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionMessageCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.AmountDto;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.BalanceDto;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.BalanceResponse;
import com.tenxbanking.cardrails.adapter.secondary.redis.DebitCardRedisRepository;
import com.tenxbanking.cardrails.adapter.secondary.redis.SubscriptionRedisRepository;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.data.DebitCardManagerDataFactory;
import com.tenxbanking.cardrails.data.SubscriptionManagerDataFactory;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.service.PanHashingService;
import com.tenxbanking.cardrails.stub.DebitCardManagerWiremockStubs;
import com.tenxbanking.cardrails.util.FileUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CardReversalRestTest extends BaseComponentTest {

  private static final String ENDPOINT = "/api/v1/transactions/reversal_advice";
  private static final String RETRIEVAL_REFERENCE_ID = "CARD_REVERSAL_TEST_12345";
  private static final String REVERSAL_JSON = "/json/reversal.json";
  private static final String REPLACE_REVERSAL_AMOUNTS = "\"reversalAmounts\": null";
  private static final String WITH_EMPTY_REVERSAL = "\"reversalAmounts\": {}";
  private static final String WITH_NO_BILLING = "\"reversalAmounts\": {\n"
      + "    \"transaction\": 1,\n"
      + "    \"settlement\": 1\n"
      + "  }";
  private static final String WITH_NO_TRANSACTION = "\"reversalAmounts\": {\n"
     // + "    \"settlement\": 1,\n"
      + "    \"billing\": 1\n"
      + "  }";
  private static final String WITH_REVERSAL_AMOUNTS = "\"reversalAmounts\": {\n"
      + "    \"transaction\": 1,\n"
      + "    \"settlement\": 1,\n"
      + "    \"billing\": 1\n"
      + "  }";

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
  public void successfulReversal() throws Exception {
    givenACardAuth();
    givenLedgerReversalRequestWillSucceed();

    HttpEntity<String> entity = buildEntity(FileUtils.readFile(REVERSAL_JSON)
        .replace(REPLACE_REVERSAL_AMOUNTS, WITH_REVERSAL_AMOUNTS));

    ResponseEntity<SchemeMessageResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, SchemeMessageResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<CardTransactionEntity> cashTransactionEntities = cardTransactionCockroachRepository.findAll()
        .stream()
        .filter(e -> e.getRetrievalReferenceNumber().equals(RETRIEVAL_REFERENCE_ID))
        .collect(Collectors.toList());

    assertThat(cashTransactionEntities)
        .extracting(CardTransactionEntity::getCardTransactionType)
        .containsExactlyInAnyOrder(AUTH, REVERSAL);

    List<TransactionMessageEntity> transactionMessages = transactionMessageCockroachRepository.findAll();

    assertThat(transactionMessages).hasSize(2);

    verify(postRequestedFor(urlEqualTo("/ledger-manager/v1/authorisations/reversal")));
  }

  @Test
  public void invalidReversalAmount() throws Exception {
    givenACardWithPanHash();
    givenASubscriptionForCard();
    givenACardAuth();
    givenLedgerReversalRequestWillSucceed();

    HttpEntity<String> entity = buildEntity(FileUtils.readFile(REVERSAL_JSON));

    ResponseEntity<ErrorResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody().getMessage())
        .isEqualTo("[No Reversal Amount is defined]");

  }

  @Test
  public void invalidEmptyReversal() throws Exception {
    givenACardWithPanHash();
    givenASubscriptionForCard();
    givenACardAuth();
    givenLedgerReversalRequestWillSucceed();

    Pattern regex = Pattern.compile(REPLACE_REVERSAL_AMOUNTS, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    HttpEntity<String> entity = buildEntity(regex.matcher(
        FileUtils.readFile(REVERSAL_JSON)).replaceAll(WITH_EMPTY_REVERSAL));

    ResponseEntity<ErrorResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody().getMessage())
        .isEqualTo("[No Billing Amount defined in Reversal Amount]");

  }

  @Test
  public void invalidReversalNoBilling() throws Exception {
    givenACardWithPanHash();
    givenASubscriptionForCard();
    givenACardAuth();
    givenLedgerReversalRequestWillSucceed();

    Pattern regex = Pattern.compile(REPLACE_REVERSAL_AMOUNTS, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    HttpEntity<String> entity = buildEntity(regex.matcher(
        FileUtils.readFile(REVERSAL_JSON)).replaceAll(WITH_NO_BILLING));

    ResponseEntity<ErrorResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody().getMessage())
        .isEqualTo("[No Billing Amount defined in Reversal Amount]");

  }

  @Test
  public void validReversalNoTransactionAmount() throws Exception {
    givenACardWithPanHash();
    givenASubscriptionForCard();
    givenACardAuth();
    givenLedgerReversalRequestWillSucceed();

    Pattern regex = Pattern.compile(REPLACE_REVERSAL_AMOUNTS, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    HttpEntity<String> entity = buildEntity(regex.matcher(
        FileUtils.readFile(REVERSAL_JSON)).replaceAll(WITH_NO_TRANSACTION));

    ResponseEntity<ErrorResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //assertThat(response.getBody().getMessage())
    //    .isEqualTo("[No Transaction Amount defined in Reversal Amount]");

  }

  @Test
  public void cannotFindMatchingAuth() throws Exception {

    HttpEntity<String> entity = buildEntity(FileUtils.readFile(REVERSAL_JSON)
        .replace(REPLACE_REVERSAL_AMOUNTS, WITH_REVERSAL_AMOUNTS));

    ResponseEntity<ErrorResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().getCode()).isEqualTo("76");
    assertThat(response.getBody().getMessage())
        .isEqualTo(String.format(
            "Cannot matching auth for reversal with retrievalReferenceNumber=%s, banknetReferenceNumber=%s, authCode=%s",
            RETRIEVAL_REFERENCE_ID,
            "00001106T",
            "123456"));
  }

  private void givenLedgerReversalRequestWillSucceed() throws JsonProcessingException {
    stubPostReverse(new BalanceResponse(new BalanceDto("n", new AmountDto("GBP", BigDecimal.valueOf(1)))));
  }


  private void givenACardAuth() {
    CardTransactionEntity entity = new CardTransactionEntity(
        null,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        Instant.now(),
        List.of(),
        "123456",
        AuthResponseCode._00,
        CARD_ID,
        BANKNET_REFERENCE_NUMBER,
        AUTH,
        RETRIEVAL_REFERENCE_ID,
        BigDecimal.valueOf(12),
        "GBP",
        BigDecimal.valueOf(12),
        "GBP",
        null,
        null,
        BigDecimal.valueOf(0),
        "GBP",
        BigDecimal.valueOf(0),
        "GBP",
        BigDecimal.valueOf(12),
        "GBP",
        "MERCHANT_CATEGORY_CODE",
        "ACTL",
        "01",
        BigDecimal.valueOf(1),
        "02/19",
        "051",
        "00",
        "networkId",
        "GBR",
        BigDecimal.valueOf(11),
        null,
        true,
        CHIP_PIN
    );

    cardTransactionCockroachRepository.save(entity);
  }

  private String givenACardWithPanHash() throws JsonProcessingException {

    String cardIdHash = panHashingService.hashCardId(CARD_ID);
    GetCardRequest request = DebitCardManagerDataFactory.getCardRequest(cardIdHash);
    GetCardResponse response = DebitCardManagerDataFactory
        .getCardResponse(SUBSCRIPTION_KEY, cardIdHash);

    DebitCardManagerWiremockStubs.stubPostCardByPanHashSuccess(request, response);

    return cardIdHash;
  }

  private void givenCardSettings(String panHash) throws JsonProcessingException {
    stubGetCardSettings(new DebitCardSettingsResponse(
        "token",
        panHash,
        CardStatus.ACTIVE,
        new ChannelSettings(true, true, true, true, true, true)
    ));
  }

  public HttpEntity<String> buildEntity(String jsonRequest) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(singletonList(APPLICATION_JSON));
    headers.setContentType(APPLICATION_JSON);
    return new HttpEntity<>(jsonRequest, headers);
  }

  private void givenASubscriptionForCard() throws Exception {
    SubscriptionProductsResponse response = SubscriptionManagerDataFactory
        .getSubscriptionProductsResponse(SUBSCRIPTION_KEY);

    stubGetSubscriptionProducts(response);
  }

}

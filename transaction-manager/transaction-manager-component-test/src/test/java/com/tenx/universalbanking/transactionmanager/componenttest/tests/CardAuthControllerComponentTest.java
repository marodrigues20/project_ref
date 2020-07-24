package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubLMProcessPayments;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubPaymentDecisionV1;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.universalbanking.transactionmanager.componenttest.BaseComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils;
import com.tenx.universalbanking.transactionmanager.entity.AuthorisationId;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.SchemeMessageResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class CardAuthControllerComponentTest extends BaseComponentTest {

  private static final String CAIN001_TRANSACTION_MESSAGE_REQUEST = "data/request/cardauth/cain001TransactionMesageWithToken.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_REQUEST_FOR_MAG_STRIPE_PAYMENT = "data/request/cardauth/cain001TransactionMessageWithMAgStripe.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_REQUEST_CARD_REVERSAL = "data/request/cardauth/cain001TransactionMessageRequestCardReversal.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_AUTH_ADVICE_REQUEST = "data/request/cardauth/cain001TransactionMesageAuthAdvice.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_REQUEST_NEGATIVE_AMOUNT = "data/request/cardauth/cain001TransactionMessageNegativeAmount.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE = "data/response/cardauth/cain002Response.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE_FOR_MAG_STRIPE = "data/response/cardauth/cain002ResponseForMagStripe.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE_DUPLICATE_AUTH_ADVICE_REQUEST = "data/response/cardauth/cain002ResponseForDuplicateRequest.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL = "data/response/cardauth/cain002ResponseFailCase.json";
  private static final String CARD_REVERSAL_CAIN002_KAFKA_MESSAGE = "data/ledger-messages/cardauth/Cain002TransactionMessageForCardReversal.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_RESPONSE_NEGATIVE_AMOUNT = "data/response/cardauth/failResponseForNegativeAmount.json";

  private ObjectMapper mapper = new ObjectMapper();

  @Autowired
  private PaymentAuthorisations paymentAuthorisations;

  @Test
  @Retry
  public void testCain001Process() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        CAIN001_TRANSACTION_MESSAGE_REQUEST);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/transaction-manager/card-auth"), entity,
            CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE);
    isMatch(expected, responseString, false);
  }

  @Test
  @Retry
  public void testCain001ProcessForMagStripe() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        CAIN001_TRANSACTION_MESSAGE_REQUEST_FOR_MAG_STRIPE_PAYMENT);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/transaction-manager/card-auth"), entity,
            CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils
        .getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE_FOR_MAG_STRIPE);
    isMatch(expected, responseString, false);
  }

  @Test
  @Retry
  public void testTurbineCardAuth() throws IOException {
    //given
    paymentAuthorisations.deleteAll();
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    stubDCM(WIREMOCK_SERVER);

    //and
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-B3-TraceId", "5c41b539181035048daf8df39b94f2fd");
    headers.set("l5d-test", "test123");
    headers.setContentType(APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(CARD_AUTH_REQUEST, headers);

    //when
    ResponseEntity<SchemeMessageResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/v1/scheme-message/authorisation"), request,
            SchemeMessageResponse.class);

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    SchemeMessageResponse body = response.getBody();

    assertThat(body).isNotNull();
    assertThat(body.getReasonCode()).isEqualTo("00");
  }

  @Test
  @Retry
  public void testTurbineCardAdvice() throws IOException {
    //given
    paymentAuthorisations.deleteAll();
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    stubDCM(WIREMOCK_SERVER);

    //and
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(CARD_AUTH_REQUEST, headers);

    //when
    ResponseEntity<SchemeMessageResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/v1/scheme-message/advice"), request,
            SchemeMessageResponse.class);

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    SchemeMessageResponse body = response.getBody();

    assertThat(body).isNotNull();
    assertThat(body.getReasonCode()).isEqualTo("00");
  }

  @Test
  @Retry
  public void testTurbineCardReversal() throws IOException {
    //given
    paymentAuthorisations.deleteAll();
    createAuthorisationEntity("AUTH");
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    stubDCM(WIREMOCK_SERVER);

    //and
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(CARD_AUTH_REQUEST, headers);

    //when
    ResponseEntity<SchemeMessageResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/v1/scheme-message/reversal"), request,
            SchemeMessageResponse.class);

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    SchemeMessageResponse body = response.getBody();

    assertThat(body).isNotNull();
    assertThat(body.getReasonCode()).isEqualTo("00");
  }

  @Test
  @Retry
  public void testCain001CardReversalProcess() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        CAIN001_TRANSACTION_MESSAGE_REQUEST_CARD_REVERSAL);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/transaction-manager/card-auth"), entity,
            CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CARD_REVERSAL_CAIN002_KAFKA_MESSAGE);
    isMatch(expected, responseString, false);
  }

  @Test
  @Retry
  public void testCain001ProcessForDuplicateRequest() throws IOException {
    createAuthorisationEntity(null);
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        CAIN001_TRANSACTION_MESSAGE_AUTH_ADVICE_REQUEST);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/transaction-manager/card-auth"), entity,
            CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils
        .getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE_DUPLICATE_AUTH_ADVICE_REQUEST);
    isMatch(expected, responseString, false);
  }

  @Test
  @Retry
  public void testCain001ProcessForNegativeAmountRequest() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        CAIN001_TRANSACTION_MESSAGE_REQUEST_NEGATIVE_AMOUNT);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/transaction-manager/card-auth"), entity,
            CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils
        .getFileContent(CAIN001_TRANSACTION_MESSAGE_RESPONSE_NEGATIVE_AMOUNT);
    isMatch(expected, responseString, false);
  }

  @Test
  @Retry
  public void testCain001ProcessFail() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, false);    //and
    stubLMProcessPayments(WIREMOCK_SERVER);
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        CAIN001_TRANSACTION_MESSAGE_REQUEST);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply("/transaction-manager/card-auth"), entity,
            CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL);
    isMatch(expected, responseString, false);
  }

  private void createAuthorisationEntity(String transactionType) {
    Authorisations authorisations = new Authorisations();
    authorisations.setTransactionId("1234");
    AuthorisationId authorisationId = new AuthorisationId();
    authorisationId.setBankNetReferenceNumber("1000");
    authorisationId.setAuthorisationCode("1234");
    authorisationId.setTransactionDate(LocalDate.of(2018, 05, 11));
    authorisations.setId(authorisationId);
    authorisations.setTsysAccountId("121212");
    authorisations.setCorrelationId("01155592457122aced38-849e-4820-b734-a9542134eb14");
    authorisations.setTransactionAmount(new BigDecimal(123));
    authorisations.setTransactionType(transactionType);
    paymentAuthorisations.save(authorisations);
  }

  private static final String CARD_AUTH_REQUEST = "{\n"
      + "   \"messageType\":\"Advice\",\n"
      + "   \"processingCode\":\"123456\",\n"
      + "   \"systemTraceNumber\":\"789\",\n"
      + "   \"transaction\":{\n"
      + "      \"amounts\":{\n"
      + "         \"transaction\":{\n"
      + "            \"amount\":1,\n"
      + "            \"currency\":\"826\"\n"
      + "         },\n"
      + "         \"settlement\":{\n"
      + "            \"amount\":2,\n"
      + "            \"currency\":\"826\"\n"
      + "         },\n"
      + "         \"billing\":{\n"
      + "            \"amount\":3,\n"
      + "            \"currency\":\"826\"\n"
      + "         },\n"
      + "         \"fee\":null,\n"
      + "         \"additionalAmounts\":null,\n"
      + "         \"conversionRate\":1\n"
      + "      },\n"
      + "      \"networkCode\":null,\n"
      + "      \"banknetReferenceNumber\":\"1000\",\n"
      + "      \"dates\": {\n"
      + "         \"transactionDate\":\"2019-01-01\",\n"
      + "         \"transactionTime\":\"080000\",\n"
      + "         \"settlementDate\":\"2019-01-01\",\n"
      + "         \"conversionDate\":null,\n"
      + "         \"transmissionDateTime\":\"2019-01-01T13:00:00.000Z\"\n"
      + "   },\n"
      + "      \"retrievalReferenceNumber\":null\n"
      + "   },\n"
      + "   \"card\":{\n"
      + "      \"id\":\"777\",\n"
      + "      \"expiryDate\":\"0824\"\n"
      + "   },\n"
      + "   \"acquirer\":{\n"
      + "      \"id\":null,\n"
      + "      \"countryCode\":null\n"
      + "   },\n"
      + "   \"merchant\":{\n"
      + "      \"name\":null,\n"
      + "      \"address\":null,\n"
      + "      \"terminalId\":null,\n"
      + "      \"categoryCode\":\"6006\",\n"
      + "      \"acceptorIdCode\":\"123\",\n"
      + "      \"bankCardPhone\":null\n"
      + "   },\n"
      + "   \"pos\":{\n"
      + "      \"conditionCode\":\"00\",\n"
      + "      \"additionalPosDetail\":\"moreDetails\",\n"
      + "      \"posEntryMode\":\"07\",\n"
      + "      \"extendedDataConditionCodes\":null\n"
      + "   },\n"
      + "   \"additionalData\":null,\n"
      + "   \"fraudScoreData\":null,\n"
      + "   \"reversalAmounts\":null,\n"
      + "   \"authCode\":null,\n"
      + "   \"authResponseCode\":null\n"
      + "}";
}
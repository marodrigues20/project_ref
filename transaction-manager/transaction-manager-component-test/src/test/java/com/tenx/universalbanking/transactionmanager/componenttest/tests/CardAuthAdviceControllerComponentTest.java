package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.*;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.AVAILABLE_BALANCE;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.universalbanking.transactionmanager.componenttest.BaseComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils;
import com.tenx.universalbanking.transactionmanager.entity.AuthorisationId;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class CardAuthAdviceControllerComponentTest extends BaseComponentTest {

  private static final String CAIN001_TRANSACTION_MESSAGE_REQUEST = "data/request/cardauthadvice/cain001TransactionMesageWithToken.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_REQUEST_CARD_REVERSAL = "data/request/cardauthadvice/cain001TransactionMessageRequestCardReversal.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_REQUEST_CARD_FAILED = "data/request/cardauthadvice/cain001TransactionMessageRequestCardFailed.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_AUTH_ADVICE_REQUEST = "data/request/cardauthadvice/cain001TransactionMesageAuthAdvice.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE = "data/response/cardauthadvice/cain002Response.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE_DUPLICATE_AUTH_ADVICE_REQUEST = "data/response/cardauthadvice/cain002ResponseForDuplicateRequest.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL = "data/response/cardauthadvice/cain002ResponseFailCase.json";
  private static final String CAIN002_LEDGER_MESSAGE_RESPONSE = "data/ledger-messages/cardauthadvice/Cain002TransactionMessage.json";
  private static final String CAIN002_LEDGER_MESSAGE_DECLINE_RESPONSE = "data/ledger-messages/cardauthadvice/Cain002TransactionMessageDecline.json";
  private static final String CARD_REVERSAL_CAIN002_KAFKA_MESSAGE = "data/ledger-messages/cardauthadvice/Cain002TransactionMessageForCardReversal.json";
  private static final String CARD_FAILED_CAIN002_KAFKA_MESSAGE = "data/ledger-messages/cardauthadvice/Cain002TransactionMessageFailed.json";

  private static final boolean NO_TRACING = false;

  private ObjectMapper mapper = new ObjectMapper();

  @Autowired
  private PaymentAuthorisations paymentAuthorisations;

  private static final String endpoint = "/transaction-manager/card-auth";

  @Test @Retry
  public void testCain001Process() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_REQUEST);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(endpoint), entity, CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CAIN002_LEDGER_MESSAGE_RESPONSE, NO_TRACING);
  }

  @Test @Retry
  public void testCain001CardReversalProcess() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_REQUEST_CARD_REVERSAL);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(endpoint), entity, CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CARD_REVERSAL_CAIN002_KAFKA_MESSAGE);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CARD_REVERSAL_CAIN002_KAFKA_MESSAGE, NO_TRACING);
  }

  @Test @Retry
  public void testCain001ProcessForDuplicateRequest() throws IOException {
    createAuthorisationEntity();

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_AUTH_ADVICE_REQUEST);
    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(endpoint), entity, CardAuthResponse.class);

    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE_DUPLICATE_AUTH_ADVICE_REQUEST);
    isMatch(expected, responseString, false);

  }

  @Test @Retry
  public void testCain001ProcessForFailedRequestWithBalance() throws IOException {
    //given
    stubGetBalancesUsingGET(WIREMOCK_SERVER);
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_REQUEST_CARD_FAILED);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(endpoint), entity, CardAuthResponse.class);
    //then
    CardAuthResponse cardAuthResponse = response.getBody();
    Double balanceAvailable = (Double)cardAuthResponse.getCain002Response().getAdditionalInfo().get(AVAILABLE_BALANCE.name());
    assertEquals(balanceAvailable.doubleValue(),9957.828,0.000);
    assertEquals(response.getStatusCode(), HttpStatus.OK);
  }

  @Test @Retry
  public void testCain001ProcessFail() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, false);    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_REQUEST);

    //when
    ResponseEntity<CardAuthResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(endpoint), entity, CardAuthResponse.class);
    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CAIN002_LEDGER_MESSAGE_DECLINE_RESPONSE, NO_TRACING);
  }

  private void createAuthorisationEntity(){
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
    authorisations.setTransactionType("AUTH");
    paymentAuthorisations.save(authorisations);
  }

}

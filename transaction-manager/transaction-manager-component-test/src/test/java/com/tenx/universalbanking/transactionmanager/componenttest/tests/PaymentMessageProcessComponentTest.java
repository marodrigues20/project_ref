package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubFAM3DSecureRedirectUrl;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubFAMFailure;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubFAMSuccess;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubLMProcessPayments;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubPaymentDecision;
import static org.junit.Assert.assertEquals;

import com.tenx.universalbanking.transactionmanager.componenttest.BaseComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public abstract class PaymentMessageProcessComponentTest extends BaseComponentTest {

  private static final String FPSIN_TRANSACTION_MESSAGE_REQUEST = "data/request/fpsin/FPSIN_TransactionMessageRequest.json";
  private static final String FPSIN_KAFKA_TRANSACTION_MESSAGE_REQUEST = "data/request/fpsin/fpsin_kafka_transaction_message.json";
  private static final String PAYMENT_DECISION_RESPONSE = "data/response/PaymentDecisionTransactionResponse.json";
  private static final String PAYMENT_DECISION_REQUEST = "data/request/PaymentDecisionRequest.json";
  private static final String FESS_AND_CHARGES_TRANSACTION_MESSAGE_REQUEST = "data/request/FeesAndChargesTransactionMessageRequest.json";
  private static final String SUCCESS_STATUS = "SUCCESS";
  private static final Boolean NO_TRACING = false;
  private static final String PAYMENT_DECISION_RESPONSE_FAILURE = "data/response/PaymentDecisionTransactionResponseFailure.json";
  private static final String FPSIN_TRANSACTION_MESSAGE_REQUEST_FAILURE = "data/request/PaymentDecisionRequestFailure.json";
  private static final String FAILURE_STATUS = "FAILED";
  private static final String FAILURE = "FAILURE";

  private static final String ONUS_TRANSACTION_MESSAGE_REQUEST = "data/request/onusrequest/OnUsTransactionMessageRequest.json";
  private static final String ONUS_KAFKA_TRANSACTION_MESSAGE_REQUEST = "data/request/onusrequest/OnUsKafkaTransactionMessageRequest.json";
  private static final String ONUS_FAILED_TRANSACTION_KAFKA_MESSAGE = "data/request/onusrequest/OnUSFailedTransactionKafkaMessage.json";
  private static final String PAYMENT_ON_US_DECISION_RESPONSE = "data/response/onusresponse/PaymentDecisionTransactionResponse.json";
  private static final String PAYMENT_ON_US_DECISION_REQUEST = "data/request/onusrequest/PaymentDecisionRequest.json";
  private static final String PAYMENT_DECISION_FAILURE_RESPONSE = "data/response/onusresponse/PaymentDecisionFailureResponse.json";

  private static final String REQUEST_PAYMENT = "data/request/request-payment/requestPayment.json";

  private final static String endpoint = "/transaction-manager/process-payment";

  @Test @Retry
  public void shouldSendMessageToKafkaTopic_whenProcessingFpsInMessage() throws IOException {

    // given
    stubPaymentDecision(WIREMOCK_SERVER, PAYMENT_DECISION_REQUEST, PAYMENT_DECISION_RESPONSE);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(FPSIN_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());
    // and
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    FPSIN_KAFKA_TRANSACTION_MESSAGE_REQUEST, NO_TRACING);
  }

  @Test @Retry
  public void processingFpsInMessageFailure() throws IOException {

    // given
    stubPaymentDecision(WIREMOCK_SERVER, PAYMENT_DECISION_REQUEST,
        PAYMENT_DECISION_RESPONSE_FAILURE);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(FPSIN_TRANSACTION_MESSAGE_REQUEST_FAILURE);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(FAILURE_STATUS, response.getBody().getPaymentStatus());

  }

  @Test @Retry
  public void shouldSendMessageToKafkaTopic_whenProcessingFeesAndChargesMessage()
      throws IOException {
    // given
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        FESS_AND_CHARGES_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());
    // and
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    FESS_AND_CHARGES_TRANSACTION_MESSAGE_REQUEST, NO_TRACING);
  }

  @Test @Retry
  public void processPaymentRequestMessageSuccess() throws IOException {

    stubFAMSuccess(WIREMOCK_SERVER);
    stubLMProcessPayments(WIREMOCK_SERVER);

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(REQUEST_PAYMENT);

    ResponseEntity<PaymentProcessResponse> response = testRestTemplate
        .exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());

  }

  @Test @Retry
  public void processPaymentRequestMessageFailure() throws IOException {

    stubFAMFailure(WIREMOCK_SERVER);
    stubLMProcessPayments(WIREMOCK_SERVER);

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(REQUEST_PAYMENT);

    ResponseEntity<PaymentProcessResponse> response = testRestTemplate
        .exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    assertEquals(FAILURE, response.getBody().getPaymentStatus());

  }

  @Test @Retry
  public void processPaymentRequestMessage3DSecure() throws IOException {
    String endpoint = "http://localhost:" + randomServerPort + "/transaction-manager/process-payment";

    stubFAM3DSecureRedirectUrl(WIREMOCK_SERVER);
    stubLMProcessPayments(WIREMOCK_SERVER);

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(REQUEST_PAYMENT);

    ResponseEntity<PaymentProcessResponse> response = testRestTemplate
        .exchange(endpoint, HttpMethod.POST, entity, PaymentProcessResponse.class);
    assertEquals(FAILURE, response.getBody().getPaymentStatus());

  }

  @Test @Retry
  public void shouldSendMessageToKafkaTopic_whenProcessingOnUsMessage() throws IOException {
    // given
    stubPaymentDecision(WIREMOCK_SERVER, PAYMENT_ON_US_DECISION_REQUEST, PAYMENT_DECISION_RESPONSE);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(ONUS_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());
    // and
    //verifyKafkaContainsMessage(LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, ONUS_KAFKA_TRANSACTION_MESSAGE_REQUEST, NO_TRACING);
  }

  @Test @Retry
  public void OnUsMessage_FailureCase() throws IOException {
    // given
    stubPaymentDecision(WIREMOCK_SERVER, PAYMENT_ON_US_DECISION_REQUEST, PAYMENT_DECISION_FAILURE_RESPONSE);
    // and
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(ONUS_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(FAILURE_STATUS, response.getBody().getPaymentStatus());
  }

}

package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubLMProcessPayments;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubPaymentDecision;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubPaymentDecisionSwaggerClient;
import static org.junit.Assert.assertEquals;

import com.tenx.universalbanking.transactionmanager.componenttest.BaseComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public abstract class BacsDirectDebitAndCreditTransferComponentTest extends BaseComponentTest {

  private static final String TRANSACTION_CT_MESSAGE_REQUEST =
      "data/request/bacsin/BACS_CT_IN_TransactionMessageRequest.json";
  private static final String SUCCESS_STATUS = "SUCCESS";
  private static final String FAILURE_STATUS = "FAILED";
  private static final boolean NO_TRACING = false;

  private static final String TRANSACTION_DD_MESSAGE_REQUEST =
       "data/request/bacsin/BACS_DD_IN_TransactionMessageRequest.json";
  private static final String PAYMENT_DECISION_REQUEST =
      "data/request/bacsin/PaymentDecisionRequest.json";
  private static final String PAYMENT_DECISION_SUCCESS_RESPONSE =
      "data/response/bacsin/PaymentDecisionSuccessResponse.json";
  private static final String PAYMENT_DECISION_FAILURE_RESPONSE =
      "data/response/bacsin/PaymentDecisionFailureResponse.json";
  
  private String endpoint = "/transaction-manager/process-payment";

  @Test @Retry
  public void testProcessPayment_shouldPublishMessageToKafka_whenPDFSPaymentDecisionIsSuccess()
      throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_CT_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());

  }

  @Test @Retry
  public void testProcessPayment_shouldPublishMessageToKafka_whenPDFSPaymentDecisionIsNotSuccess()
      throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, false);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_CT_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);

  }

  @Test @Retry
  public void testProcessPayment_shouldReturnFailureResponse_whenPDFSPaymentDecisionIsNotSuccess()
      throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, false);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_CT_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);

  }

  @Test @Retry
  public void testProcessPayment_shouldReturnSuccessResponse_whenPDFSPaymentDecisionIsSuccess()
      throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_CT_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());

  }

  @Test @Retry
  public void testProcessPayment_whenPDFPaymentDecisionIsSuccess() throws IOException {

    stubPaymentDecision(WIREMOCK_SERVER, PAYMENT_DECISION_REQUEST,
        PAYMENT_DECISION_SUCCESS_RESPONSE);
    stubLMProcessPayments(WIREMOCK_SERVER);

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_DD_MESSAGE_REQUEST);

    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);

    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());

  }

  @Test @Retry
  public void testProcessPayment_whenPDFPaymentDecisionIsNotSuccess() throws IOException {

    stubPaymentDecision(WIREMOCK_SERVER, PAYMENT_DECISION_REQUEST,
        PAYMENT_DECISION_FAILURE_RESPONSE);

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_DD_MESSAGE_REQUEST);

    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    assertEquals(FAILURE_STATUS, response.getBody().getPaymentStatus());
  }


}

package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.*;
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

public abstract class ChildSubscriptionComponentTest extends BaseComponentTest {

  private static final String CHILD_SUBSCRIPTION_TRANSACTION_MESSAGE_REQUEST = "data/request/childSubscription/ChildSubscriptionTransactionMessageRequest.json";
  private static final String CHILD_SUBSCRIPTION_TRANSACTION_MESSAGE_SEND_TO_KAFKA = "data/request/childSubscription/ChildSubscriptionKafkaTransactionMessage.json";
  private static final String PAYMENT_DECISION_RESPONSE = "data/response/PaymentDecisionTransactionResponse.json";
  private static final String PAYMENT_DECISION_REQUEST = "data/request/PaymentDecisionRequest.json";
  private static final String PAYMENT_DECISION_FAILURE_RESPONSE = "data/response/onusresponse/PaymentDecisionFailureResponse.json";
  private static final String SUCCESS_STATUS = "SUCCESS";
  private static final String FAILURE_STATUS = "FAILED";
  private static final boolean NO_TRACING = false;

  private final static String endpoint = "/transaction-manager/process-payment";

  @Test @Retry
  public void shouldSendMessageToKafkaTopic_whenProcessingChildSubscritionMessage() throws IOException {
    // given
    stubPaymentDecision(WIREMOCK_SERVER, PAYMENT_DECISION_REQUEST, PAYMENT_DECISION_RESPONSE);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(CHILD_SUBSCRIPTION_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());
    // and
   // verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CHILD_SUBSCRIPTION_TRANSACTION_MESSAGE_SEND_TO_KAFKA, NO_TRACING);
  }

  @Test @Retry
  public void ChildSubscrptionMessage_FailureCase() throws IOException {
    // given
    stubPaymentDecision(WIREMOCK_SERVER, PAYMENT_DECISION_REQUEST, PAYMENT_DECISION_FAILURE_RESPONSE);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(CHILD_SUBSCRIPTION_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(FAILURE_STATUS, response.getBody().getPaymentStatus());

  }

}

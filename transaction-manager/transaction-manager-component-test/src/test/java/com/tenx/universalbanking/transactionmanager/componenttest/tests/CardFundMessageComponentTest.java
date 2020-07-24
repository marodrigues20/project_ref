package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubLMProcessPayments;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubPdfFailureResponse;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubPdfSuccessResponse;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubWorldPayFaliureRefusedResponse;
import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.stubWorldPaySuccessResponse;
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

public abstract class CardFundMessageComponentTest extends BaseComponentTest {

  private static final String CARD_FUND_TRANSACTION_MESSAGE_REQUEST = "data/request/cardfund/cardFunTransactionMessageRequest.json";
  private static final String CARD_FUND_TOKEN_TRANSACTION_MESSAGE_REQUEST = "data/request/cardfund/cardFundTokenTransactionMessageRequest.json";
  private static final String CARD_FUND_TRANSACTION_KAFKA_RESPONCE = "data/response/cardfund/cardFundTranscationKafkaResponce.json";
  private static final String CARD_FUND_TRANSACTION_PDF_FAILURE_KAFKA_RESPONCE = "data/response/cardfund/cardFundTranscationPdfFailureKafkaResponce.json";
  private static final String WORLDPAY_FAILURE_KAFKA_MESSAGE = "data/response/cardfund/worldpay-failure-kafka-message.json";
  private static final String SUCCESS_STATUS = "SUCCESS";
  private static final boolean NO_TRACING = false;
  private static final String FAILED_STATUS = "FAILED";
  private static final String endpoint = "/transaction-manager/process-payment";

  @Test @Retry
  public void shouldReturnSuccess_whenCallFundAccountWithPan() throws IOException {
    stubPdfSuccessResponse(WIREMOCK_SERVER);
    stubWorldPaySuccessResponse(WIREMOCK_SERVER);
    stubLMProcessPayments(WIREMOCK_SERVER);
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(CARD_FUND_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());
    // and
    //verifyKafkaContainsMessage(LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    CARD_FUND_TRANSACTION_KAFKA_RESPONCE, NO_TRACING);
  }

  @Test @Retry
  public void shouldReturnFailure_whenPdfReturnFailure() throws IOException {
    stubPdfFailureResponse(WIREMOCK_SERVER);
    stubLMProcessPayments(WIREMOCK_SERVER);
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(CARD_FUND_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(FAILED_STATUS, response.getBody().getPaymentStatus());
    //verifyKafkaContainsMessage(LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    CARD_FUND_TRANSACTION_PDF_FAILURE_KAFKA_RESPONCE, NO_TRACING);
  }

  @Test @Retry
  public void shouldReturnFailure_whenWorldPayFailure_refusedResponse() throws IOException {
    stubPdfSuccessResponse(WIREMOCK_SERVER);
    stubWorldPayFaliureRefusedResponse(WIREMOCK_SERVER);
    stubLMProcessPayments(WIREMOCK_SERVER);
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(CARD_FUND_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(FAILED_STATUS, response.getBody().getPaymentStatus());
    assertEquals("Payment Refused - Refused", response.getBody().getReason().getMessage());
    assertEquals("22004", String.valueOf(response.getBody().getReason().getCode()));
    //verifyKafkaContainsMessage(LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    WORLDPAY_FAILURE_KAFKA_MESSAGE, NO_TRACING);
  }

  @Test @Retry
  public void shouldReturnSuccess_whenCallFundAccountWithToken() throws IOException {
    stubPdfSuccessResponse(WIREMOCK_SERVER);
    stubWorldPaySuccessResponse(WIREMOCK_SERVER);
    stubLMProcessPayments(WIREMOCK_SERVER);
    HttpEntity<TransactionMessage> entity =
        buildTransactionMsgRequest(CARD_FUND_TOKEN_TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());
    // and
    //verifyKafkaContainsMessage(LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    CARD_FUND_TRANSACTION_KAFKA_RESPONCE, NO_TRACING);
  }
}

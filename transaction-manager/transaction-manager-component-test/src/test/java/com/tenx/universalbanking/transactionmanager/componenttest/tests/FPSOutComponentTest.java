package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.*;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PDF_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVICE_UNAVAILABLE;
import static org.junit.Assert.assertEquals;

import com.tenx.universalbanking.transactionmanager.componenttest.BaseComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.validationlib.response.Error;
import com.tenx.validationlib.response.Errors;
import java.io.IOException;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;


public abstract class FPSOutComponentTest extends BaseComponentTest {

  private static final String TM_BASE = "data/request/fpsout/";
  private static final String TRANSACTION_MESSAGE_REQUEST =
          TM_BASE + "FPS_Out-TransactionMessageRequest.json";
  private static final String TRANSACTION_MESSAGE_FDP_REQUEST =
          TM_BASE + "FPS_Out-TransactionMessageFDPRequest.json";
  private static final String TRANSACTION_MESSAGE_AMOUNT_EXCEED_REQUEST =
          TM_BASE + "FpsOut-TransactionMessage_Amount_Exceed.json";
  private static final String TRANSACTION_MESSAGE_AMOUNT_LESS_ZERO_REQUEST =
          TM_BASE + "FpsOut-TransactionMessage_Amount_Less_Zero.json";
  private static final String TRANSACTION_MESSAGE_PAIN002_SUCCESS_REQUEST =
          TM_BASE + "FPS_Out-TransactionMessageRequest-Pain002-Success.json";
  private static final String TRANSACTION_MESSAGE_PAIN002_SUCCESS_RETURN_REQUEST =
          TM_BASE + "FPS_Out-TransactionMessageRequest-Pain002-Success-Return.json";
  private static final String TRANSACTION_MESSAGE_PAIN002_FAILURE_REQUEST =
          TM_BASE + "FPS_Out-TransactionMessageRequest-Pain002-failure.json";
  private static final String KAFKA_TRANSACTION_MESSAGE_REQUEST_PAIN001_ACK =
          TM_BASE + "fpsout_kafka_transaction_message_pain001_ack_success.json";
  private static final String KAFKA_TRANSACTION_MESSAGE_REQUEST_PAIN002 =
          TM_BASE + "fpsout_kafka_transaction_message_pain002_success.json";
  private static final String KAFKA_TRANSACTION_MESSAGE_REQUEST_PAIN002_RETURN =
          TM_BASE + "fpsout_kafka_transaction_message_pain002_success_return.json";

  private static final String LOCALHOST = "http://localhost:";
  private static final String SUCCESS_STATUS = "SUCCESS";
  private static final String FAILURE_STATUS = "FAILED";
  private static final boolean NO_TRACING = false;

  private static final String endpoint = "/transaction-manager/process-payment";

  @Test @Retry
  public void testProcessPayment_Success() throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());

    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    KAFKA_TRANSACTION_MESSAGE_REQUEST_PAIN001_ACK, NO_TRACING);
  }

  @Test @Retry
  public void testLedgerManagerPostingGateWayTimeOutException() throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
    stubLMProcessWithGatewayTimeoutStatus(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<Errors> response = testRestTemplate
        .postForEntity(serverUrl.apply(endpoint), entity, Errors.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    Error error =response.getBody().getErrors().get(0);
    assertEquals(INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getStatusCode(),
        error.getCode());
    assertEquals(INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getMessage(),error.getMessage());
  }

  @Test @Retry
  public void testFpsOutAmountExceedPDFfalure() throws IOException {
    stubPaymentDecisionFpsOutAmountExceedFailure(WIREMOCK_SERVER);

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_AMOUNT_EXCEED_REQUEST);

    ResponseEntity<PaymentProcessResponse> response = testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, PaymentProcessResponse.class);
    assertEquals(HttpStatus.OK,response.getStatusCode());
    assertEquals(TransactionManagerExceptionCodes.DECISION_FPS_PAYMENT_GREATER_THAN_LIMIT,
        response.getBody().getReason().getCode().intValue());
    assertEquals("FPS OUT Payment amount must be less than £250,000",response.getBody().getReason().getMessage());

  }

  @Test @Retry
  public void testFpsOutAmountLessZeroPDFfalure() throws IOException {
    stubPaymentDecisionFpsOutAmountLessZeroFailure(WIREMOCK_SERVER);

    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_AMOUNT_LESS_ZERO_REQUEST);

    ResponseEntity<PaymentProcessResponse> response = testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, PaymentProcessResponse.class);
    assertEquals(HttpStatus.OK,response.getStatusCode());
    assertEquals(TransactionManagerExceptionCodes.DECISION_FPS_PAYMENT_LESS_THAN_ZERO,
        response.getBody().getReason().getCode().intValue());
    assertEquals("Payment amount must be greater than zero",response.getBody().getReason().getMessage());

  }

  @Test @Retry
  public void testPDFReservationGateWayTimeOutException() throws IOException {
    // given
    stubPaymentDecisionWithGatewayTimeoutStatus(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<Errors> response = testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, Errors.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,response.getStatusCode());
    Error error =response.getBody().getErrors().get(0);
    assertEquals(INTERNAL_SERVER_ERROR_PDF_FAILURE.getStatusCode(),
        error.getCode());
    assertEquals(INTERNAL_SERVER_ERROR_PDF_FAILURE.getMessage(),error.getMessage());
  }

  @Test @Retry
  public void testPaymentProxyGateWayTimeOutException() throws IOException {
    // given
    stubPaymentProxyWithGatewayTimeoutStatus(WIREMOCK_SERVER, true);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<Errors> response = testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, Errors.class);
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE,response.getStatusCode());
    Error error =response.getBody().getErrors().get(0);
    assertEquals(INTERNAL_SERVICE_UNAVAILABLE.getStatusCode(),
        error.getCode());
    assertEquals(INTERNAL_SERVICE_UNAVAILABLE.getMessage(),error.getMessage());
  }

  @Test @Retry
  public void testProcessFDPPayment_Success() throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        TRANSACTION_MESSAGE_FDP_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());

    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    KAFKA_TRANSACTION_MESSAGE_REQUEST_PAIN001_ACK, NO_TRACING);
  }

  @Test @Retry
  public void testProcessPayment_Failure() throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, false);
    stubLMProcessPayments(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, PaymentProcessResponse.class);
    // then
    assertEquals(FAILURE_STATUS, response.getBody().getPaymentStatus());
  }


  @Test @Retry
  public void testProcessPaymentPAIN002_Success() throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    stubPPMSuccess(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_PAIN002_SUCCESS_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //    KAFKA_TRANSACTION_MESSAGE_REQUEST_PAIN002, NO_TRACING);
  }

  @Test @Retry
  public void testProcessPaymentPAIN002_SuccessReturn() throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    stubPPMSuccess(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_PAIN002_SUCCESS_RETURN_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
            testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, PaymentProcessResponse.class);
    // then
    assertEquals(SUCCESS_STATUS, response.getBody().getPaymentStatus());
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME,
    //        KAFKA_TRANSACTION_MESSAGE_REQUEST_PAIN002_RETURN, NO_TRACING);
  }

  @Test @Retry
  public void testProcessPaymentPAIN002_Failure() throws IOException {
    // given
    stubPaymentDecisionSwaggerClient(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    stubPPMSuccess(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_PAIN002_FAILURE_REQUEST);
    // when
    ResponseEntity<PaymentProcessResponse> response =
        testRestTemplate.postForEntity(serverUrl.apply(endpoint), entity, PaymentProcessResponse.class);
    // then
    assertEquals(FAILURE_STATUS, response.getBody().getPaymentStatus());

  }

  @Test @Retry
  public void testFPSOutForm3ServerErrorFailure() throws IOException {
    // given
    stubPdfSuccessResponse(WIREMOCK_SERVER);
    stubPaymentProxyForm3ServerError(WIREMOCK_SERVER);
    // and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(TRANSACTION_MESSAGE_REQUEST);
    // when
    ResponseEntity<Errors> response =
        testRestTemplate.exchange(serverUrl.apply(endpoint), HttpMethod.POST, entity, Errors.class);
    // then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(
        Integer.valueOf(TransactionManagerExceptionCodes.FPSOUT_INITIATION_TIMEOUT_ERROR),
        Integer.valueOf(response.getBody().getErrors().get(0).getCode()));
    assertEquals("Server error, request Timed out. Please retry after 5 mins",
        response.getBody().getErrors().get(0).getMessage());
  }

}

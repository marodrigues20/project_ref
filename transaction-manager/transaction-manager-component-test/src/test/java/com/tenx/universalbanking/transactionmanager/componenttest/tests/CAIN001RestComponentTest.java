package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import static com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.universalbanking.transactionmanager.componenttest.BaseComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.WireMockStubs;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public abstract class CAIN001RestComponentTest  extends BaseComponentTest {

  private static final String CAIN001_TRANSACTION_MESSAGE_REQUEST = "data/request/cardauth/cain001TransactionMesage.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE = "data/response/cardauth/cain002Response.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL = "data/response/cardauth/cain002ResponseFailCase.json";
  private static final String CAIN002_LEDGER_MESSAGE_RESPONSE = "data/ledger-messages/cardauth/Cain002TransactionMessage.json";
  private static final String CAIN002_LEDGER_MESSAGE_DECLINE_RESPONSE = "data/ledger-messages/cardauth/Cain002TransactionMessageDecline.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL_WITH_REASON_CODE_FNDI = "data/response/cardauth/cain002ResponseFailReasonCodeFNDICase.json";
  private static final String CAIN001_TRANSACTION_MESSAGE_INVALID_SM_REQUEST = "data/request/cardauth/cain001TransactionMesageInValidSM.json";
  private static final String CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL_WITH_REASON_CODE_05 = "data/response/cardauth/cain002ResponseFailReasonCode05Case.json";
  private static final boolean NO_TRACING = false;

  private ObjectMapper mapper = new ObjectMapper();
  
  @Test @Retry
  public void testCain001Process() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, true);
    stubLMProcessPayments(WIREMOCK_SERVER);
    //and
    String url = "http://localhost:" + randomServerPort + "/transaction-manager/card-auth";
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_REQUEST);
    //when
    ResponseEntity<PaymentProcessResponse> response = testRestTemplate
        .postForEntity(url, entity, PaymentProcessResponse.class);
    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CAIN002_LEDGER_MESSAGE_RESPONSE, NO_TRACING);
  }

  @Test @Retry
  public void testCain001ProcessFail() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, false);    //and
    stubLMProcessPayments(WIREMOCK_SERVER);
    String url = "http://localhost:" + randomServerPort + "/transaction-manager/card-auth";
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_REQUEST);
    //when
    ResponseEntity<PaymentProcessResponse> response = testRestTemplate
        .postForEntity(url, entity, PaymentProcessResponse.class);
    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CAIN002_LEDGER_MESSAGE_DECLINE_RESPONSE, NO_TRACING);
  }

  @Test @Retry
  public void testCain001ProcessFailReasonCodeFNDI() throws IOException {
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, false);    //and
    stubLMProcessPayments(WIREMOCK_SERVER);
    String url = "http://localhost:" + randomServerPort + "/transaction-manager/card-auth";
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_REQUEST);
    //when
    ResponseEntity<PaymentProcessResponse> response = testRestTemplate
        .postForEntity(url, entity, PaymentProcessResponse.class);
    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL_WITH_REASON_CODE_FNDI);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CAIN002_LEDGER_MESSAGE_DECLINE_RESPONSE, NO_TRACING);
  }

  @Test @Retry
  public void testCain001ProcessFailReasonCode05() throws IOException {
    //This case needs change when SM2 changes are done
    //given
    stubPaymentDecisionV1(WIREMOCK_SERVER, false);    //and
    stubLMProcessPayments(WIREMOCK_SERVER);
    String url = "http://localhost:" + randomServerPort + "/transaction-manager/card-auth";
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(CAIN001_TRANSACTION_MESSAGE_INVALID_SM_REQUEST);
    //when
    ResponseEntity<PaymentProcessResponse> response = testRestTemplate
        .postForEntity(url, entity, PaymentProcessResponse.class);
    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(CAIN002_TRANSACTION_MESSAGE_RESPONSE_FAIL_WITH_REASON_CODE_05);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CAIN002_LEDGER_MESSAGE_DECLINE_RESPONSE, NO_TRACING);
  }
}

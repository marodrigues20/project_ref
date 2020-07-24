package com.tenx.universalbanking.transactionmanager.componenttest.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.universalbanking.transactionmanager.componenttest.BaseComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils;
import com.tenx.universalbanking.transactionmanager.rest.responses.SettlementResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public abstract class CardSettlementControllerComponentTest extends BaseComponentTest {

  private static final String CAIN003_TRANSACTION_MESSAGE_REQUEST = "data/request/settlement/cain003.json";
  private static final String SETTLEMENT_RESPONSE = "data/response/settlement/settlement_response.json";
  private static final String CAIN005_TRANSACTION_MESSAGE_REQUEST = "data/request/settlement/cain005.json";
  private static final String CAIN003_LEDGER_MESSAGE = "data/response/settlement/cain003-ledger-manager-kafka-message.json";
  private static final String CAIN005_LEDGER_MESSAGE = "data/response/settlement/cain005-ledger-manager-kafka-message.json";

  private static final boolean NO_TRACING = false;

  private final static String endpoint = "/transaction-manager/settlement";

  private ObjectMapper mapper = new ObjectMapper();

  @Test @Retry
  public void testCain003Process() throws IOException {
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        CAIN003_TRANSACTION_MESSAGE_REQUEST);
    //when
    ResponseEntity<SettlementResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(endpoint), entity, SettlementResponse.class);
    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(SETTLEMENT_RESPONSE);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CAIN003_LEDGER_MESSAGE,
    //    NO_TRACING);
  }

  @Test @Retry
  public void testCain005Process() throws IOException {
    //and
    HttpEntity<TransactionMessage> entity = buildTransactionMsgRequest(
        CAIN005_TRANSACTION_MESSAGE_REQUEST);
    //when
    ResponseEntity<SettlementResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(endpoint), entity, SettlementResponse.class);
    //then
    final String responseString = mapper.writeValueAsString(response.getBody());
    final String expected = FileUtils.getFileContent(SETTLEMENT_RESPONSE);
    isMatch(expected, responseString, false);
    //verifyKafkaContainsMessage(Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME, CAIN005_LEDGER_MESSAGE,
    //    NO_TRACING);

  }
}

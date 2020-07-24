package com.tenx.universalbanking.transactionmanager.pacttest;

import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static au.com.dius.pact.model.PactSpecVersion.V3;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils.getFileContent;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.ImmutableMap;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.Retry;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class LedgerManagerPactTest {

  private static final String CONSUMER = "TransactionManager";
  private static final String PROVIDER = "LedgerManager";

  private static final String LEDGER_MANAGER_PAYMENTS_URL = "/ledger-manager/v1/processPayments";
  private static final String EXAMPLE_STRING = "string";
  private static final String EXAMPLE_AMOUNT = new BigDecimal("12.0").toString();
  private static final String EXAMPLE_EXCHANGE_AMOUNT = new BigDecimal("2.0").toString();

  private static final String END_DATE = "firstAvailableDateTime";
  private static final String START_DATE = "lastAvailableDateTime";
  private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private static final String FIRST_DATE_STRING = "2018-05-11T13:14:30.893+0000";
  private static final String LAST_DATE_STRING = "2019-02-12T09:33:30.850Z";

  private static final String GENERIC_TRANSACTION_MESSAGE_FILE = "data/pact-request/generic-transaction-message.json";
  private final TestRestTemplate restTemplate = new TestRestTemplate();

  private static final Map<String, String> HEADERS = ImmutableMap
      .of(CONTENT_TYPE, APPLICATION_JSON.toString());


  @Pact(consumer = CONSUMER, provider = PROVIDER)
  private RequestResponsePact createPactToProcessPayment(PactDslWithProvider builder) {
    return builder.given("Ledger Manager: Process the Payment")
        .uponReceiving(
            "a request to process payment")
        .method("POST")
        .headers(HEADERS)
        .body(new PactDslJsonBody()
            .object("additionalInfo")
            .stringType("TRANSACTION_STATUS", "SUCCESS")
            .stringType("TRANSACTION_CORRELATION_ID", EXAMPLE_STRING)
            .stringType("REQUEST_ID", EXAMPLE_STRING)
            .stringType("TENANT_PARTY_KEY", "10000")
            .closeObject()
            .object("header")
            .stringType("type", "CARD_AUTH")
            .nullValue("url")
            .closeObject()
            .eachLike("messages")
            .stringValue("type", "CAIN002")
            .object("message")
            .timestamp("TRANSACTION_DATE", DATE_PATTERN, getDate(FIRST_DATE_STRING))
            .stringType("TRANSACTION_CURRENCY_CODE", "GBP")
            .stringType("MERCHANT_CATEGORY_CODE", EXAMPLE_STRING)
            .stringType("CARD_ACCEPTOR_NAME", EXAMPLE_STRING)
            .stringType("TRANSACTION_TYPE", EXAMPLE_STRING)
            .stringType("CARD_DATA_ENTRY_MODE", EXAMPLE_STRING)
            .stringType("TRANSACTION_AMOUNT_QUALIFIER", EXAMPLE_STRING)
            .booleanValue("CARDHOLDER_PRESENT", true)
            .stringValue("EXCHANGE_RATE", EXAMPLE_EXCHANGE_AMOUNT)
            .stringValue("TOTAL_AMOUNT", EXAMPLE_AMOUNT)
            .stringType("CARD_ACCEPTOR_ID", EXAMPLE_STRING)
            .stringType("PAN_NUMBER", "5584345930798931")
            .stringValue("AMOUNT", EXAMPLE_AMOUNT)
            .stringValue("TRANSACTION_RESPONSE_CODE", "APPR")
            .closeObject()
            .object("additionalInfo")
            .stringType("SUBSCRIPTION_KEY", "a72cb8ca-e1c4-45f0-9de0-c20c40cd5569")
            .stringType("TRANSACTION_ID", "1749682283")
            .stringType("PARTY_KEY", EXAMPLE_STRING)
            .stringType("DIGITAL_SERVICE_KEY", EXAMPLE_STRING)
            .stringType("PAYMENT_METHOD_TYPE", EXAMPLE_STRING)
            .stringType("TRANSACTION_CODE", EXAMPLE_STRING)
            .closeObject()
            .closeObject()
            .closeArray()
            .close()
        )
        .path(LEDGER_MANAGER_PAYMENTS_URL)
        .willRespondWith()
        .status(HttpStatus.OK.value())
        .headers(HEADERS)
        .body(
            new PactDslJsonBody()
                .eachLike("data")
                .stringType("accountId", EXAMPLE_STRING)
                .stringType("creditDebitIndicator", "DEBIT")
                .stringType("datetime", "Description")
                .stringType("type", "CREDIT")
                .object("amount")
                .stringType("currency", "GBP")
                .decimalType("value", 0.0)
                .closeObject()
                .closeArray()
                .object("links")
                .nullValue("first")
                .nullValue("last")
                .nullValue("next")
                .nullValue("prev")
                .nullValue("self")
                .closeObject()
                .object("meta")
                .nullValue(END_DATE)
                .nullValue(START_DATE)
                .nullValue("totalPages")
                .closeObject()
                .close()
        )
        .toPact();
  }

  @Test @Retry
  @PactVerification(PROVIDER)
  public void shouldReturnTransactionResponse() {
    MockProviderConfig config = MockProviderConfig.createDefault(V3);
    RequestResponsePact pact = createPactToProcessPayment(
        ConsumerPactBuilder.consumer(CONSUMER).hasPactWith(PROVIDER));

    runConsumerTest(pact, config, runTest -> {
      ResponseEntity<String> response = callProvider(config,
          LEDGER_MANAGER_PAYMENTS_URL);
      verifyResponse(response);

    });
  }

  private ResponseEntity<String> callProvider(MockProviderConfig config, String url)
      throws IOException {
    return restTemplate.postForEntity(config.url() + url,
        buildTransactionMsgRequest(GENERIC_TRANSACTION_MESSAGE_FILE), String.class);
  }

  private void verifyResponse(ResponseEntity<String> response) {
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getHeaders().get(CONTENT_TYPE)).isNotNull();
  }

  private Date getDate(String sampleDate) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      return dateFormat.parse(sampleDate);
    } catch (ParseException e) {
      return new Date();
    }
  }

  private HttpEntity<TransactionMessage> buildTransactionMsgRequest(String requestBody)
      throws IOException {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(singletonList(APPLICATION_JSON));
    TransactionMessage requestJson = stringToJson(getFileContent(requestBody),
        TransactionMessage.class);
    return new HttpEntity<>(requestJson, headers);
  }
}

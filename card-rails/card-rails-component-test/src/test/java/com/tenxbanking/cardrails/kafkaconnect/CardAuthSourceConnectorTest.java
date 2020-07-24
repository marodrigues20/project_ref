package com.tenxbanking.cardrails.kafkaconnect;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.tenxbanking.cardrails.stub.CardRailsKafkaConnectWiremockStubs.stubDeleteConnector;
import static com.tenxbanking.cardrails.stub.CardRailsKafkaConnectWiremockStubs.stubGetConnectorStatus;
import static com.tenxbanking.cardrails.stub.CardRailsKafkaConnectWiremockStubs.stubPostCreateConnector;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TWO_SECONDS;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model.ConnectorStatus;
import com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model.GetConnectorResponse;
import com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model.GetConnectorStatusResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class CardAuthSourceConnectorTest extends BaseComponentTest {

  private static final String CARD_AUTH_CONNECTOR_NAME = "card_auth_source_connector";
  private static final Map<String, Object> CARD_AUTH_CONNECTOR_CONFIG = getExpectedConnectorConfig();

  @Before
  public void before() {
    WireMock.reset();
  }

  @Test
  public void shouldCreateConnectorWhenItIsNotRegistered() throws JsonProcessingException {

    stubDeleteConnector(CARD_AUTH_CONNECTOR_NAME);
    stubPostCreateConnector(CARD_AUTH_CONNECTOR_CONFIG, true);

    WireMock.resetAllRequests();

    assertConnectorIsCreated();
  }

  @Test
  public void shouldCreateConnectorWhenItsNotRunning() throws JsonProcessingException {

    GetConnectorStatusResponse response = getConnectorStatusResponse(false);
    stubDeleteConnector(CARD_AUTH_CONNECTOR_NAME);
    stubGetConnectorStatus(CARD_AUTH_CONNECTOR_NAME, response);
    stubPostCreateConnector(getExpectedConnectorConfig(), true);

    WireMock.resetAllRequests();

    assertConnectorIsCreated();
  }

  private void assertConnectorIsCreated() {
    await().pollDelay(TWO_SECONDS).until(() -> {
      verify(deleteRequestedFor((urlEqualTo("/connectors/" + CARD_AUTH_CONNECTOR_NAME))));
      verify(postRequestedFor(urlEqualTo("/connectors"))
          .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
          .withRequestBody(
              equalToJson(new ObjectMapper().writeValueAsString(CARD_AUTH_CONNECTOR_CONFIG)))
      );
      return true;
    });
  }

  private GetConnectorStatusResponse getConnectorStatusResponse(boolean running) {

    return GetConnectorStatusResponse.builder()
        .name(CARD_AUTH_CONNECTOR_NAME)
        .connector(ConnectorStatus.builder()
            .state(running ? "RUNNING" : "OTHER_VALUE")
            .build())
        .build();
  }

  private GetConnectorResponse getDummyGetConnectorResponse() {

    Map<String, Object> config = new HashMap<>();
    config.put("name", CARD_AUTH_CONNECTOR_NAME);
    config.put("prop1", "prop2");

    return GetConnectorResponse.builder()
        .name(CARD_AUTH_CONNECTOR_NAME)
        .config(config)
        .build();
  }

  private static Map<String, Object> getExpectedConnectorConfig() {

    final Map<String, Object> config = new HashMap<>();
    config.put("topic.prefix", "payment-messages-topic");
    config.put("tasks.max", 1);
    config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
    config
        .put("connection.url",
            "jdbc:postgresql://cockroach.db.svc.cluster.local:26257/" + DATABASE_NAME + "?"
                + "sslmode=verify-ca&sslcert=/cockroach-certs/client." + DATABASE_USER + ".crt"
                + "&sslkey=/cockroach-certs/client." + DATABASE_USER
                + ".pk8&sslrootcert=/cockroach-certs/ca.crt");
    config.put("connection.user", DATABASE_USER);
    config.put("dialect.name", "PostgreSqlDatabaseDialect");
    config.put("mode", "timestamp");
    config.put("timestamp.column.name", "created_date");
    config.put("query",
        "select card_transaction.subscription_key::string as subscription_key"
            + ", card_transaction.created_date as created_date"
            + ", transaction_message.transaction_message as transaction_message "
            + "from card_transaction join transaction_message on card_transaction.id = transaction_message.card_transaction_id");
    config.put("poll.interval.ms", 250);
    config.put("timestamp.delay.interval.ms", 250);
    config.put("key.converter", "org.apache.kafka.connect.storage.StringConverter");
    config.put("value.converter.schemas.enable", "false");
    config.put("value.converter", "org.apache.kafka.connect.storage.StringConverter");
    config.put("transforms", "extractKey,extractValue");
    config
        .put("transforms.extractKey.type", "org.apache.kafka.connect.transforms.ExtractField$Key");
    config.put("transforms.extractKey.field", "subscription_key");
    config.put("transforms.extractValue.type",
        "org.apache.kafka.connect.transforms.ExtractField$Value");
    config.put("transforms.extractValue.field", "transaction_message");
    config.put("errors.log.enable", "true");
    config.put("errors.log.include.messages", "true");

    final Map<String, Object> sourceConnector = new HashMap<>();
    sourceConnector.put("name", CARD_AUTH_CONNECTOR_NAME);
    sourceConnector.put("config", config);
    return sourceConnector;
  }
}

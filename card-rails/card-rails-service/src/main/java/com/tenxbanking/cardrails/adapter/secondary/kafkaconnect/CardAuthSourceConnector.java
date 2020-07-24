package com.tenxbanking.cardrails.adapter.secondary.kafkaconnect;

import com.google.common.collect.ImmutableMap;
import com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model.GetConnectorStatusResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Here are a few important notes.
 * <ul>
 *   <li>changing the connector name will republish all messaged stored in card_auth table - be very
 * careful with changing it!</li>
 *   <li>The definition of the connector is hard-coded here temporarily because there are lots of
 * issues defining the connector via terraform - Sam is investigating another solution for this
 * problem.</li>
 *   <li>This approach does not work on a blue/green deployment if the config changes - so be very
 * careful with changing it!</li>
 * </ul>
 */
@Slf4j
@Component
public class CardAuthSourceConnector {

  private static final String CARD_AUTH_CONNECTOR_NAME = "card_auth_source_connector";
  private static final String RUNNING_STATE = "RUNNING";

  private final Map<String, Object> cardAuthConnectorConfig;
  private final CardRailsKafkaConnectClient cardRailsKafkaConnectClient;

  public CardAuthSourceConnector(
      @NonNull final CardRailsKafkaConnectClient cardRailsKafkaConnectClient,
      @Value("#{environment.dbUsername}") @NonNull final String databaseUser,
      @Value("#{environment.database}") @NonNull final String databaseName) {

    this.cardRailsKafkaConnectClient = cardRailsKafkaConnectClient;
    this.cardAuthConnectorConfig = getConnectorConfig(databaseName, databaseUser);
  }

  @Scheduled(cron = "${cardAuthSourceConnector.cronExpression}")
  public void verifyCardAuthConnector() {

    if (!isConnectorRunning()) {
      deleteConnector();
      registerConnector();
    }
  }

  private void registerConnector() {

    try {
      cardRailsKafkaConnectClient.createConnector(cardAuthConnectorConfig);
      log.info("Registered connector {}", CARD_AUTH_CONNECTOR_NAME);
    } catch (Exception e) {
      log.warn("Exception registering connector {}. Exception message: {}",
          CARD_AUTH_CONNECTOR_NAME,
          e.getMessage());
    }
  }

  private void deleteConnector() {
    try {
      cardRailsKafkaConnectClient.deleteConnector(CARD_AUTH_CONNECTOR_NAME);
      log.info("Deleted connector {}", CARD_AUTH_CONNECTOR_NAME);
    } catch (Exception e) {
      log.warn("Exception deleting connector {}. Exception message: {}",
          CARD_AUTH_CONNECTOR_NAME,
          e.getMessage());
    }
  }

  private boolean isConnectorRunning() {
    return getConnectorStatus()
        .map(response -> response.getConnector().getState().equalsIgnoreCase(RUNNING_STATE))
        .orElse(false);
  }

  private Optional<GetConnectorStatusResponse> getConnectorStatus() {

    try {
      final ResponseEntity<GetConnectorStatusResponse> response = cardRailsKafkaConnectClient
          .getConnectorStatus(CARD_AUTH_CONNECTOR_NAME);
      return Optional.ofNullable(response.getBody());
    } catch (Exception e) {
      log.warn("Exception getting connector status {}. Exception message: {}",
          CARD_AUTH_CONNECTOR_NAME, e.getMessage());
      return Optional.empty();
    }
  }

  private static Map<String, Object> getConnectorConfig(@NonNull final String databaseName,
      @NonNull final String databaseUser) {

    final Map<String, Object> config = new HashMap<>();
    config.put("tasks.max", 1);
    config.put("connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector");
    config.put("connection.url", "jdbc:postgresql://cockroach.db.svc.cluster.local:26257/"
        + databaseName
        + "?sslmode=verify-ca&sslcert=/cockroach-certs/client." + databaseUser + ".crt"
        + "&sslkey=/cockroach-certs/client." + databaseUser
        + ".pk8&sslrootcert=/cockroach-certs/ca.crt");

    config.put("connection.user", databaseUser);
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
    config.put("topic.prefix", "payment-messages-topic");

    final Map<String, Object> sourceConnector = new HashMap<>();
    sourceConnector.put("name", CARD_AUTH_CONNECTOR_NAME);
    sourceConnector.put("config", ImmutableMap.copyOf(config));
    return ImmutableMap.copyOf(sourceConnector);
  }
}

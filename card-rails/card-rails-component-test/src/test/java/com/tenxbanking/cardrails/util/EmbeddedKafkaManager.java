package com.tenxbanking.cardrails.util;

import static java.lang.String.format;

import io.confluent.kafka.schemaregistry.RestApp;
import java.util.Properties;
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;

/**
 * A auxiliary class to start embedded kafka and embedded schema registry just once. Reason: starting/stopping an
 * embedded kafka is quite slow.
 */
public class EmbeddedKafkaManager {

  private static final int SCHEMA_REGISTRY_PORT = 9876;
  private static final EmbeddedKafkaManager INSTANCE = new EmbeddedKafkaManager();

  private EmbeddedKafkaCluster embeddedKafkaCluster;
  private RestApp schemaRegistry;

  private EmbeddedKafkaManager() {
    startEmbeddedKafkaCluster();
    startSchemaRegistry();
    addShutdownHook();
  }

  public static EmbeddedKafkaManager getInstance() {
    return INSTANCE;
  }

  public String bootstrapServers() {
    return embeddedKafkaCluster.bootstrapServers();
  }

  public String schemaRegistryUrl() {
    return format("http://127.0.0.1:%s", SCHEMA_REGISTRY_PORT);
  }

  private void startEmbeddedKafkaCluster() {
    try {
      embeddedKafkaCluster = new EmbeddedKafkaCluster(1, brokerProperties());
      embeddedKafkaCluster.start();
    } catch (Exception e) {
      exit(e);
    }
  }

  private void startSchemaRegistry() {
    try {
      schemaRegistry = new RestApp(SCHEMA_REGISTRY_PORT, embeddedKafkaCluster.zKConnectString(),
          "_schemas");
      schemaRegistry.start();
    } catch (Exception e) {
      exit(e);
    }
  }

  private void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          schemaRegistry.stop();
        } catch (Exception e) {
          exit(e);
        }
      }
    });
  }

  private Properties brokerProperties() {
    Properties props = new Properties();
    props.put("transaction.state.log.replication.factor", Short.valueOf("1"));
    props.put("transaction.state.log.min.isr", 1);
    return props;
  }

  private void exit(Exception e) {
    e.printStackTrace();
    System.exit(1);
  }
}

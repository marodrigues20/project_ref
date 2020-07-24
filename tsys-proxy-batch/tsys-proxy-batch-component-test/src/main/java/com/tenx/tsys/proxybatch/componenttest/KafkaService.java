package com.tenx.tsys.proxybatch.componenttest;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ISOLATION_LEVEL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringDeserializer;

class KafkaService {

  private final String bootstrapServers;
  private final String clientId;

  private Producer<String, String> producer;
  private Map<String, Consumer<String, String>> consumersMap = new HashMap<>();

  KafkaService(String bootstrapServers, String clientId) {
    this.bootstrapServers = bootstrapServers;
    this.clientId = clientId;
  }

  public void createConsumer(String... topics) {
    for (String topic : topics) {
      Consumer<String, String> consumer = new KafkaConsumer<>(consumerProperties());
      consumer.subscribe(Collections.singletonList(topic));
      consumersMap.put(topic, consumer);
    }
  }

  public void destroy() {
    consumersMap.forEach((topic, consumer) -> {
      if (consumer != null) {
        consumer.close(5, SECONDS);
      }
    });

  }

  public ConsumerRecords<String, String> consume(String topic) {
    Consumer<String, String> consumer = consumersMap.get(topic);
    ConsumerRecords<String, String> records = consumer.poll(1000);
    if (!records.isEmpty()) {
      consumer.commitSync();
    }
    return records;
  }

  private Properties consumerProperties() {
    Properties properties = new Properties();
    properties.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
    properties.put(SASL_MECHANISM, "PLAIN");
    properties.put(SASL_JAAS_CONFIG, String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";", "apiKey", "apiPassword"));
    properties.put(SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");

    properties.put(REQUEST_TIMEOUT_MS_CONFIG, "20000");
    properties.put(RETRY_BACKOFF_MS_CONFIG, "500");

    properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(GROUP_ID_CONFIG, randomUUID().toString());
    properties.put(CLIENT_ID_CONFIG, clientId + randomUUID());
    properties.put(ISOLATION_LEVEL_CONFIG, "read_committed");

    properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ENABLE_AUTO_COMMIT_CONFIG, false);
    properties.put(MAX_POLL_RECORDS_CONFIG, 5);

    return properties;
  }
}

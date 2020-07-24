package com.tenx.universalbanking.transactionmanager.componenttest;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ISOLATION_LEVEL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

class KafkaService {

  private final String bootstrapServers;
  private final String clientId;

  private Producer<String, String> producer;
  private Map<String, Consumer<String, String>> consumersMap = new HashMap<>();

  KafkaService(String bootstrapServers, String clientId) {
    this.bootstrapServers = bootstrapServers;
    this.clientId = clientId;
  }

  public void createProducer() {
    this.producer = new KafkaProducer<>(producerProperties());
  }

  public void createConsumer(String... topics) {
    for (String topic : topics) {
      Consumer<String, String> consumer = new KafkaConsumer<>(consumerProperties());
      consumer.subscribe(Collections.singletonList(topic));
      consumersMap.put(topic, consumer);
    }
  }

  public void destroy() {
    if (producer != null) {
      producer.close(5, SECONDS);
    }
    consumersMap.forEach((topic, consumer) -> {
      if (consumer != null) {
        consumer.close(5, SECONDS);
      }
    });

  }

  public void sendSync(String topic, String message) throws Exception {
    ProducerRecord<String, String> msgToSend = new ProducerRecord<>(topic, message);
    producer.send(msgToSend).get();
  }

  public ConsumerRecords<String, String> consume(String topic) {
    Consumer<String, String> consumer = consumersMap.get(topic);
    ConsumerRecords<String, String> records = consumer.poll(500);
    if (!records.isEmpty()) {
      consumer.commitSync();
    }
    return records;
  }

  private Properties producerProperties() {
    Properties properties = new Properties();
    properties.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(ACKS_CONFIG, "all");
    properties.put(RETRIES_CONFIG, 0);
    properties.put("min.insync.replicas", "1");
    properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(CLIENT_ID_CONFIG, clientId + randomUUID());
    return properties;
  }

  private Properties consumerProperties() {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(GROUP_ID_CONFIG, randomUUID().toString());
    properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(CLIENT_ID_CONFIG, clientId + randomUUID());
    properties.put(ISOLATION_LEVEL_CONFIG, "read_committed");
    return properties;
  }
}

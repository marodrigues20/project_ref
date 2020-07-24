package com.tenxbanking.cardrails.util;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.hamcrest.core.IsNull.notNullValue;

import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

public class KafkaService {

  private static final String CONSUMER_GROUP_ID = "card-rails-consumer-group";
  private static final String CONSUMER_CLIENT_ID = "card-rails-consumer";
  private static final String PRODUCER_CLIENT_ID = "card-rails-producer";

  private final String bootstrapServers;
  private final String schemaRegistryUrl;

  private KafkaConsumer kafkaConsumer;
  private KafkaTemplate avroBasedTemplate;
  private KafkaTemplate stringBasedTemplate;

  public KafkaService(final String bootstrapServers, final String schemaRegistryUrl) {
    this.bootstrapServers = bootstrapServers;
    this.schemaRegistryUrl = schemaRegistryUrl;
    this.createAvroBasedTemplate();
    this.createStringBasedTemplate();
  }

  public void createConsumer(String topicName) {
    kafkaConsumer = new KafkaConsumer(consumerProperties());
    kafkaConsumer.subscribe(singletonList(topicName));
  }

  public void createConsumer(List<String> topicNames) {
    kafkaConsumer = new KafkaConsumer(consumerProperties());
    kafkaConsumer.subscribe(topicNames);
  }

  public void closeConsumer() {
    if (kafkaConsumer != null) {
      kafkaConsumer.unsubscribe();
      kafkaConsumer.close();
      kafkaConsumer = null;
    }
  }

  public <T> ConsumerRecords<String, T> consume() {
    ConsumerRecords<String, T> records = kafkaConsumer.poll(ofMillis(250));
    kafkaConsumer.commitSync();
    return records;
  }

  public <T> T getRecordForKey(String key) {
    return getRecordForKey(key, null);
  }

  public <T> T getRecordForKey(String key, String topicName) {

    //reset the offset so that it reads the events from beginning
    kafkaConsumer.seekToBeginning(kafkaConsumer.assignment());

    return await()
        .atMost(TEN_SECONDS)
        .until(() -> findRecordForKey(key, topicName), notNullValue());
  }

  public void sendMessageWithSchema(final String topicName, final String key,
      final SubscriptionEvent value) {
    try {
      avroBasedTemplate.send(topicName, key, value).get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void sendStringMessage(final String topicName, final String key, final String value) {
    try {
      stringBasedTemplate.send(topicName, value).get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private <T> T findRecordForKey(String key, String topicName) {
    ConsumerRecords<String, T> records = consume();

    Iterator<ConsumerRecord<String, T>> iterator = (nonNull(topicName)
        ? records.records(topicName)
        : records)
        .iterator();

    while (iterator.hasNext()) {
      ConsumerRecord<String, T> record = iterator.next();
      if (record.key().equals(key)) {
        return record.value();
      }
    }

    return null;
  }

  private void createAvroBasedTemplate() {
    avroBasedTemplate = new KafkaTemplate<>(avroProducerFactoryConfig());
  }

  private void createStringBasedTemplate() {
    stringBasedTemplate = new KafkaTemplate<>(stringProducerFactoryConfig());
  }

  private Map<String, Object> defaultFactoryConfig() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(CLIENT_ID_CONFIG, PRODUCER_CLIENT_ID);
    configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return configProps;
  }

  private ProducerFactory<String, Serializable> avroProducerFactoryConfig() {
    Map<String, Object> configProps = defaultFactoryConfig();
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    configProps.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  private ProducerFactory<String, String> stringProducerFactoryConfig() {
    Map<String, Object> configProps = defaultFactoryConfig();
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  private Properties consumerProperties() {
    Properties properties = new Properties();
    properties.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    properties.put(GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
    properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(CLIENT_ID_CONFIG, CONSUMER_CLIENT_ID);
    properties.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    properties.put(SPECIFIC_AVRO_READER_CONFIG, true);
    return properties;
  }

}

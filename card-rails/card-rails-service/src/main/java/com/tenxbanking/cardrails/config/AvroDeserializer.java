package com.tenxbanking.cardrails.config;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvroDeserializer extends KafkaAvroDeserializer {

  public AvroDeserializer() {
    super();
  }

  public AvroDeserializer(SchemaRegistryClient client) {
    super(client);
  }

  public AvroDeserializer(SchemaRegistryClient client, Map<String, ?> props) {
    super(client, props);
  }

  @Override
  public Object deserialize(String topicName, byte[] bytes) {
    try {
      return super.deserialize(bytes);
    } catch (Exception e) {
      log.error("Exception deserializing message on the topic {}", topicName);
      return null;
    }
  }
}

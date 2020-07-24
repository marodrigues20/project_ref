package com.tenxbanking.cardrails.config;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.RETRY_BACKOFF_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE;

import avro.shaded.com.google.common.collect.ImmutableMap;
import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@EnableKafka
public class KafkaConfig {

  private static final long A_DAY_MILLIS = 24 * 60 * 60 * 1000;

  private static final String SASL_JAAS_CONFIG_TEMPLATE =
      "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";

  private final Map<String, Object> kafkaProperties;

  //TODO: this could all be extracted into application.yml
  public KafkaConfig(
      @Value("${kafka.ssl.algorithm}") String sslAlgorithm,
      @Value("${kafka.sasl.mechanism}") String saslMechanism,
      @Value("${kafka.sasl.api.key}") String saslApiKey,
      @Value("${kafka.sasl.api.secret}") String saslApiSecret,
      @Value("${kafka.request.timeout.ms}") String requestTimeoutMs,
      @Value("${kafka.retry.backoff.ms}") String retryBackoffMs,
      @Value("${kafka.security.protocol}") String securityProtocol,
      @Value("${kafka.schema.registry.url}") String schemaRegistryUrl,
      @Value("${kafka.bootstrap.servers}") String bootstrapServers) {

    kafkaProperties = new ImmutableMap.Builder<String, Object>()
        .put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        .put(REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs)
        .put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, sslAlgorithm)
        .put(SASL_MECHANISM, saslMechanism)
        .put(SASL_JAAS_CONFIG, String.format(SASL_JAAS_CONFIG_TEMPLATE, saslApiKey, saslApiSecret))
        .put(SECURITY_PROTOCOL_CONFIG, securityProtocol)
        .put(RETRY_BACKOFF_MS_CONFIG, retryBackoffMs)
        .put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
        .build();
  }

  private Map<String, Object> consumerProperties() {
    return new ImmutableMap.Builder<String, Object>()
        .put(SPECIFIC_AVRO_READER_CONFIG, true)
        .put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        .put(VALUE_DESERIALIZER_CLASS_CONFIG, AvroDeserializer.class)
        .put(ENABLE_AUTO_COMMIT_CONFIG, false)
        .put(AUTO_OFFSET_RESET_CONFIG, "latest")
        .put(CONNECTIONS_MAX_IDLE_MS_CONFIG, A_DAY_MILLIS)
        .putAll(kafkaProperties)
        .build();
  }

  @Bean("subscriptionEventContainerFactory")
  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, SubscriptionEvent>> subscriptionEventContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, SubscriptionEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProperties()));
    factory.getContainerProperties().setAckMode(MANUAL_IMMEDIATE);
    factory.getContainerProperties().setSyncCommits(true);
    return factory;
  }

}

package com.tenx.universalbanking.transactionmanager.messagingservice.producer;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import com.tenx.universalbanking.transactionmanager.messagingservice.common.CommonConfig;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProducerConfig {

  private static final String ACKS_ALL = "all";
  private static final String MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_ONE = "1";
  private static final String ENABLE_IDEMPOTENCE_TRUE = "true";

  private final String bootstrapServers;
  private final String apiKey;
  private final String apiPassword;
  private final String securityProtocol;
  private final String clientIdPrefix;

  ProducerConfig(@Value("${kafka.bootstrap.servers}") String bootstrapServers,
      @Value("${kafka.api.key}") String apiKey,
      @Value("${kafka.api.password}") String apiPassword,
      @Value("${kafka.security.protocol}") String securityProtocol,
      @Value("${kafka.producer.client.id.prefix}") String clientIdPrefix) {
    this.bootstrapServers = bootstrapServers;
    this.apiKey = apiKey;
    this.apiPassword = apiPassword;
    this.securityProtocol = securityProtocol;
    this.clientIdPrefix = clientIdPrefix;
  }

  public Properties producerConfig() {
    Properties props = CommonConfig.commonConfig(bootstrapServers, apiKey, apiPassword, securityProtocol, clientIdPrefix);
    props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ENABLE_IDEMPOTENCE_CONFIG, ENABLE_IDEMPOTENCE_TRUE);
    props.put(ACKS_CONFIG, ACKS_ALL);
    props.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_ONE);
    props.put(RETRIES_CONFIG, Integer.MAX_VALUE);

    return props;
  }
}

package com.tenx.tsys.proxybatch.service;

import static java.lang.String.format;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.TRANSACTIONAL_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Value;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ProducerConfig.class})
public class ProducerConfigTest {

  private final String BOOTSTRAP_SERVERS = "bootstrapServers";
  private final String CLIENT_ID_PREFIX = "clientIdPrefix";
  private final String KAFKA_API_KEY = "kafkaApiKey";
  private final String KAFKA_SECURITY_PROTOCOL = "kafkaSecurityProtocol";
  @Value("${kafka.api.password}")
  private String KAFKA_API_PASSWORD;

  @Test
  public void nonTransactionalProducerConfig_shouldReturnExpectedProperties() {
    //given
    mockStatic(UUID.class);
    given(UUID.randomUUID()).willReturn(mock(UUID.class));
    // when
    Properties actualProperties = aNewProducerConfig().nonTransactionalProducerConfig();
    // then
    assertThat(actualProperties).isEqualTo(buildExpectedProperties(false));
  }

  private Properties buildExpectedProperties(boolean transactional) {
    Properties props = new Properties();
    String uniqueClientId = format("%s-%s", CLIENT_ID_PREFIX,
        "00000000-0000-0000-0000-000000000000");
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
    props.put(SASL_MECHANISM, "PLAIN");
    props.put(SASL_JAAS_CONFIG, String.format(
        "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
        KAFKA_API_KEY, KAFKA_API_PASSWORD));
    props.put(SECURITY_PROTOCOL_CONFIG, KAFKA_SECURITY_PROTOCOL);

    props.put(REQUEST_TIMEOUT_MS_CONFIG, "20000");
    props.put(RETRY_BACKOFF_MS_CONFIG, "500");
    props.put(CLIENT_ID_CONFIG, format("%s-%s", CLIENT_ID_PREFIX, UUID.randomUUID()));

    props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    props.put(ENABLE_IDEMPOTENCE_CONFIG, "true");
    props.put(ACKS_CONFIG, "all");
    props.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
    props.put(org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG, uniqueClientId);
    props.put(RETRIES_CONFIG, Integer.MAX_VALUE);

    if (transactional) {
      props.put(TRANSACTIONAL_ID_CONFIG, uniqueClientId);
    }
    return props;


  }

  private ProducerConfig aNewProducerConfig() {
    return new ProducerConfig(BOOTSTRAP_SERVERS, KAFKA_API_KEY, KAFKA_API_PASSWORD,
        KAFKA_SECURITY_PROTOCOL, CLIENT_ID_PREFIX);
  }
}

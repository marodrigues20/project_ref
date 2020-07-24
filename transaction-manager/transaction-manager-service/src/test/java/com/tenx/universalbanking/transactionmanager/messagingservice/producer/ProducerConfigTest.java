package com.tenx.universalbanking.transactionmanager.messagingservice.producer;

import static java.lang.String.format;
import static org.apache.kafka.clients.admin.AdminClientConfig.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.RETRY_BACKOFF_MS_CONFIG;
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

import com.tenx.universalbanking.transactionmanager.messagingservice.common.CommonConfig;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommonConfig.class)
public class ProducerConfigTest {

  private static final String BOOTSTRAP_SERVERS = "bootstrapServers";
  private static final String API_KEY = "apiKey";
  private static final String API_PASSWORD = "apiPassword";
  private static final String SECURITY_PROTOCOL = "SASL_SSL";
  private static final String CLIENT_ID_PREFIX = "clientIdPrefix";

  @Test
  public void producerConfig_shouldReturnExpectedProperties() {
    //given
    mockStatic(UUID.class);
    given(UUID.randomUUID()).willReturn(mock(UUID.class));
    // when
    Properties actualProperties = aNewProducerConfig().producerConfig();
    // then
    assertThat(actualProperties).isEqualTo(buildExpectedProperties());
  }

  private Properties buildExpectedProperties() {
    Properties props = new Properties();
    String uniqueClientId = format("%s-%s", CLIENT_ID_PREFIX,
        "00000000-0000-0000-0000-000000000000");
    props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
    props.put(SASL_MECHANISM, "PLAIN");
    props.put(REQUEST_TIMEOUT_MS_CONFIG, "20000");
    props.put(RETRY_BACKOFF_MS_CONFIG, "500");
    props.put(SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"apiKey\" password=\"apiPassword\";");
    props.put(SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
    props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ENABLE_IDEMPOTENCE_CONFIG, "true");
    props.put(ACKS_CONFIG, "all");
    props.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
    props.put(org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG, uniqueClientId);
    props.put(RETRIES_CONFIG, Integer.MAX_VALUE);
    if (false) {
      props.put(TRANSACTIONAL_ID_CONFIG, uniqueClientId);
    }
    return props;
  }

  private ProducerConfig aNewProducerConfig() {
    return new ProducerConfig(BOOTSTRAP_SERVERS, API_KEY, API_PASSWORD, SECURITY_PROTOCOL, CLIENT_ID_PREFIX);
  }
}

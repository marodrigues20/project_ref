package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.admin.AdminClientConfig.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ISOLATION_LEVEL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.RETRY_BACKOFF_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.tenx.universalbanking.transactionmanager.messagingservice.producer.ProducerConfig;
import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;

public abstract class ConsumerConfigTest {

  static final String BOOTSTRAP_SERVERS = "bootstrapServers";
  static final String API_KEY = "apiKey";
  static final String API_PASSWORD = "apiPassword";
  static final String SECURITY_PROTOCOL = "SASL_SSL";
  static final String CLIENT_ID_PREFIX = "clientIdPrefix";
  static final String GROUP_ID = "groupId";
  static final String TOPIC_NAME = "topic1,topic2";
  static final Integer NUMBER_CONSUMER_THREADS = 3;
  static final Integer POLL_TIMEOUT_MS = 100;
  static final Integer POLL_MAX_RECORDS = 2;

  @Test
  public void testDefaultConfig() {
    //given
    mockStatic(UUID.class);
    given(UUID.randomUUID()).willReturn(mock(UUID.class));

    Properties expected = buildExpectedConsumerProperties();

    //  when
    Properties actual = buildConsumerConfig().consumerConfig();

    //  then
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testTopicName() {
    assertThat(buildConsumerConfig().getTopics()).isEqualTo(asList(TOPIC_NAME.split(",")));
  }

  @Test
  public void testPollTimeoutMs() {
    assertThat(buildConsumerConfig().getPollTimeoutMs()).isEqualTo(POLL_TIMEOUT_MS);
  }

  @Test
  public void getConsumerThreadCountTest(){
    assertThat(buildConsumerConfig().getConsumerThreadCount()).isEqualTo(NUMBER_CONSUMER_THREADS);
  }

  @Test
  public void testGroupId() {
    assertThat(buildConsumerConfig().getGroupId()).isEqualTo(GROUP_ID);
  }

  abstract ConsumerConfig buildConsumerConfig();

  private static Properties buildExpectedConsumerProperties() {
    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
    props.put(SASL_MECHANISM, "PLAIN");
    props.put(REQUEST_TIMEOUT_MS_CONFIG, "20000");
    props.put(RETRY_BACKOFF_MS_CONFIG, "500");
    props.put(SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"apiKey\" password=\"apiPassword\";");
    props.put(SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
    props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(GROUP_ID_CONFIG, GROUP_ID);
    props.put(CLIENT_ID_CONFIG,
        format("%s-%s", CLIENT_ID_PREFIX, "00000000-0000-0000-0000-000000000000"));
    props.put(ISOLATION_LEVEL_CONFIG, "read_committed");
    props.put(ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(MAX_POLL_RECORDS_CONFIG, POLL_MAX_RECORDS);
    return props;
  }
}
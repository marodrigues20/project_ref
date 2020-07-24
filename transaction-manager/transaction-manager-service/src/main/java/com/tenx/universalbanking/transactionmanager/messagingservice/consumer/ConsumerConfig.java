package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.tenx.universalbanking.transactionmanager.messagingservice.common.CommonConfig.commonConfig;
import static java.util.Arrays.asList;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ISOLATION_LEVEL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import com.tenx.universalbanking.transactionmanager.messagingservice.producer.ProducerConfig;
import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.serialization.StringDeserializer;

abstract class ConsumerConfig {

  private final String bootstrapServers;
  private final String apiKey;
  private final String apiPassword;
  private final String securityProtocol;
  private final String clientIdPrefix;
  private final String groupId;
  private final List<String> topics;
  private final Integer consumerThreadCount;
  private final Integer pollTimeoutMs;
  private final Integer pollMaxRecords;

  ConsumerConfig(String bootstrapServers, String apiKey, String apiPassword, String securityProtocol, String clientIdPrefix,
      String groupId, String topics, Integer consumerThreadCount, Integer pollTimeoutMs, Integer pollMaxRecords) {
    this.bootstrapServers = bootstrapServers;
    this.apiKey = apiKey;
    this.apiPassword = apiPassword;
    this.securityProtocol = securityProtocol;
    this.clientIdPrefix = clientIdPrefix;
    this.groupId = groupId;
    this.topics = asList(topics.split(","));
    this.consumerThreadCount = consumerThreadCount;
    this.pollTimeoutMs = pollTimeoutMs;
    this.pollMaxRecords = pollMaxRecords;

    checkArgument(this.consumerThreadCount > 0, "consumer threads must be greater than 0");
    checkArgument(this.pollTimeoutMs > 0, "poll timeout must be greater than 0");
    checkArgument(this.pollMaxRecords > 0, "poll max records must be greater than 0");
  }

  public Properties consumerConfig() {
    Properties props = commonConfig(bootstrapServers, apiKey, apiPassword, securityProtocol, clientIdPrefix);
    props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(GROUP_ID_CONFIG, getGroupId());
    props.put(ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ISOLATION_LEVEL_CONFIG, "read_committed");
    props.put(MAX_POLL_RECORDS_CONFIG, pollMaxRecords);
    return props;
  }

  public List<String> getTopics() {
    return topics;
  }

  public String getGroupId() {
    return groupId;
  }

  public Integer getConsumerThreadCount() {
    return consumerThreadCount;
  }

  public Integer getPollTimeoutMs() {
    return pollTimeoutMs;
  }

  public abstract KafkaProcessorManager buildKafkaProcessorManager(ProducerConfig producerConfig,
                                                                   TransactionProcessor transactionProcessor);
}

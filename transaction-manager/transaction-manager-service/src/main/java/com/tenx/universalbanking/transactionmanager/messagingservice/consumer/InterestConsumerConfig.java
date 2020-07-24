package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static java.util.concurrent.Executors.newFixedThreadPool;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.tenx.universalbanking.transactionmanager.messagingservice.producer.ProducerConfig;
import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;

@Configuration
public class InterestConsumerConfig extends ConsumerConfig {

  InterestConsumerConfig(@Value("${kafka.bootstrap.servers}") String bootstrapServers,
      @Value("${kafka.api.key}") String apiKey,
      @Value("${kafka.api.password}") String apiPassword,
      @Value("${kafka.security.protocol}") String securityProtocol,
      @Value("${kafka.consumer.interest.client.id.prefix}") String clientIdPrefix,
      @Value("${kafka.consumer.interest.group.id}") String groupId,
      @Value("${kafka.consumer.interest.topics}") String topics,
      @Value("${kafka.consumer.interest.thread.count:1}") Integer consumerThreadCount,
      @Value("${kafka.consumer.interest.poll.timeout.ms:250}") Integer pollTimeoutMs,
      @Value("${kafka.consumer.interest.poll.max.records:5}") Integer pollMaxRecords) {

    super(bootstrapServers, apiKey, apiPassword, securityProtocol, clientIdPrefix, groupId, topics, consumerThreadCount, pollTimeoutMs,
        pollMaxRecords);
  }

  @Bean("InterestKafkaProcessorManager")
  public KafkaProcessorManager buildKafkaProcessorManager(ProducerConfig producerConfig,
                                                          TransactionProcessor transactionProcessor) {

    final ExecutorService consumerThreadPool = newFixedThreadPool(getConsumerThreadCount());
    return new KafkaProcessorManager(this, consumerThreadPool, transactionProcessor);
  }
}

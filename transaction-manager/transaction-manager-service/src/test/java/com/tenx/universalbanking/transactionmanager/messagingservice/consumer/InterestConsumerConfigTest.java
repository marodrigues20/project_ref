package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tenx.universalbanking.transactionmanager.messagingservice.common.CommonConfig;
import com.tenx.universalbanking.transactionmanager.messagingservice.producer.ProducerConfig;
import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonConfig.class, InterestConsumerConfig.class})
public class InterestConsumerConfigTest extends ConsumerConfigTest {

  @Mock
  private ProducerConfig producerConfig;

  @Mock
  private TransactionProcessor transactionProcessor;

  InterestConsumerConfig buildConsumerConfig() {
    return new InterestConsumerConfig(BOOTSTRAP_SERVERS, API_KEY, API_PASSWORD, SECURITY_PROTOCOL, CLIENT_ID_PREFIX,
        GROUP_ID, TOPIC_NAME, NUMBER_CONSUMER_THREADS, POLL_TIMEOUT_MS, POLL_MAX_RECORDS);
  }

  @Test
  public void buildKafkaProcessorManagerTest(){
    KafkaProcessorManager actual = buildConsumerConfig().buildKafkaProcessorManager(producerConfig, transactionProcessor);
    assertNotNull(actual);
  }
}
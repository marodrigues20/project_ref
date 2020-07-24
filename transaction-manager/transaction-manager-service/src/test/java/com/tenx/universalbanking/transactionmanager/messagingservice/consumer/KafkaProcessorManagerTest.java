package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class KafkaProcessorManagerTest {

  @Spy
  private ExecutorService consumerThreadPool;

  @Mock
  private ProcessorTask processorTask;

  @Mock
  private TransactionProcessor transactionProcessor;

  private List<ProcessorTask> consumerTasks = new ArrayList<ProcessorTask>();
  private ConsumerConfig consumerConfig;
  private KafkaProcessorManager kafkaProcessorManager;

  @BeforeEach
  public void setUp() {
    consumerConfig = new InterestConsumerConfig("localhost:9093", "apiKey", "apiPassword",
        "PLAINTEXT", "tm-interest-consumer", "tm-interest-group",
        "transaction-interest-commands-topic", 1, 1, 5);
    consumerTasks.add(processorTask);
    kafkaProcessorManager = new KafkaProcessorManager(consumerConfig, consumerThreadPool,
        transactionProcessor);
    ReflectionTestUtils.setField(kafkaProcessorManager, "consumerTasks", consumerTasks);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void initTest() {
    doNothing().when(consumerThreadPool).execute(any(ProcessorTask.class));
    kafkaProcessorManager.init();
    assertEquals(2, consumerTasks.size());
    verify(consumerThreadPool).execute(any(ProcessorTask.class));
  }

  @Test
  public void destroyTest() throws InterruptedException {
    doNothing().when(consumerThreadPool).shutdown();
    when(consumerThreadPool.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);
    kafkaProcessorManager.destroy();
    verify(consumerThreadPool).shutdown();
    verify(consumerThreadPool).awaitTermination(1, TimeUnit.MINUTES);
  }
}
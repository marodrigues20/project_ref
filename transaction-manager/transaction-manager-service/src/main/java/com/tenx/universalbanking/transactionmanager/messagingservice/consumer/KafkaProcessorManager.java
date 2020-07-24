package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;

public class KafkaProcessorManager {

  private final Logger logger = getLogger(getClass());

  private final ConsumerConfig consumerConfig;
  private final ExecutorService consumerThreadPool;
  private final TransactionProcessor transactionProcessor;
  private final List<ProcessorTask> consumerTasks;

  public KafkaProcessorManager(ConsumerConfig consumerConfig, ExecutorService consumerThreadPool,
      TransactionProcessor transactionProcessor) {

    this.consumerTasks = new LinkedList<>();
    this.consumerConfig = consumerConfig;
    this.consumerThreadPool = consumerThreadPool;
    this.transactionProcessor = transactionProcessor;
  }

  @PostConstruct
  public void init() {
    for (int count = 0; count < consumerConfig.getConsumerThreadCount(); count++) {
      ProcessorTask task = new ProcessorTask(consumerConfig, transactionProcessor);
      consumerTasks.add(task);
      consumerThreadPool.execute(task);
    }
  }

  @PreDestroy
  public void destroy() {
    logger.debug("Terminating processor tasks...");
    consumerTasks.forEach(ProcessorTask::cleanup);
    consumerThreadPool.shutdown();
    try {
      logger.debug("Waiting for processor tasks to stop...");
      consumerThreadPool.awaitTermination(1, TimeUnit.MINUTES);
      logger.debug("Processor tasks stopped gracefully.");
    } catch (InterruptedException e) {
      logger.error("Exception while waiting for processor tasks to shutdown gracefully.", e);
      consumerThreadPool.shutdownNow();
    }
  }
}

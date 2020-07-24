package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;

class ProcessorTask implements Runnable {

  private final Logger logger = getLogger(getClass());

  private final Integer pollTimeout;
  private final List<String> topics;
  private final KafkaConsumer<String, String> kafkaConsumer;
  private final RecordProcessor recordProcessor;
  private final AtomicBoolean running = new AtomicBoolean(TRUE);

  public ProcessorTask(ConsumerConfig consumerConfig, TransactionProcessor transactionProcessor) {

    this.pollTimeout = consumerConfig.getPollTimeoutMs();
    this.topics = consumerConfig.getTopics();

    this.kafkaConsumer = new KafkaConsumer<>(consumerConfig.consumerConfig());

    this.recordProcessor = new RecordProcessor(kafkaConsumer, transactionProcessor);
  }

  @Override
  public void run() {
    kafkaConsumer.subscribe(topics, new RebalanceListener());
    logger.debug("Starting to consume messages from topics {}...", topics);
    try {
      while (running.get()) {
        pollAndProcessOnce();
      }
    } catch (WakeupException e) {
      logger.debug("Processor task received wakeup signal.");
    } catch (Exception e) {
      logger.error("Processor task suffered from an unrecoverable error!", e);
    } finally {
      closeConsumer();
    }
  }

  private void pollAndProcessOnce() {
    ConsumerRecords<String, String> records = kafkaConsumer.poll(pollTimeout);
    if (!records.isEmpty()) {
      logger.debug("Poll returned {} records.", records.count());
    }
    records.forEach(this::processOnce);
  }

  private void processOnce(ConsumerRecord<String, String> record) {
    if (!running.get()) {
      logger.debug("Processor task marked as not running. Going to throw a WakeupException!");
      throw new WakeupException();
    }
    recordProcessor.process(record);
  }

  private void closeConsumer() {
    logger.debug("Closing Kafka consumer...");
    try {
      kafkaConsumer.close();
      logger.debug("Kafka consumer closed successfully.");
    } catch (RuntimeException e) {
      logger.error("Exception while closing Kafka consumer.", e);
    }
  }

  public void cleanup() {
    running.set(FALSE);
    logger.debug("Processor task marked as not running.");
  }
}

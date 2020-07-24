package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;

class RecordProcessor {

  private final Logger logger = getLogger(getClass());

  private final KafkaConsumer kafkaConsumer;
  private final TransactionProcessor transactionProcessor;

  RecordProcessor(KafkaConsumer kafkaConsumer, TransactionProcessor transactionProcessor) {
    this.kafkaConsumer = kafkaConsumer;
    this.transactionProcessor = transactionProcessor;
  }

  public void process(ConsumerRecord<String, String> record) {

    logger.debug("Processing record with key {}", record.key());

    TransactionMessage txnMessage = stringToJson(record.value(), TransactionMessage.class);

    process(txnMessage, record);
  }

  private void process(TransactionMessage transactionMessage, ConsumerRecord<String, String> record) {
    ConsumerRecordState consumerRecordState = new ConsumerRecordState(record);

    if (transactionMessage != null) {
      try {
        transactionProcessor.handle(transactionMessage);
      } catch (RuntimeException e) {
        logger.error("Exception processing record with key {}.", record.key(), e);
      }
    } else {
      logger.error("Error processing record with key {}. Its value resulted in a null message!",
          record.key());
    }

    commitOffset(consumerRecordState);

    logger.debug("Done processing record with key {}.", record.key());
  }

  /**
   * If the consumer offset is not yet committed, commit it here.
   *
   * Examples:
   *
   * a) Fees & Charges flow
   *
   *
   * b) Interest flow
   *
   * c) Rejection scenario on all flows These scenarios don't produce any output to kafka. As consequence, the offset of
   * the consumer is not committed.
   *
   * Hence, it needs to be done here.
   */
  private void commitOffset(ConsumerRecordState consumerRecordState) {

    if (ConsumerRecordState.CommitState.UNCOMMITTED == consumerRecordState.getCommitState()) {
      Map<TopicPartition, OffsetAndMetadata> offsets = consumerRecordState.nextOffset();
      kafkaConsumer.commitSync(offsets);
      logger.debug("Committed the offsets: {}", offsets);
    }
  }

}
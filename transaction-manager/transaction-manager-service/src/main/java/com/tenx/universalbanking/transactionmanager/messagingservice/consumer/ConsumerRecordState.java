package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConsumerRecordState {

  public enum CommitState {COMMITTED, UNCOMMITTED}

  private final ConsumerRecord<String, String> record;
  private CommitState commitState = CommitState.UNCOMMITTED;

  ConsumerRecordState(ConsumerRecord<String, String> record) {
    checkNotNull(record, "record cannot be null");
    this.record = record;
  }

  public ConsumerRecord<String, String> getRecord() {
    return record;
  }

  public CommitState getCommitState() {
    return commitState;
  }

  public void commit() {
    this.commitState = CommitState.COMMITTED;
  }

  public Map<TopicPartition, OffsetAndMetadata> nextOffset() {
    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    TopicPartition key = new TopicPartition(record.topic(), record.partition());
    OffsetAndMetadata value = new OffsetAndMetadata(record.offset() + 1);
    offsets.put(key, value);
    return offsets;
  }
}

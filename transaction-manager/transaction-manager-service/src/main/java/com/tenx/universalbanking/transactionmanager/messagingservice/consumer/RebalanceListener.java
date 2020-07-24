package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

class RebalanceListener implements ConsumerRebalanceListener {

  private final Logger logger = getLogger(getClass());

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    if (!partitions.isEmpty()) {
      logger.debug("Partitions revoked: {}", partitions);
    }
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    if (!partitions.isEmpty()) {
      logger.debug("Partitions assigned: {}", partitions);
    }
  }
}

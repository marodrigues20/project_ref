package com.tenxbanking.cardrails.adapter.primary.consumer;

import static java.util.Objects.isNull;

import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SubscriptionEventListener {

  private static final String GROUP = "card-rails-group";
  private static final String TOPIC = "${kafka.consumer.topic.subscription}";

  private SubscriptionEventHandler subscriptionEventHandler;

  @KafkaListener(topics = TOPIC,
      containerFactory = "subscriptionEventContainerFactory",
      groupId = GROUP,
      autoStartup = "false")
  public void onMessage(final @NonNull ConsumerRecord<String, SubscriptionEvent> record,
      final Acknowledgment acknowledgment) {

    log.debug("Received subscription event with key {}", record.key());

    final SubscriptionEvent subscriptionEvent = record.value();
    if (isNull(subscriptionEvent)) {
      log.error(
          "Could not parse subscription event on topic {}, partition {} and offset {} with record key {}",
          record.topic(), record.partition(), record.offset(), record.key());
      subscriptionEventHandler.invalidateCache(record.key());
      acknowledgment.acknowledge();
      return;
    }

    try {
      subscriptionEventHandler.process(subscriptionEvent);
    } catch (Exception ex) {
      subscriptionEventHandler.invalidateCache(subscriptionEvent.getSubscriptionKey());
      log.error("Could not process subscription event for subscription key {}", subscriptionEvent.getSubscriptionKey(), ex);
    } finally {
      acknowledgment.acknowledge();
    }
  }
}

package com.tenxbanking.cardrails.adapter.primary.consumer;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class SubscriptionEventListenerTest {

  private static final String RECORD_KEY = "KEY";

  @Mock
  private SubscriptionEventHandler subscriptionEventHandler;

  @InjectMocks
  private SubscriptionEventListener unit;

  @Test
  void shouldInvalidateSubscriptionCacheWhenSubscriptionEventIsNullAndAcknowledgeConsumption() {

    ConsumerRecord<String, SubscriptionEvent> record = getSubscriptionEventRecord(RECORD_KEY, null);
    Acknowledgment acknowledgment = mock(Acknowledgment.class);

    unit.onMessage(record, acknowledgment);

    verify(subscriptionEventHandler).invalidateCache(RECORD_KEY);
    verify(acknowledgment).acknowledge();
  }

  @Test
  void shouldDelegateProcessingOfSubscriptionEventToSubscriptionEventHandlerAndAcknowledgeConsumption() {

    SubscriptionEvent subscriptionEvent = mock(SubscriptionEvent.class);
    ConsumerRecord<String, SubscriptionEvent> record = getSubscriptionEventRecord(RECORD_KEY,
        subscriptionEvent);
    Acknowledgment acknowledgment = mock(Acknowledgment.class);

    unit.onMessage(record, acknowledgment);

    verify(subscriptionEventHandler).process(subscriptionEvent);
    verify(acknowledgment).acknowledge();
  }

  @Test
  void shouldInvalidateSubscriptionCacheWhenSubscriptionEventHandleCannotProcessSubscriptionEvent() {

    String subscriptionKey = randomUUID().toString();
    SubscriptionEvent subscriptionEvent = mock(SubscriptionEvent.class);
    when(subscriptionEvent.getSubscriptionKey()).thenReturn(subscriptionKey);
    ConsumerRecord<String, SubscriptionEvent> record = getSubscriptionEventRecord(RECORD_KEY,
        subscriptionEvent);
    Acknowledgment acknowledgment = mock(Acknowledgment.class);
    doThrow(RuntimeException.class).when(subscriptionEventHandler).process(subscriptionEvent);

    unit.onMessage(record, acknowledgment);

    verify(subscriptionEventHandler).process(subscriptionEvent);
    verify(subscriptionEventHandler).invalidateCache(subscriptionKey);
    verify(acknowledgment).acknowledge();
  }

  private ConsumerRecord<String, SubscriptionEvent> getSubscriptionEventRecord(
      final String subscriptionKey,
      final SubscriptionEvent subscriptionEvent) {

    return new ConsumerRecord<>("topic", 0, 0, subscriptionKey, subscriptionEvent);
  }
}
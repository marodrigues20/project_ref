package com.tenx.tsys.proxybatch.service;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.google.common.util.concurrent.Futures;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KafkaProducerService.class, KafkaProducer.class})
public class KafkaProducerServiceTest {

  @Mock
  private ProducerConfig producerConfig;

  @Mock
  private KafkaProducer<String, String> nonTransactionalKafkaProducer;

  private KafkaProducerService kafkaProducerService;

  @Before
  public void before() throws Exception {
    kafkaProducerService = new KafkaProducerService(producerConfig);
    callPostConstruct();
  }

  @Test
  public void sendShouldSendSynchronously() {
    //given
    ProducerRecord<String, String> record = new ProducerRecord<>("topic-123", "Hello world");
    when(nonTransactionalKafkaProducer.send(record))
        .thenReturn(Futures.immediateFuture(null));
    //when
    kafkaProducerService.send(record);
    //then
    verify(nonTransactionalKafkaProducer).send(record);
  }

  @Test
  public void sendShouldThrowExceptionWhenSendFails() throws Exception {
    //given
    ProducerRecord<String, String> record = new ProducerRecord<>("topic-123", "Hello world");
    when(nonTransactionalKafkaProducer.send(record))
        .thenReturn(Futures.immediateFailedFuture(new RuntimeException("testing exception")));
    //when
    Assertions.assertThatThrownBy(() -> kafkaProducerService.send(record))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Couldn't deliver message to kafka-topic: topic-123");
    //then
    verify(nonTransactionalKafkaProducer).send(record);
  }

  @Test
  public void testDestroy() {
    //when
    kafkaProducerService.destroy();
    //then
    verify(nonTransactionalKafkaProducer).close();
  }

  private void callPostConstruct() throws Exception {
    Properties nonTransactionProperties = mock(Properties.class);
    when(producerConfig.nonTransactionalProducerConfig()).thenReturn(nonTransactionProperties);
    whenNew(KafkaProducer.class).withParameterTypes(Properties.class)
        .withArguments(nonTransactionProperties).thenReturn(
        nonTransactionalKafkaProducer);

    kafkaProducerService.postConstruct();
  }
}
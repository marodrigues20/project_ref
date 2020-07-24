package com.tenx.universalbanking.transactionmanager.messagingservice.producer;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.google.common.util.concurrent.Futures;
import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
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
  private KafkaProducer<String, String> kafkaProducer;

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
    when(kafkaProducer.send(record)).thenReturn(Futures.immediateCheckedFuture(null));
    //when
    kafkaProducerService.send(record);
    //then
    verify(kafkaProducer).send(record);
  }

  @Test(expected = RuntimeException.class)
  public void sendShouldThrowExceptionWhenSendFails() {
    //given
    ProducerRecord<String, String> record = new ProducerRecord<>("topic-123", "Hello world");
    when(kafkaProducer.send(record)).thenReturn(Futures.immediateFailedFuture(new RuntimeException("testing exception")));
    //when
    kafkaProducerService.send(record);
    //then
    verify(kafkaProducer).send(record);
  }

  @Test
  public void testDestroy() {
    //when
    kafkaProducerService.destroy();
    //then
    verify(kafkaProducer).close();
  }

  private void callPostConstruct() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("key.deserializer", StringSerializer.class.getName());
    properties.setProperty("value.deserializer", StringSerializer.class.getName());
    when(producerConfig.producerConfig()).thenReturn(properties);
    whenNew(KafkaProducer.class).withParameterTypes(Properties.class).withArguments(properties).thenReturn(
        kafkaProducer);

    kafkaProducerService.postConstruct();
  }
}
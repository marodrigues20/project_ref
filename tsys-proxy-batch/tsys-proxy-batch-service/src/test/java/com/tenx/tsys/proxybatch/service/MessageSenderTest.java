package com.tenx.tsys.proxybatch.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.tenx.tsys.proxybatch.utils.JsonUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ProducerRecord.class, JsonUtils.class, MessageSender.class})
public class MessageSenderTest {

  private final String repairTopic = "repairTopic";
  @Mock
  private KafkaProducerService kafkaProducerService;
  private MessageSender unit;

  @Before
  public void setUp() {
    unit = new MessageSender(kafkaProducerService, repairTopic);
  }

  @Test
  public void sendErrorMessage_shouldSendRecord() throws Exception {

    ProducerRecord expectedRecord = mock(ProducerRecord.class);
    whenNew(ProducerRecord.class)
        .withArguments(eq(repairTopic), eq("error message"))
        .thenReturn(expectedRecord);

    unit.sendErrorMessage("error message");

    verify(kafkaProducerService).send(expectedRecord);
  }
}
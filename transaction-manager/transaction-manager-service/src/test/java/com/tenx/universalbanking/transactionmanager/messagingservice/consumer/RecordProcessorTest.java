package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static com.tenx.universalbanking.transactionmanager.messagingservice.consumer.ConsumerRecordState.CommitState.COMMITTED;
import static com.tenx.universalbanking.transactionmanager.messagingservice.consumer.ConsumerRecordState.CommitState.UNCOMMITTED;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.tenx.universalbanking.transactionmanager.messagingservice.consumer.ConsumerRecordState.CommitState;
import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import com.tenx.universalbanking.transactionmanager.utils.JsonUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JsonUtils.class, RecordProcessor.class})
public class RecordProcessorTest {

  @Mock
  private ConsumerRecord<String, String> record;

  @Mock
  private TransactionProcessor transactionHandler;

  @Mock
  private KafkaConsumer<String, String> kafkaConsumer;

  @Mock
  private TransactionProcessor transactionProcessor;

  @Mock
  private ConsumerRecordState consumerRecordState;


  private RecordProcessor recordProcessor;

  @Before
  public void before() {
    mockStatic(JsonUtils.class);

    this.recordProcessor = new RecordProcessor(kafkaConsumer, transactionHandler);
  }

  @Test
  public void shouldHandleTransactionWithoutCommittingOffsets_whenOffsetsAreAlreadyCommitted()
      throws Exception {
    TransactionMessage message = mockDeserializationOfMessage();
    ConsumerRecordState consumerRecordState = mockCreationOfConsumerRecordState(record, COMMITTED);

    recordProcessor.process(record);

    verify(transactionHandler).handle(message);
    verifyZeroInteractions(kafkaConsumer);
  }

  @Test
  public void shouldCommitOffsets_whenThereIsAnErrorHandlingTheMessage() throws Exception {
    TransactionMessage message = mockDeserializationOfMessage();
    ConsumerRecordState consumerRecordState = mockCreationOfConsumerRecordState(record,
        UNCOMMITTED);
    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    when(consumerRecordState.nextOffset()).thenReturn(offsets);
    doNothing().when(kafkaConsumer).commitSync(offsets);
    doThrow(RuntimeException.class).when(transactionHandler).handle(message);

    recordProcessor.process(record);

    verify(transactionHandler).handle(message);

    verify(kafkaConsumer).commitSync(offsets);
  }

  @Test
  public void shouldCommitOffsets_whenTheMessageIsNull() throws Exception {
    mockDeserializationOfNullMessage();
    ConsumerRecordState consumerRecordState = mockCreationOfConsumerRecordState(record, COMMITTED);

    recordProcessor.process(record);
    verifyZeroInteractions(transactionHandler);
    verifyZeroInteractions(kafkaConsumer);
  }

  @Test
  public void shouldCommitOffsets_whenTheMessageHandlingOfMessageDoesNotCommitOffsets()
      throws Exception {
    TransactionMessage message = mockDeserializationOfMessage();
    ConsumerRecordState consumerRecordState = mockCreationOfConsumerRecordState(record,
        UNCOMMITTED);
    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    when(consumerRecordState.nextOffset()).thenReturn(offsets);
    doNothing().when(kafkaConsumer).commitSync(offsets);
    recordProcessor.process(record);

    verify(transactionHandler).handle(message);
    verify(kafkaConsumer).commitSync(offsets);
  }

  private TransactionMessage mockDeserializationOfMessage() {
    String rawMessage = "message";
    TransactionMessage transactionMessage = mock(TransactionMessage.class);
    doNothing().when(transactionProcessor).handle(transactionMessage);
    when(record.value()).thenReturn(rawMessage);
    when(stringToJson(rawMessage, TransactionMessage.class)).thenReturn(transactionMessage);
    return transactionMessage;
  }

  private void mockDeserializationOfNullMessage() {
    String rawMessage = "message";
    when(record.value()).thenReturn(rawMessage);
    when(stringToJson(rawMessage, TransactionMessage.class)).thenReturn(null);
  }

  private ConsumerRecordState mockCreationOfConsumerRecordState(
      ConsumerRecord<String, String> record, CommitState commitState) throws Exception {

    whenNew(ConsumerRecordState.class).withArguments(record).thenReturn(consumerRecordState);
    when(consumerRecordState.getCommitState()).thenReturn(commitState);

    return consumerRecordState;
  }
}
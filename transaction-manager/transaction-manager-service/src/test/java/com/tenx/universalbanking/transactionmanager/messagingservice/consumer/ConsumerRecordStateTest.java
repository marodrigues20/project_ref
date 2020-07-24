package com.tenx.universalbanking.transactionmanager.messagingservice.consumer;

import static com.tenx.universalbanking.transactionmanager.messagingservice.consumer.ConsumerRecordState.CommitState.COMMITTED;
import static com.tenx.universalbanking.transactionmanager.messagingservice.consumer.ConsumerRecordState.CommitState.UNCOMMITTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.tenx.universalbanking.transactionmanager.messagingservice.consumer.ConsumerRecordState;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerRecordStateTest {

  @Mock
  private ConsumerRecord<String, String> record;

  @Test
  public void shouldSetStateToUncommitted_whenCreatingANewInstance() {
    //when
    ConsumerRecordState consumerRecordState = new ConsumerRecordState(record);
    //then
    assertThat(consumerRecordState.getCommitState()).isEqualTo(UNCOMMITTED);
  }

  @Test
  public void shouldChangeStateToCommitted_whenCommitIsCalled() {
    //given
    ConsumerRecordState consumerRecordState = new ConsumerRecordState(record);
    //when
    consumerRecordState.commit();
    //then
    assertThat(consumerRecordState.getCommitState()).isEqualTo(COMMITTED);
  }

  @Test
  public void getRecordTest() {
    //given
    ConsumerRecordState consumerRecordState = new ConsumerRecordState(record);

    assertThat(consumerRecordState.getRecord()).isEqualTo(record);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowException_whenCreatingANewInstanceWithNullRecord() {
    new ConsumerRecordState(null);
  }

  @Test
  public void shouldReturnNextOffset_whenNextOffsetIsCalled() {
    //given
    String topic = "topic";
    int partition = 0;
    long offset = 4;
    long expectedOffset = offset + 1;
    //and
    when(record.topic()).thenReturn(topic);
    when(record.partition()).thenReturn(partition);
    when(record.offset()).thenReturn(offset);
    //and
    ConsumerRecordState consumerRecordState = new ConsumerRecordState(record);
    //when
    Map<TopicPartition, OffsetAndMetadata> nextOffset = consumerRecordState.nextOffset();
    //when
    //then
    assertThat(nextOffset.size()).isEqualTo(1);
    assertThat(nextOffset.get(new TopicPartition(topic, partition)))
        .isEqualTo(new OffsetAndMetadata(expectedOffset));
  }
}

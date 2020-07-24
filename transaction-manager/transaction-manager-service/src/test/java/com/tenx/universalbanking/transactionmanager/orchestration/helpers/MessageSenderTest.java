package com.tenx.universalbanking.transactionmanager.orchestration.helpers;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.tenx.universalbanking.transactionmanager.messagingservice.producer.KafkaProducerService;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmanager.utils.JsonUtils;
import com.tenx.universalbanking.transactionmanager.utils.XMLMessageUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ProducerRecord.class, XMLMessageUtils.class, JsonUtils.class, MessageSender.class})
public class MessageSenderTest {

  @Mock
  private KafkaProducerService kafkaProducerService;

  private final String paymentMessageCommandTopic = "paymentMessageCommandTopic";

  private MessageSender unit;
  private static final String MESSAGE_REQUEST = "message/cardFundTransactionMessage.json";
  FileReaderUtil fileReaderUtil = new FileReaderUtil();
  TransactionMessage txnMessage;

  @Before
  public void setUp() throws IOException {
    unit = new MessageSender(kafkaProducerService, paymentMessageCommandTopic);
    txnMessage = stringToJson(
        fileReaderUtil.getFileContent(MESSAGE_REQUEST),
        TransactionMessage.class);
  }

  @Test
  public void sendPaymentMessage_shouldSendRecord() throws Exception {

    mockStatic(XMLMessageUtils.class);
    when(XMLMessageUtils.escapeXMLSpecialCharactersForCain001PaymentMessages(eq(txnMessage), any()))
        .thenReturn(txnMessage);

    mockStatic(JsonUtils.class);
    when(JsonUtils.jsonToString(txnMessage)).thenReturn("abc");

    ProducerRecord expectedRecord = mock(ProducerRecord.class);
    whenNew(ProducerRecord.class)
        .withArguments(eq(paymentMessageCommandTopic), eq("123"), eq("abc"))
        .thenReturn(expectedRecord);

    unit.sendPaymentMessage("123", txnMessage);

    verify(kafkaProducerService).send(expectedRecord);
  }

  @Test
  public void sendPaymentMessage_shouldSendRecordWithEmptyBody() throws Exception {

    txnMessage = new TransactionMessage();
    mockStatic(XMLMessageUtils.class);
    when(XMLMessageUtils.escapeXMLSpecialCharactersForCain001PaymentMessages(eq(txnMessage), any()))
        .thenReturn(txnMessage);

    mockStatic(JsonUtils.class);
    when(JsonUtils.jsonToString(txnMessage)).thenReturn("abc");

    ProducerRecord expectedRecord = mock(ProducerRecord.class);
    whenNew(ProducerRecord.class)
        .withArguments(eq(paymentMessageCommandTopic), eq("123"), eq("abc"))
        .thenReturn(expectedRecord);

    unit.sendPaymentMessage("123", txnMessage);

    verify(kafkaProducerService).send(expectedRecord);
  }

  @Test
  public void sendPaymentMessageBacs_shouldSendRecord() throws Exception {

    mockStatic(JsonUtils.class);
    when(JsonUtils.jsonToString(txnMessage)).thenReturn("abc");

    ProducerRecord expectedRecord = mock(ProducerRecord.class);
    whenNew(ProducerRecord.class)
        .withArguments(eq(paymentMessageCommandTopic), eq("abc"))
        .thenReturn(expectedRecord);

    unit.sendPaymentMessageBacs(txnMessage);

    verify(kafkaProducerService).send(expectedRecord);
  }

  @Test
  public void sendPaymentMessageBacs_shouldSendRecordWithEmptyBody() throws Exception {

    txnMessage = new TransactionMessage();
    mockStatic(JsonUtils.class);
    when(JsonUtils.jsonToString(txnMessage)).thenReturn("abc");

    ProducerRecord expectedRecord = mock(ProducerRecord.class);
    whenNew(ProducerRecord.class)
        .withArguments(eq(paymentMessageCommandTopic), eq("abc"))
        .thenReturn(expectedRecord);

    unit.sendPaymentMessageBacs(txnMessage);

    verify(kafkaProducerService).send(expectedRecord);
  }
}
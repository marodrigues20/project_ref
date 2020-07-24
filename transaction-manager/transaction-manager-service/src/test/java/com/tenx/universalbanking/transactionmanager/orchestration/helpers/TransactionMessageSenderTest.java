package com.tenx.universalbanking.transactionmanager.orchestration.helpers;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.APPLICATION_ADJUSTMENTS;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import com.tenx.universalbanking.transactionmanager.messagingservice.producer.KafkaProducerService;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageSender;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;

public class TransactionMessageSenderTest {

  private KafkaProducerService kafkaProducerService;
  private TransactionMessageSender transactionMessageSender;

  private static final String PAYMENT_TOPIC = "payment-topic";
  private static final String TRANSACTION_CORRELATION_ID_VALUE = "123345";
  private static final String EXPECTED_JSON_STRING = "{\"header\":{\"type\":\"APPLICATION_ADJUSTMENTS\",\"url\":null},"
      + "\"messages\":[{\"type\":null," + "\"message\":{},\"additionalInfo\":{}}],"
      + "\"additionalInfo\":{\"TRANSACTION_CORRELATION_ID\":\"123345\"}}";

  @Before
  public void setUp() {
    kafkaProducerService = mock(KafkaProducerService.class);
    transactionMessageSender = new TransactionMessageSender(kafkaProducerService, PAYMENT_TOPIC);
  }

  @Test
  public void canSendMessageOnTopic() {
    TransactionMessage transactionMessage = getTransactionMessage(true);
    ProducerRecord<String, String> expectedProducerRecord = new ProducerRecord<>(PAYMENT_TOPIC,
        TRANSACTION_CORRELATION_ID_VALUE, EXPECTED_JSON_STRING);

    transactionMessageSender.send(transactionMessage);

    verify(kafkaProducerService).send(expectedProducerRecord);
  }

  @Test
  public void canSendMessageOnTopicWhenCorrelationIdNotPresent() {
    TransactionMessage transactionMessage = getTransactionMessage(false);
    ProducerRecord<String, String> expectedProducerRecord = new ProducerRecord<>(PAYMENT_TOPIC,
        "",
        "{\"header\":{\"type\":\"APPLICATION_ADJUSTMENTS\",\"url\":null},"
            + "\"messages\":[{\"type\":null,"
            + "\"message\":{},\"additionalInfo\":{}}],\"additionalInfo\":{}}");

    transactionMessageSender.send(transactionMessage);

    verify(kafkaProducerService).send(expectedProducerRecord);
  }

  private TransactionMessage getTransactionMessage(boolean withAdditionalInfo) {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setHeader(createTransactionMessageHeader());
    if (withAdditionalInfo) {
      transactionMessage.setAdditionalInfo(createAdditionalInfo());
    }
    transactionMessage.setMessages(singletonList(new PaymentMessage()));
    return transactionMessage;
  }

  private TransactionMessageHeader createTransactionMessageHeader() {
    TransactionMessageHeader transactionMessageHeader = new TransactionMessageHeader();
    transactionMessageHeader.setType(APPLICATION_ADJUSTMENTS.name());
    return transactionMessageHeader;
  }

  private Map<String, Object> createAdditionalInfo() {
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put(TRANSACTION_CORRELATION_ID.name(), TRANSACTION_CORRELATION_ID_VALUE);
    return additionalInfo;
  }

}
package com.tenx.universalbanking.transactionmanager.orchestration.helpers;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.jsonToString;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_ACCEPTOR_NAME;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.messagingservice.producer.KafkaProducerService;
import com.tenx.universalbanking.transactionmanager.utils.XMLMessageUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.util.stream.Collectors;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

  private final Logger logger = getLogger(getClass());

  private final KafkaProducerService kafkaProducerService;

  private final String paymentMessageCommandTopic;

  @Autowired
  private XMLMessageUtils xmlMessageUtils;

  @Autowired
  public MessageSender(KafkaProducerService kafkaProducerService,
    @Value("${kafka.producer.payment-messages.topic.name}") String paymentMessageCommandTopic) {
    this.kafkaProducerService = kafkaProducerService;
    this.paymentMessageCommandTopic = paymentMessageCommandTopic;
  }

  public void sendPaymentMessage(String key,
         TransactionMessage transactionMessage) {
    logger.debug("Attempting to Post the transaction message to Topic.");
    send(paymentMessageCommandTopic, key, transactionMessage);
    logger.debug("Transaction message has been posted to Topic.");
  }

  public void sendPaymentMessageBacs(TransactionMessage transactionMessage) {
    logger.debug("Attempting to Post the transaction message to Topic.");
    sendToTopic(paymentMessageCommandTopic, transactionMessage);
    logger.debug("Transaction message has been posted to Topic.");
  }

  private void send(String topic, String key, TransactionMessage message) {
    XMLMessageUtils.escapeXMLSpecialCharactersForCain001PaymentMessages(message, CARD_ACCEPTOR_NAME);
    String messageStr = jsonToString(message);

    String paymentMessageTypes = message.getMessages()
        .stream().map(PaymentMessage::getType)
        .collect(Collectors.joining(", "));

    Object correlationId = message.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name());

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, messageStr);
    kafkaProducerService.send(record);
    logger.debug("Sent Transaction Message of type {} containing {} to kafka topic {} with key: {} with correlationid: {}"
        , message.getHeader().getType(), paymentMessageTypes, topic, key,
        correlationId != null ? correlationId.toString() : null);
  }

  private void sendToTopic(String topic, TransactionMessage message) {
    String messageStr = jsonToString(message);

    String paymentMessageTypes = message.getMessages()
            .stream().map(PaymentMessage::getType)
            .collect(Collectors.joining(", "));

    Object correlationId = message.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name());

    ProducerRecord<String, String> record = new ProducerRecord<>(topic, messageStr);
    kafkaProducerService.send(record);
    logger.debug("Sent Transaction Message of type {} containing {} to kafka topic {} with correlationid: {}"
            , message.getHeader().getType(), paymentMessageTypes, topic,
            correlationId != null ? correlationId.toString() : null);
  }
}
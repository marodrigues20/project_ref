package com.tenx.universalbanking.transactionmanager.orchestration.helpers;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.jsonToString;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static org.slf4j.LoggerFactory.getLogger;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.tenx.universalbanking.transactionmanager.messagingservice.producer.KafkaProducerService;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;

@Component
public class TransactionMessageSender {

  private final Logger logger = getLogger(TransactionMessageSender.class);
  private final KafkaProducerService kafkaProducerService;
  private final String paymentMessagesTopic;

  public TransactionMessageSender(KafkaProducerService kafkaProducerService,
      @Value("${kafka.producer.ledger-manager.payment-messages.topic.name}") String paymentMessagesTopic) {
    this.kafkaProducerService = kafkaProducerService;
    this.paymentMessagesTopic = paymentMessagesTopic;
  }

  public void send(TransactionMessage transactionMessage) {
    String transactionCorrelationId = getTransactionCorrelationId(transactionMessage);
    ProducerRecord<String, String> record = new ProducerRecord<>(paymentMessagesTopic, transactionCorrelationId,
        jsonToString(transactionMessage));
    kafkaProducerService.send(record);
    logger.debug("Sent record to kafka topic {} with key: {}", paymentMessagesTopic, transactionCorrelationId);
  }

  private String getTransactionCorrelationId(TransactionMessage transactionMessage) {
    return transactionMessage.getAdditionalInfo().getOrDefault(TRANSACTION_CORRELATION_ID.name(), "").toString();
  }
}

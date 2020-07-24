package com.tenx.universalbanking.transactionmanager.messagingservice.producer;

import com.tenx.universalbanking.transactionmanager.exception.ErrorType;
import com.tenx.universalbanking.transactionmanager.exception.TMKafkaException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ProducerConfig producerConfig;

  private KafkaProducer<String, String> nonTransactionalKafkaProducer;

  KafkaProducerService(ProducerConfig producerConfig) {
    this.producerConfig = producerConfig;
  }

  @PostConstruct
  public void postConstruct() {
    Properties config = producerConfig.producerConfig();
    nonTransactionalKafkaProducer = new KafkaProducer<>(config);
  }

  public void send(ProducerRecord<String, String> record) {
    try {
      nonTransactionalKafkaProducer.send(record).get();
    } catch (InterruptedException | ExecutionException e) {

      logger
          .error("Couldn't deliver message with key {} to kafka-topic: {} due to {}", record.key(),
              record.topic(), e);

      if (e instanceof ExecutionException) {
        throw new TMKafkaException(ErrorType.KAFKA_CONNECTION_TIMEOUT_EXP,
            ErrorType.KAFKA_CONNECTION_TIMEOUT_EXP.getMessage());
      }

      throw new RuntimeException(String
          .format("Couldn't deliver message with key %s to kafka-topic: %s", record.key(),
              record.topic()), e);
    }
  }

  @PreDestroy
  public void destroy() {
    logger.debug("Closing kafka producer...");
    nonTransactionalKafkaProducer.close();
    logger.debug("Kafka producer closed successfully.");
  }
}

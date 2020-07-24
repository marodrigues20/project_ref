package com.tenx.tsys.proxybatch.service;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class KafkaProducerService {

  private final ProducerConfig producerConfig;
  private Logger logger = LoggerFactory.getLogger(getClass());
  private KafkaProducer<String, String> nonTransactionalKafkaProducer;

  KafkaProducerService(ProducerConfig producerConfig) {
    this.producerConfig = producerConfig;
  }

  @PostConstruct
  public void postConstruct() {
    Properties config = producerConfig.nonTransactionalProducerConfig();
    nonTransactionalKafkaProducer = new KafkaProducer<>(config);
  }

  public void send(ProducerRecord<String, String> record) {
    try {
      nonTransactionalKafkaProducer.send(record).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(String
          .format("Couldn't deliver message to kafka-topic: %s",
              record.topic()), e);
    }
  }

  @PreDestroy
  public void destroy() {
    logger.info("Closing kafka producer...");
    nonTransactionalKafkaProducer.close();
    logger.info("Kafka producer closed successfully.");
  }
}

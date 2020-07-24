package com.tenx.tsys.proxybatch.service;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

  private final Logger logger = getLogger(getClass());

  private final KafkaProducerService kafkaProducerService;

  private final String repairTopic;

  @Autowired
  public MessageSender(KafkaProducerService kafkaProducerService,
      @Value("${kafka.producer.repair.topic.name}") String repairTopic) {
    this.kafkaProducerService = kafkaProducerService;
    this.repairTopic = repairTopic;
  }

  public void sendErrorMessage(String errorMessage) {
    send(repairTopic, errorMessage);
  }

  public void send(String topic, String errorMessage) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, errorMessage);
    kafkaProducerService.send(record);
    logger.info("Sent error message to kafka stream");
  }
}

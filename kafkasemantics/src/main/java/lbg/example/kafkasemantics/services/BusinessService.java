package lbg.example.kafkasemantics.services;

import lbg.example.kafkasemantics.entities.PaymentBusinessEvent;
import lbg.example.kafkasemantics.models.KafkaPosition;
import lbg.example.kafkasemantics.models.Notification;
import lbg.example.kafkasemantics.models.SourceTopic;
import lbg.example.kafkasemantics.models.TargetTopic;
import lbg.example.kafkasemantics.repositories.PaymentBusinessEventRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Log4j2
public class BusinessService {

    @Autowired
    private PaymentBusinessEventRepository paymentBusinessEventRepository;

    @Transactional
    public void businessFlow() {
        log.info("Starting Business Flow with Save action");
        PaymentBusinessEvent paymentBusinessEvent = paymentBusinessEventRepository.save(PaymentBusinessEvent.builder()
                .correlationId(UUID.randomUUID().toString())
                .currentTimestamp(LocalDateTime.now())
                .eventName("kafkaSemantics")
                .eventSource("rCBS")
                .build());

        log.info("Saved into Payment Business Event Table with records -> {}", paymentBusinessEvent);
    }

    public Long getAmountCommitted() {
        long recordsCommited = paymentBusinessEventRepository.count();
        log.info("How many records has been commited till now -> {}", recordsCommited);
        return recordsCommited;
    }

    public KafkaPosition getLastOffSetCommitted() {

        KafkaConsumer kafkaConsumer = getKafkaConsumer();
        List<TopicPartition> listOfTopicPartition = List.of(new TopicPartition("json-pos-topic", 0), new TopicPartition("loyalty-topic", 0));

        //Get the last committed offset for the given partition
        OffsetAndMetadata offsetAndMetadataJsonPosTopic = null;
        OffsetAndMetadata offsetAndMetadataLoyaltyTopic = null;

        try {
            //Retrieve the offset commited
            offsetAndMetadataJsonPosTopic = kafkaConsumer.committed(listOfTopicPartition.get(0));
            offsetAndMetadataLoyaltyTopic = kafkaConsumer.committed(listOfTopicPartition.get(1));
        } catch (Exception e) {
            log.error("Not committed any offset to the topic {}", offsetAndMetadataJsonPosTopic = kafkaConsumer.committed(listOfTopicPartition.get(0)));
            log.error("Not committed any offset to the topic {}", offsetAndMetadataLoyaltyTopic = kafkaConsumer.committed(listOfTopicPartition.get(1)));
        }


        return KafkaPosition.builder()
                .sourceTopic(SourceTopic.builder()
                        .topicName("json-pos-topic")
                        .currentOffSet(getPositionOfLatestOffSet(kafkaConsumer, listOfTopicPartition.get(0)).toString())
                        .committedOffSet(Long.toString(offsetAndMetadataJsonPosTopic != null ? offsetAndMetadataJsonPosTopic.offset() : 0L))
                        .consumerPositionOffSet(String.valueOf(kafkaConsumer.position(listOfTopicPartition.get(0))))
                        .build())
                .targetTopic(TargetTopic.builder()
                        .topicName("loyalty-topic")
                        .currentOffSet(getPositionOfLatestOffSet(kafkaConsumer, listOfTopicPartition.get(1)).toString())
                        .committedOffSet(Long.toString(offsetAndMetadataLoyaltyTopic != null ? offsetAndMetadataLoyaltyTopic.offset() : 0L))
                        .consumerPositionOffSet(String.valueOf(kafkaConsumer.position(listOfTopicPartition.get(1))))
                        .build()).build();
    }

    private KafkaConsumer getKafkaConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "PosListenerService-process-applicationId");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");

        DefaultKafkaConsumerFactory<String, String> defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory(consumerProps);
        return (KafkaConsumer) defaultKafkaConsumerFactory.createConsumer();
    }

    private Long getPositionOfLatestOffSet(KafkaConsumer kafkaConsumer, TopicPartition topicPartition) {
        //assign the topic
        kafkaConsumer.assign(List.of(topicPartition));
        //seek to end of the topic
        kafkaConsumer.seekToEnd(List.of(topicPartition));
        //the position is the latest offset
        Long positionOfLatestOffset = kafkaConsumer.position(topicPartition);
        log.info("Let's see the positionOfLatestOffset: " + positionOfLatestOffset);

        return positionOfLatestOffset;

    }

    public List<Notification> retrieveRecordsFromLoyaltyTopic() {

        List<Notification> list = new ArrayList<>();

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "WorldCup2022");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
        consumerProps.put("spring.kafka.producer.properties.spring.json.add.type.headers", "false");


        try (final KafkaConsumer<String, Notification> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList("loyalty-topic"));
            consumer.assignment();
            ConsumerRecords<String, Notification> records = consumer.poll(50000);
            for (ConsumerRecord<String, Notification> record : records) {
                list.add(record.value());
            }
        }

        return list;
    }
}

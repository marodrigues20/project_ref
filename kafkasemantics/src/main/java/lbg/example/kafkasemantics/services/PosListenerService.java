package lbg.example.kafkasemantics.services;

import lbg.example.kafkasemantics.bindings.PosListenerBinding;
import lbg.example.kafkasemantics.models.Notification;
import lbg.example.kafkasemantics.models.PosInvoice;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Log4j2
@EnableBinding(PosListenerBinding.class)
public class PosListenerService {

    @Autowired
    RecordBuilder recordBuilder;
    @Autowired
    private BusinessService businessService;

    private static int count = 1;

    @StreamListener("pos-input-channel")
    @SendTo("notification-output-channel")
    public KStream<String, Notification> process(KStream<String, PosInvoice> input) {
        KStream<String, Notification> notificationKStream = input
                .mapValues(v -> recordBuilder.getNotification(v));

        notificationKStream.foreach((k, v) -> {
                    log.info(String.format("Notification:- Key: %s, Value: %s", k, v));
                    businessService.businessFlow();
                    businessService.getAmountCommitted();
                }
        );

        //log.info("Messages sent, hit enter to commit tx");
        //System.in.read();


        return notificationKStream;
    }

    @KafkaListener(id = "fooGroup3", topics = "loyalty-topic")
    public void listen(String in) {

        log.info("Received message {} from loyalty-topic topic. Whose message is: {} ", count++, in);
    }

}

package lbg.example.kafkasemantics.services;

import lbg.example.kafkasemantics.models.PosInvoice;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, PosInvoice> kafkaTemplate;

    public void sendMessage(PosInvoice invoice){
        log.info(String.format("Producing Invoice No: %s", invoice.getInvoiceNumber()));
        // kafkaTemplate will automatically serialize this invoice object to a JSON String and send it to Kafka topic.
        // The KafkaTemplate will do it because we configure the template to use the JSON serialization.
        kafkaTemplate.send("json-pos-topic", invoice.getStoreID(), invoice);
    }
}

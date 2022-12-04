package lbg.example.kafkasemantics.serde;

import com.fasterxml.jackson.core.type.TypeReference;
import lbg.example.kafkasemantics.models.Notification;
import lbg.example.kafkasemantics.models.PosInvoice;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class PosInvoiceSerde implements Serde<PosInvoice> {
    @Override
    public Serializer<PosInvoice> serializer() {
        return new JsonSerializer<>();
    }

    @Override
    public Deserializer<PosInvoice> deserializer() {
        return new JsonDeserializer<>(new TypeReference<>() {},false);
    }
}

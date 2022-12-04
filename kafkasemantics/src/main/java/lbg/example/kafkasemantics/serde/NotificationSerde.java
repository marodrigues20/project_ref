package lbg.example.kafkasemantics.serde;

import com.fasterxml.jackson.core.type.TypeReference;
import lbg.example.kafkasemantics.models.Notification;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class NotificationSerde implements Serde<Notification> {
    @Override
    public Serializer<Notification> serializer() {
        return new JsonSerializer<>();
    }

    @Override
    public Deserializer<Notification> deserializer() {
        return new JsonDeserializer<>(new TypeReference<>() {},false);
    }
}

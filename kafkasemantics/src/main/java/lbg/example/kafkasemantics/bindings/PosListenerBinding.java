package lbg.example.kafkasemantics.bindings;



import lbg.example.kafkasemantics.models.Notification;
import lbg.example.kafkasemantics.models.PosInvoice;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;

public interface PosListenerBinding {

    @Input("pos-input-channel")
    KStream<String, PosInvoice> posInputStream();

    @Output("notification-output-channel")
    KStream<String, Notification> notificationOutputStream();

}

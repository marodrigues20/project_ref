package lbg.example.kafkasemantics.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name="PAYMENT_BUSINESS_EVENT")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentBusinessEvent {

    @Id
    @Column(name="CORRELATION_ID")
    private String correlationId;
    @Column(name="EVENT_NAME")
    private String eventName;
    @Column(name = "TIMESTAMP_CURRENT", columnDefinition = "TIMESTAMP")
    private LocalDateTime currentTimestamp;
    @Column(name = "EVENT_SOURCE")
    private String eventSource;
}

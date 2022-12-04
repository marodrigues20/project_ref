package lbg.example.kafkasemantics.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaPosition {

    private SourceTopic sourceTopic;
    private TargetTopic targetTopic;
}

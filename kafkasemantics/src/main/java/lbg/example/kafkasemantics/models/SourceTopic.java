package lbg.example.kafkasemantics.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceTopic {

    private String topicName;
    private String currentOffSet;
    private String committedOffSet;
    private String consumerPositionOffSet;
}

package lbg.example.kafkasemantics.rest;


import lbg.example.kafkasemantics.models.KafkaPosition;
import lbg.example.kafkasemantics.models.Notification;
import lbg.example.kafkasemantics.services.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class KafkaRestController {

    @Autowired
    private BusinessService businessService;

    @GetMapping("/kafka")
    public KafkaPosition getCustomers(){
        return businessService.getLastOffSetCommitted();
    }

    @GetMapping("/retrieveRecordsFromLoyaltyTopic")
    public List<Notification> getRecords(){
        return businessService.retrieveRecordsFromLoyaltyTopic();
    }
}

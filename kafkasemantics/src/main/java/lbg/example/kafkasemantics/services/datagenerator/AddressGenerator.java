package lbg.example.kafkasemantics.services.datagenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import lbg.example.kafkasemantics.models.DeliveryAddress;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Random;

@Service
class AddressGenerator {

    private final Random random;
    private final DeliveryAddress[] addresses;

    public AddressGenerator() {
        final String DATAFILE = "src/main/resources/data/address.json";
        final ObjectMapper mapper;
        random = new Random();
        mapper = new ObjectMapper();
        try {
            addresses = mapper.readValue(new File(DATAFILE), DeliveryAddress[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getIndex() {
        return random.nextInt(10);
    }

    public DeliveryAddress getNextAddress() {
        return addresses[getIndex()];
    }


}

package lbg.example.kafkasemantics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lbg.example.kafkasemantics.models.PosInvoice;
import lbg.example.kafkasemantics.services.KafkaProducerService;
import lbg.example.kafkasemantics.services.datagenerator.InvoiceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkasemanticsApplication implements ApplicationRunner {

	@Autowired
	private KafkaProducerService producerService;
	@Autowired
	private InvoiceGenerator invoiceGenerator;

	ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args) {
		SpringApplication.run(KafkasemanticsApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		for(int i = 0; i < 10; i++){
			PosInvoice posInvoice = invoiceGenerator.getNextInvoice();
			String json = objectMapper.writeValueAsString(posInvoice);
			producerService.sendMessage(posInvoice);
			Thread.sleep(1000);
		}
	}
}

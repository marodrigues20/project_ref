package com.example.pubsubgcpspringapplications;


import java.util.HashMap;
import java.util.Map;

import com.example.pubsubgcpspringapplications.services.PublishingMessage;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@SpringBootApplication
public class PubsubgcpSpringApplicationsApplication implements CommandLineRunner {

	@Autowired
	PublishingMessage publishingMessage;
	@Autowired
	private PubSubTemplate pubSubTemplate;

	public static void main(String[] args) {
		SpringApplication.run(PubsubgcpSpringApplicationsApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {

		System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
		System.out.println(System.getenv("GOOGLE_CLOUD_PROJECT"));

		//PublishingMessage publishingMessage = new PublishingMessage(pubSubTemplate);
		//Map<String, Object> message = new HashMap<>();
		//message.put("name", "Alexandre");
		//message.put("age", 41);


		publishingMessage.publishMessage("Teste Completo Alexandre");

		Thread.sleep(20000L);
	}
}

package com.tenxbanking.cardrails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.Lifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories(repositoryImplementationPostfix = "CockroachRepository")
@EnableScheduling
public class Application {

  @Value("${spring.kafka.startlisteners:true}")
  private boolean startListeners;

  @Autowired
  private KafkaListenerEndpointRegistry registry;

  public static void main(String... args) {
    SpringApplication.run(Application.class);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReadyEvent() {
    if (startListeners) {
      registry.getAllListenerContainers().forEach(Lifecycle::start);
      log.info("Kafka listeners started");
    } else {
      log.warn("Kafka listeners not started");
    }
  }
}

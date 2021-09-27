package com.example.pubsubgcpspringapplications;


import java.io.IOException;

import com.example.pubsubgcpspringapplications.config.PubSubTestConfig;
import com.example.pubsubgcpspringapplications.config.PubSubTestNoSpringConfig;
import com.example.pubsubgcpspringapplications.services.MessageRealGcpService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class EmulatorPubSubWithSpringTest {

  @Autowired
  MessageRealGcpService messageRealGcpService;

  @BeforeAll
  static void startUpTests() throws IOException {
    PubSubTestConfig.setupPubSubEmulator();
  }

  @Test
  void successfulTest() throws InterruptedException {

    messageRealGcpService.publishMessage("Vania!!!");

    Thread.sleep(15000);

  }

  /*@AfterAll
  public static void destroy() throws Exception {
    PubSubTestConfig.tearDown();
  }*/

}

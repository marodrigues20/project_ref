package com.example.pubsubgcpspringapplications.controller;


import com.example.pubsubgcpspringapplications.services.MessageRealGcpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RealGcpPubSubController {

  @Autowired
  MessageRealGcpService publishingMessage;

  @GetMapping("/sendMessageToRealGcp")
  public String sendMessageToRealPublish() throws InterruptedException {


    //It's comming null in output because I am setting in application.properties
    System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
    System.out.println(System.getenv("GOOGLE_CLOUD_PROJECT"));

    publishingMessage.publishMessage("Teste Completo Alexandre");

    Thread.sleep(20000L);

    return "Check the result on output";

  }

}

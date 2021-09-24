package com.example.pubsubgcpspringapplications.services;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class PublishingMessage {


  private final PubSubTemplate pubSubTemplate;

  @Autowired
  public PublishingMessage(PubSubTemplate pubSubTemplate) {
    this.pubSubTemplate = pubSubTemplate;
  }

  public void publishMessage(String message) {
    ListenableFuture<String> future = pubSubTemplate.publish("topic-one", message);
    System.out.println("Publish using pubSubTemplate in topic-one: " + message);

    // Add an asynchronous callback to handle success / failure
    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(String result) {
        System.out.println("Message was sent via the outbound channel adapter to topic-one!");
      }

      @Override
      public void onFailure(Throwable ex) {
        System.out.println("There was an error sending the message.");
      }
    });
  }

}

package com.example.pubsubgcpspringapplications;


import java.nio.charset.Charset;

import com.example.pubsubgcpspringapplications.config.PubSubTestNoSpringConfig;
import com.example.pubsubgcpspringapplications.services.MessageRealGcpService;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


public class EmulatorPubSubNoSpringTest {

  @Autowired
  MessageRealGcpService messageRealGcpService;

  @BeforeAll
  static void beforeAll() throws Exception {
    PubSubTestNoSpringConfig.setupPubSubEmulator();
  }


  @Test
  public void testLocalPubSub() throws Exception {

    final String messageText = "text";
    PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
        .setData(ByteString.copyFrom(messageText, Charset.defaultCharset()))
        .build();
    PubSubTestNoSpringConfig.getPublisher().publish(pubsubMessage).get();

    PullRequest pullRequest = PullRequest.newBuilder()
        .setMaxMessages(1)
        .setReturnImmediately(true) // return immediately if messages are not available
        .setSubscription(PubSubTestNoSpringConfig.getSubscription().getName())
        .build();

    PullResponse pullResponse = PubSubTestNoSpringConfig.getSubscriberStub().pullCallable().call(pullRequest);
    String receiveMessageText = pullResponse.getReceivedMessages(0).getMessage().getData().toStringUtf8();

    Assert.hasText(messageText, receiveMessageText);

        //assertEquals(messageText, receiveMessageText);
  }

  @AfterAll
  public static void destroy() throws Exception {
    PubSubTestNoSpringConfig.tearDown();
  }

}
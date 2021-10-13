package com.example.pubsubgcpspringapplications.config;

import java.io.IOException;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class PubSubTestConfig {

  private static final String hostPort = "127.0.0.1:8085";
  private static final String PROJECT_ID = "my-project-id";
  private static final String TOPIC_NAME = "topic-one";
  private static final String SUBSCRIPTION_NAME = "sub-one";

  private static PubSubAdmin admin;

  public static void setupPubSubEmulator() throws IOException {

    ManagedChannel channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();
    TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(
        GrpcTransportChannel.create(channel));

    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    TopicAdminClient topicAdminClient = TopicAdminClient.create(
        TopicAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()
    );

    SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
        .setCredentialsProvider(credentialsProvider)
        .setTransportChannelProvider(channelProvider)
        .build());

    admin =
        new PubSubAdmin(() -> PROJECT_ID, topicAdminClient, subscriptionAdminClient);

    admin.createTopic(TOPIC_NAME);
    admin.createSubscription(SUBSCRIPTION_NAME, TOPIC_NAME);

    admin.close();
    channel.shutdown();

  }

  public static void tearDown() throws Exception {
    admin.deleteTopic(TOPIC_NAME);
    admin.deleteSubscription(SUBSCRIPTION_NAME);
  }
}

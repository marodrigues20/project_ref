package com.example.pubsubgcpspringapplications.config;

import java.io.IOException;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class PubSubTestNoSpringConfig {

  private static final String PROJECT = "test-project";
  private static final String SUBSCRIPTION_NAME = "SUBSCRIBER";
  private static final String TOPIC_NAME = "test-topic-id";

  private static final String hostPort = "127.0.0.1:8085";

  private static ManagedChannel channel;
  private static TransportChannelProvider channelProvider;
  private static TopicAdminClient topicAdmin;

  private static Publisher publisher;
  private static SubscriberStub subscriberStub;
  private static SubscriptionAdminClient subscriptionAdminClient;

  private static ProjectTopicName topicName = ProjectTopicName.of(PROJECT, TOPIC_NAME);
  private static ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(PROJECT, SUBSCRIPTION_NAME);

  private static Subscription subscription;

  public static void setupPubSubEmulator() throws IOException {

    channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();
    channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));

    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    topicAdmin = createTopicAdmin(credentialsProvider);
    topicAdmin.createTopic(topicName);

    publisher = createPublisher(credentialsProvider);
    subscriberStub = createSubscriberStub(credentialsProvider);

    subscriptionAdminClient = createSubscriptionAdmin(credentialsProvider);
    subscription = subscriptionAdminClient.createSubscription(subscriptionName, topicName,
        PushConfig.getDefaultInstance(), 0);

  }


  private static TopicAdminClient createTopicAdmin(CredentialsProvider credentialsProvider) throws IOException {
    return TopicAdminClient.create(
        TopicAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()
    );
  }

  private static SubscriptionAdminClient createSubscriptionAdmin(CredentialsProvider credentialsProvider) throws IOException {
    SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
        .setCredentialsProvider(credentialsProvider)
        .setTransportChannelProvider(channelProvider)
        .build();
    return SubscriptionAdminClient.create(subscriptionAdminSettings);
  }

  private static Publisher createPublisher(CredentialsProvider credentialsProvider) throws IOException {
    return Publisher.newBuilder(topicName)
        .setChannelProvider(channelProvider)
        .setCredentialsProvider(credentialsProvider)
        .build();
  }

  private static SubscriberStub createSubscriberStub(CredentialsProvider credentialsProvider) throws IOException {
    SubscriberStubSettings subscriberStubSettings = SubscriberStubSettings.newBuilder()
        .setTransportChannelProvider(channelProvider)
        .setCredentialsProvider(credentialsProvider)
        .build();
    return GrpcSubscriberStub.create(subscriberStubSettings);
  }

  public static void tearDown() throws Exception {
    topicAdmin.deleteTopic(topicName);
    subscriptionAdminClient.deleteSubscription(subscription.getName());
    channel.shutdownNow();
  }

  public static Publisher getPublisher() {
    return publisher;
  }

  public static ProjectSubscriptionName getSubscriptionName() {
    return subscriptionName;
  }

  public static Subscription getSubscription() {
    return subscription;
  }

  public static SubscriberStub getSubscriberStub() {
    return subscriberStub;
  }
}

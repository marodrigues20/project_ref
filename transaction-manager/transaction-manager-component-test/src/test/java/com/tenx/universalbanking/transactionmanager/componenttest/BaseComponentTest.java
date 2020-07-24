package com.tenx.universalbanking.transactionmanager.componenttest;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.FEES_AND_CHARGES_COMMAND_TOPIC;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.FEES_AND_CHARGES_EVENTS_TOPIC;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.LEDGER_COMMAND_TOPIC_NAME;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.LEDGER_PAYMENT_MESSAGE_TOPIC_NAME;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.PAYMENT_COMMAND_TOPIC_NAME;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.PAYMENT_EVENT_TOPIC;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.PAYMENT_PROXY_COMMAND_TOPIC_NAME;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.PAYMENT_PROXY_EVENT_TOPIC;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.TRANSACTION_ENRICHMENT_COMMAND_TOPIC;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.TRANSACTION_MANAGER_COMMAND_TOPIC_NAME;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.TRANSACTION_MANAGER_EVENT_TOPIC;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.Constants.WIREMOCK_PORT;
import static com.tenx.universalbanking.transactionmanager.componenttest.utils.FileUtils.getFileContent;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.FIVE_SECONDS;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.opentable.db.postgres.embedded.FlywayPreparer;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.PreparedDbRule;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;
import com.tenx.universalbanking.transactionmanager.TransactionManagerApp;
import com.tenx.universalbanking.transactionmanager.componenttest.retry.RetryRule;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan(basePackages = {"com.tenx.*"})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {TransactionManagerApp.class})
@TestPropertySource(properties = {"downstream.debit-card-manager.rest.url=http://localhost:${mock.port}"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class BaseComponentTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected static TestRestTemplate testRestTemplate;

  private static final int SCHEMA_REGISTRY_PORT = 9876;
  private static final String SCHEMA_REGISTRY_URL = "http://localhost:" + SCHEMA_REGISTRY_PORT;

  @LocalServerPort
  protected int randomServerPort;

  @Rule
  public RetryRule rule = new RetryRule(2);

  @ClassRule
  public static SingleInstancePostgresRule db = EmbeddedPostgresRules.singleInstance()
          .customize((db) -> db.setPort(1234));

  @ClassRule
  public static PreparedDbRule dbPreparer =
      EmbeddedPostgresRules.preparedDatabase(
          FlywayPreparer.forClasspathLocation("db/migration/cockroachdb"))
          .customize((db) -> db.setPort(1234));

  @ClassRule
  public static EmbeddedKafkaCluster KAFKA_CLUSTER = new EmbeddedKafkaCluster(1, brokerProperties());

  @ClassRule
  public static WireMockRule WIREMOCK_SERVER = new WireMockRule(
      wireMockConfig().port(WIREMOCK_PORT));

  private static KafkaService kafkaService;
  private static Map<String, List<ConsumerRecord<String, String>>> recordsPerTopic;

  protected Function<String, String> serverUrl = (String path) -> format("http://localhost:%s%s",
      randomServerPort, path);

  @BeforeClass
  public static void beforeClass() throws InterruptedException {
    testRestTemplate = new TestRestTemplate();

    System.setProperty("kafka.bootstrap.servers", KAFKA_CLUSTER.bootstrapServers());
    System.setProperty("kafkastore.bootstrap.servers", KAFKA_CLUSTER.bootstrapServers());
    System.setProperty("kafka.schema.registry.url", SCHEMA_REGISTRY_URL);

    KAFKA_CLUSTER.createTopics(PAYMENT_COMMAND_TOPIC_NAME, PAYMENT_EVENT_TOPIC,
        TRANSACTION_ENRICHMENT_COMMAND_TOPIC, FEES_AND_CHARGES_COMMAND_TOPIC,
        FEES_AND_CHARGES_EVENTS_TOPIC, LEDGER_COMMAND_TOPIC_NAME,
        TRANSACTION_MANAGER_COMMAND_TOPIC_NAME,
        TRANSACTION_MANAGER_EVENT_TOPIC, PAYMENT_PROXY_COMMAND_TOPIC_NAME,
        PAYMENT_PROXY_EVENT_TOPIC, LEDGER_PAYMENT_MESSAGE_TOPIC_NAME);

    kafkaService = new KafkaService(KAFKA_CLUSTER.bootstrapServers(),
        BaseComponentTest.class.getSimpleName());

    kafkaService.createProducer();
    kafkaService.createConsumer(
        PAYMENT_EVENT_TOPIC, TRANSACTION_ENRICHMENT_COMMAND_TOPIC,
        FEES_AND_CHARGES_COMMAND_TOPIC, LEDGER_COMMAND_TOPIC_NAME,
        TRANSACTION_MANAGER_COMMAND_TOPIC_NAME, PAYMENT_PROXY_COMMAND_TOPIC_NAME,
        TRANSACTION_MANAGER_EVENT_TOPIC, LEDGER_PAYMENT_MESSAGE_TOPIC_NAME);
  }

  @AfterClass
  public static void afterClass() {
    if (kafkaService != null) {
      kafkaService.destroy();
    }
  }

  @Before
  public void before() throws InterruptedException {
    WIREMOCK_SERVER.resetAll();
    recordsPerTopic = new HashMap<>();
  }

  private static Properties brokerProperties() {
    Properties props = new Properties();
    props.put("transaction.state.log.replication.factor", Short.valueOf("1"));
    props.put("transaction.state.log.min.isr", 1);

    return props;
  }

  void waitUntilReadNMessages(String topic, int numberOfMessages) throws IOException {

    await().atMost(FIVE_SECONDS)
        .until(() -> {
          List<ConsumerRecord<String, String>> topicRecords = new ArrayList<>();
          while (topicRecords.size() < numberOfMessages) {
            ConsumerRecords<String, String> records = kafkaService.consume(topic);

            for (ConsumerRecord<String, String> record : records) {
              topicRecords.add(record);
            }
          }
          recordsPerTopic.put(topic, topicRecords);
          return topicRecords.size() >= numberOfMessages;
        });
  }

  public void verifyTopicContainsMessage(String topic, String messageFile) throws IOException {
    final String expectedEventMessage = getFileContent(messageFile);

    await()
        .atMost(FIVE_SECONDS)
        .untilAsserted(() -> {

          final String matchedMessage = matchRecord(recordsPerTopic.get(topic),
              expectedEventMessage);
          if (matchedMessage == null) {
            String errorMessage = format(
                "Verification failed - Topic %s does not contain expected message %s:",
                topic, expectedEventMessage);
            throw new AssertionError(errorMessage);
          }
        });
  }

  String verifyKafkaContainsMessage(String topic, String messageFile) throws IOException {
    return verifyMessageOnKafka(topic, messageFile, true);
  }

  private String verifyMessageOnKafka(String topic, String messageFile, boolean verifyTracing)
      throws IOException {
    AtomicReference<String> atomicReference = new AtomicReference<>("");
    final String expectedEventMessage = getFileContent(messageFile);
    await().atMost(FIVE_SECONDS)
        .until(() -> {
          boolean match = false;
          ConsumerRecords<String, String> records = kafkaService.consume(topic);
          for (ConsumerRecord<String, String> record : records) {
            match = isMatch(expectedEventMessage, record.value(), verifyTracing);
            atomicReference.set(record.value());
            if (match) {
              break;
            }
          }
          return match;
        });
    return atomicReference.get();
  }

  String verifyKafkaContainsMessage(String topic, String messageFile, boolean verifyTracing)
      throws IOException {
    return verifyMessageOnKafka(topic, messageFile, verifyTracing);
  }

  private String matchRecord(List<ConsumerRecord<String, String>> topicRecords, String expected) {

    for (ConsumerRecord<String, String> record : topicRecords) {
      JSONCompareResult result = compareJSON(expected, record.value(), JSONCompareMode.LENIENT);
      if (!result.failed() && containsTraceSegmentParent(record.value())) {
        return record.value();
      }
    }
    return null;
  }

  public boolean isMatch(String expected, String actual, boolean verifyTracing) {

    try {
      assertEquals(expected, actual, JSONCompareMode.LENIENT);
      if (verifyTracing) {
        return containsTraceSegmentParent(actual);
      }
      return true;
    } catch (AssertionError | JSONException e) {
      logger.debug("Verification failed - \nexpected: {}\nactual: {}", expected, actual);
      return false;
    }
  }

  private boolean containsTraceSegmentParent(String actual) {
    try {
      JSONObject additionalInfo = new JSONObject(actual).getJSONObject("additionalInfo");
      String traceSegmentParent = additionalInfo.getString("TRACE_SEGMENT_PARENT");
      return isNotEmpty(traceSegmentParent);
    } catch (Exception e) {
      logger.error("Message does not contain TRACE_SEGMENT_PARENT");
      return false;
    }
  }

  public HttpEntity<TransactionMessage> buildTransactionMsgRequest(String requestBody) throws IOException {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-B3-TraceId", "5c41b539181035048daf8df39b94f2fd");
    headers.set("l5d-test", "test123");
    headers.setAccept(singletonList(APPLICATION_JSON));
    TransactionMessage requestJson = stringToJson(getFileContent(requestBody), TransactionMessage.class);
    return new HttpEntity<>(requestJson, headers);
  }

}
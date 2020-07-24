package com.tenx.tsys.proxybatch.componenttest;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.tenx.tsys.proxybatch.componenttest.Constants.TSYS_PROXY_BATCH_REPAIR_TOPIC;
import static com.tenx.tsys.proxybatch.componenttest.util.FileUtility.getFileContent;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tenx.TsysProxyBatchApplication;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.yml")
@ComponentScan(basePackages = {"com.tenx.*"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TsysProxyBatchApplication.class)
@AutoConfigureMockMvc
public abstract class ComponentTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  static final Integer WIREMOCK_PORT = 9090;

  @ClassRule
  public static EmbeddedKafkaCluster KAFKA_CLUSTER = new EmbeddedKafkaCluster(1,
      brokerProperties());

  @ClassRule
  public static WireMockRule WIREMOCK_SERVER = new WireMockRule(options().port(WIREMOCK_PORT),
      false);

  static KafkaService kafkaService;
  static Map<String, List<ConsumerRecord<String, String>>> recordsPerTopic;

  @BeforeClass
  public static void beforeClass() throws InterruptedException {
    System.setProperty("kafka.bootstrap.servers", KAFKA_CLUSTER.bootstrapServers());
    KAFKA_CLUSTER.createTopics(TSYS_PROXY_BATCH_REPAIR_TOPIC);

    kafkaService = new KafkaService(KAFKA_CLUSTER.bootstrapServers(),
        ComponentTest.class.getSimpleName());

    kafkaService.createConsumer(TSYS_PROXY_BATCH_REPAIR_TOPIC);

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
    Thread.sleep(2000);
  }

  private static Properties brokerProperties() {
    Properties props = new Properties();
    props.put("transaction.state.log.replication.factor", Short.valueOf("1"));
    props.put("transaction.state.log.min.isr", 1);
    return props;
  }

  private String verifyMessageOnKafka(String topic, String messageFile, boolean verifyTracing)
      throws IOException, URISyntaxException {
    AtomicReference<String> atomicReference = new AtomicReference<>("");
    final String expectedEventMessage = getFileContent(messageFile);
    await().atMost(Duration.ofSeconds(5))
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
      throws IOException, URISyntaxException {
    return verifyMessageOnKafka(topic, messageFile, verifyTracing);
  }

  public boolean isMatch(String expected, String actual, boolean verifyTracing) {

    try {
      assertThat(expected).isEqualTo(actual);
      return true;
    } catch (AssertionError e) {
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

}

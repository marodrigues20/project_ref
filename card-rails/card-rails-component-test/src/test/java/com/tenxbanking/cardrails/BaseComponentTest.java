package com.tenxbanking.cardrails;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tenxbanking.cardrails.util.EmbeddedKafkaManager;
import com.tenxbanking.cardrails.util.EmbeddedRedisManager;
import com.tenxbanking.cardrails.util.FixedClock;
import com.tenxbanking.cardrails.util.KafkaService;
import java.time.Clock;
import java.util.function.Function;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = {Application.class, FixedClock.class}
)
@TestPropertySource(properties = {"dbUsername=" + BaseComponentTest.DATABASE_USER,
    "database=" + BaseComponentTest.DATABASE_NAME})
public abstract class BaseComponentTest {

  protected static final String DATABASE_NAME = "defaultdb";
  protected static final String DATABASE_USER = "root";

  protected static final EmbeddedKafkaManager KAFKA_MANAGER = EmbeddedKafkaManager.getInstance();
  protected static final EmbeddedRedisManager REDIS_MANAGER = EmbeddedRedisManager.getInstance();

  private static final int WIREMOCK_PORT = 8888;

  @ClassRule
  public static WireMockRule WIREMOCK_SERVER = new WireMockRule(wireMockConfig().port(WIREMOCK_PORT));

  protected static KafkaService KAFKA_SERVICE;

  @Autowired
  protected Clock clock;

  @LocalServerPort
  protected int randomServerPort;

  protected Function<String, String> serverUrl = (String path) -> format("http://localhost:%s%s", randomServerPort, path);

  @BeforeClass
  public static void createKafkaService() {
    KAFKA_SERVICE = new KafkaService(KAFKA_MANAGER.bootstrapServers(),
        KAFKA_MANAGER.schemaRegistryUrl());
  }

  @AfterClass
  public static void afterClass() {
    WIREMOCK_SERVER.resetAll();
  }

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("schema.registry.url", KAFKA_MANAGER.schemaRegistryUrl());
    System.setProperty("kafka.schema.registry.url", KAFKA_MANAGER.schemaRegistryUrl());
    System.setProperty("kafka.bootstrap.servers", KAFKA_MANAGER.bootstrapServers());
  }
}

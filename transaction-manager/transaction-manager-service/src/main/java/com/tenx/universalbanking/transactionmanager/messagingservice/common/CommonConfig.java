package com.tenx.universalbanking.transactionmanager.messagingservice.common;

import static java.lang.String.format;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;

import java.util.Properties;
import java.util.UUID;

public class CommonConfig {

  public static Properties commonConfig(String bootstrapServers, String apiKey, String apiPassword, String securityProtocol, String clientIdPrefix) {

    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
    props.put(SASL_MECHANISM, "PLAIN");
    props.put(SASL_JAAS_CONFIG, String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";", apiKey, apiPassword));
    props.put(SECURITY_PROTOCOL_CONFIG, securityProtocol);

    props.put(REQUEST_TIMEOUT_MS_CONFIG, "20000");
    props.put(RETRY_BACKOFF_MS_CONFIG, "500");
    props.put(CLIENT_ID_CONFIG, format("%s-%s", clientIdPrefix, UUID.randomUUID()));

    return props;
  }
}

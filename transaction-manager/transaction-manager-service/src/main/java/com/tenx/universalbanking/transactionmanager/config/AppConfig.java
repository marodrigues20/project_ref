package com.tenx.universalbanking.transactionmanager.config;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
class AppConfig {

  @Value("${http.proxyHost:#{null}}")
  private String proxyHost;

  @Value("${http.proxyPort:#{null}}")
  private String proxyPort;

  /**
   * Instantiate a RestTemplate providing a request factory from the tracing library.
   * @return RestTemplate instance
   */
  @Bean
  public RestTemplate restTemplate() {

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

    if(proxyHost != null && proxyPort != null) {
      log.info("Setting proxy host{} proxyPort{}",proxyHost,proxyPort);
      httpClientBuilder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
    }

    HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());

    return new RestTemplate(httpComponentsClientHttpRequestFactory);
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().registerModule(new JavaTimeModule());
  }
}
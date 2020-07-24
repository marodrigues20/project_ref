package com.tenx.tsys.proxybatch.config;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestConfig.class);

  @Value("${http.proxyHost:#{null}}")
  private String proxyHost;

  @Value("${http.proxyPort:#{null}}")
  private String proxyPort;

  @Bean
  public RestTemplate restTemplate() {

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

    if (proxyHost != null && proxyPort != null) {
      LOGGER.info("Setting proxyHost {} & proxyPort {} on HttpClient.", proxyHost, proxyPort);
      httpClientBuilder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
    }

    HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory
        = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());

    return new RestTemplate(httpComponentsClientHttpRequestFactory);
  }
}

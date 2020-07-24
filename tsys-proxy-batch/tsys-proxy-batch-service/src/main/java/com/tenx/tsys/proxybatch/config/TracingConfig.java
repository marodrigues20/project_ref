package com.tenx.tsys.proxybatch.config;

import brave.sampler.Sampler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Span;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.xray_udp.XRayUDPReporter;

@Configuration
@EnableConfigurationProperties(TracingProperties.class)
public class TracingConfig {

  private final TracingProperties properties;

  public TracingConfig(final TracingProperties properties) {
    this.properties = properties;
  }

  @Bean
  public Sampler sampling() {
    return Sampler.create(properties.getSamplingRate());
  }

  @Bean
  @ConditionalOnProperty("tracing.xrayAddress")
  public Reporter<Span> xRayReporter() {
    return XRayUDPReporter.create(properties.getXrayAddress());
  }

}

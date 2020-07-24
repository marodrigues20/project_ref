package com.tenx.universalbanking.transactionmanager.config;

import brave.sampler.Sampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Span;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.xray_udp.XRayUDPReporter;

@Configuration
class TracingConfig {

  @Bean
  public Sampler sampling() {
    return Sampler.ALWAYS_SAMPLE;
  }

  @Bean
  public Reporter<Span> xRayReporter() {
    return XRayUDPReporter.create();
  }

}

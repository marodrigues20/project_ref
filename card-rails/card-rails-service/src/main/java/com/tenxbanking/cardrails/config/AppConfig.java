package com.tenxbanking.cardrails.config;

import com.tenxbanking.cardrails.domain.validator.ValidationConfiguration;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ValidationConfiguration.class)
public class AppConfig {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public Supplier<UUID> uuidSupplier() {
    return UUID::randomUUID;
  }
}

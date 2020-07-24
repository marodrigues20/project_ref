package com.tenxbanking.cardrails.util;

import static java.time.ZoneOffset.UTC;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test class to freeze time while running component tests.
 */
@TestConfiguration
public class FixedClock {

  @Bean
  public Clock clock() {
    final Instant instant = LocalDateTime.of(2019, 7, 21, 17, 48).toInstant(UTC);
    return Clock.fixed(instant, UTC);
  }

}
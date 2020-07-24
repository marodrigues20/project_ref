package com.tenxbanking.cardrails.domain.service;

import static java.time.LocalDateTime.of;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class TimeServiceTest {

  private static final Clock CLOCK = Clock.fixed(of(2019, 8, 11, 11, 2).toInstant(UTC), UTC);

  private TimeService unit = new TimeService(CLOCK);

  @Test
  void toInstant_shouldReturnNullWhenValueIsNull() {
    assertThat(unit.toInstant(null)).isNull();
  }

  @Test
  void fromInstant_shouldReturnNullWhenValueIsNull() {
    assertThat(unit.fromInstant(null)).isNull();
  }

  @Test
  void shouldReturnInstantWhenValueIsNotNull() {
    String value = "2018-08-19T10:12:14.000+0000";
    Instant expected = OffsetDateTime.of(2018, 8, 19, 10, 12, 14, 0, UTC).toInstant();
    assertThat(unit.toInstant(value)).isEqualTo(expected);
  }

  @Test
  void shouldReturnStringWhenValueIsNotNull() {
    Instant value = OffsetDateTime.of(2018, 8, 19, 10, 12, 14, 0, UTC).toInstant();
    String expected = "2018-08-19T10:12:14.000+0000";
    assertThat(unit.fromInstant(value)).isEqualTo(expected);
  }

  @Test
  void shouldReturnCurrentInstant() {
    Instant expected = Instant.now(CLOCK);
    Instant actual = unit.now();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldReturnStartCurrentDay() {
    Instant expected = of(2019, 8, 11, 0, 0).toInstant(UTC);
    Instant actual = unit.startCurrentDay();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldReturnEndCurrentDayDay() {
    Instant expected = of(2019, 8, 11, 23, 59, 59, 999999999).toInstant(UTC);
    Instant actual = unit.endCurrentDay();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldReturnStartCurrentMonth() {
    Instant expected = of(2019, 8, 1, 0, 0).toInstant(UTC);
    Instant actual = unit.startCurrentMonth();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldReturnEndCurrentMonth() {
    Instant expected = of(2019, 8, 31, 23, 59, 59, 999999999).toInstant(UTC);
    Instant actual = unit.endCurrentMonth();
    assertThat(actual).isEqualTo(expected);
  }
}
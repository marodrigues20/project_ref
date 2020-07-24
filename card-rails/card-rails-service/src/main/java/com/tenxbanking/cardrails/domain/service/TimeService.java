package com.tenxbanking.cardrails.domain.service;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.Objects.nonNull;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class TimeService {

  private final Clock clock;

  private static final String ISO8601_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static final DateTimeFormatter ISO8601_DATETIME_FORMATTER = ofPattern(
      ISO8601_DATETIME_FORMAT);

  public Instant dateToInstant(final String value) {

    return nonNull(value)
        ? LocalDate.parse(value, DateTimeFormatter.ISO_DATE).atStartOfDay(ZoneId.of("Z")).toInstant()
        : null;
  }

  public Instant toInstant(final String value) {

    return nonNull(value)
        ? OffsetDateTime.parse(value, ISO8601_DATETIME_FORMATTER).toInstant()
        : null;
  }

  public String fromInstant(final Instant value) {

    return nonNull(value)
        ? value.atOffset(UTC).format(ISO8601_DATETIME_FORMATTER)
        : null;
  }

  public Instant now() {
    return Instant.now(clock);
  }

  public Instant startCurrentDay() {
    return LocalDateTime.now(clock).with(LocalTime.MIN).toInstant(UTC);
  }

  public Instant endCurrentDay() {

    return LocalDateTime.now(clock).with(LocalTime.MAX).toInstant(UTC);
  }

  public Instant startCurrentMonth() {

    return LocalDateTime.now(clock).with(firstDayOfMonth()).with(LocalTime.MIN).toInstant(UTC);
  }

  public Instant endCurrentMonth() {

    return LocalDateTime.now(clock).with(lastDayOfMonth()).with(LocalTime.MAX).toInstant(UTC);
  }
}

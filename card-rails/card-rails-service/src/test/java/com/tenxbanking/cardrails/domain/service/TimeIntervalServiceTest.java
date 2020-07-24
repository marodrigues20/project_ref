package com.tenxbanking.cardrails.domain.service;

import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.DAY;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.MONTH;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.TRANSACTION;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.model.limits.TimeInterval;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimeIntervalServiceTest {

  private static final Instant START_INSTANT = Instant.now();
  private static final Instant END_INSTANT = START_INSTANT.plus(Duration.ofDays(1));

  @Mock
  private TimeService timeService;

  @InjectMocks
  private TimeIntervalService unit;

  @Test
  void shouldReturnDailyCheckPeriodForDayResetPeriod() {

    TimeInterval expectedTimeInterval = getExpectedCheckPeriod();

    when(timeService.startCurrentDay()).thenReturn(START_INSTANT);
    when(timeService.endCurrentDay()).thenReturn(END_INSTANT);

    TimeInterval actual = unit.getCheckPeriod(DAY);

    assertThat(actual).isEqualTo(expectedTimeInterval);
  }

  @Test
  void shouldReturnMonthlyCheckPeriodForMonthResetPeriod() {

    TimeInterval expectedTimeInterval = getExpectedCheckPeriod();

    when(timeService.startCurrentMonth()).thenReturn(START_INSTANT);
    when(timeService.endCurrentMonth()).thenReturn(END_INSTANT);

    TimeInterval actual = unit.getCheckPeriod(MONTH);

    assertThat(actual).isEqualTo(expectedTimeInterval);
  }

  @Test
  void shouldThrowExceptionForTransactionResetPeriod() {

    assertThatThrownBy(() -> unit.getCheckPeriod(TRANSACTION))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unsupported reset period: TRANSACTION");
  }

  private static TimeInterval getExpectedCheckPeriod() {

    return TimeInterval.builder()
        .startInstant(START_INSTANT)
        .endInstant(END_INSTANT)
        .build();
  }
}
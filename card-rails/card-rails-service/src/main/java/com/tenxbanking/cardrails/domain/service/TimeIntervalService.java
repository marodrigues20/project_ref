package com.tenxbanking.cardrails.domain.service;

import com.tenxbanking.cardrails.domain.model.limits.TimeInterval;
import com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class TimeIntervalService {

  private final TimeService timeService;

  public TimeInterval getCheckPeriod(@NonNull final ResetPeriodEnums resetPeriod) {

    switch (resetPeriod) {
      case DAY:
        return TimeInterval.builder()
            .startInstant(timeService.startCurrentDay())
            .endInstant(timeService.endCurrentDay())
            .build();

      case MONTH:
        return TimeInterval.builder()
            .startInstant(timeService.startCurrentMonth())
            .endInstant(timeService.endCurrentMonth())
            .build();

      default:
        throw new RuntimeException("Unsupported reset period: " + resetPeriod);
    }
  }
}

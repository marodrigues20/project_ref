package com.tenxbanking.cardrails.domain.model.limits;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class TimeInterval {

  private Instant startInstant;
  private Instant endInstant;
}

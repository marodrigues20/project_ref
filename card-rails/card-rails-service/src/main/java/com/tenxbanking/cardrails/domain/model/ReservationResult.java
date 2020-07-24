package com.tenxbanking.cardrails.domain.model;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class ReservationResult {

  private BigDecimal availableBalance;
  private boolean success;

}

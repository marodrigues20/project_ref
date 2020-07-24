package com.tenxbanking.cardrails.adapter.secondary.ledger.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AmountDto {

  private String currency;
  private BigDecimal value;

}
package com.tenxbanking.cardrails.domain.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class Tax {

  private final BigDecimal taxAmount;
  private final String parentTransactionId;
  private final String transactionId;
  private final String statementDescription;

}

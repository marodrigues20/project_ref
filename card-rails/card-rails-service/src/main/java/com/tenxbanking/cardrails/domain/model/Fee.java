package com.tenxbanking.cardrails.domain.model;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class Fee {

  private final UUID id;
  private final BigDecimal amount;
  private final String description;
  private final String feeCurrencyCode;
  private final String status;
  private final Tax tax;
  private final String transactionCode;
  private final String transactionCorrelationId;
  private final String transactionDate;
  private final String transactionId;
  private final String valueDateTime;

  public Optional<Tax> getTax() {
    return Optional.ofNullable(tax);
  }

}

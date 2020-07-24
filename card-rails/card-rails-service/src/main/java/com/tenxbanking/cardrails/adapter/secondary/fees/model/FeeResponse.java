package com.tenxbanking.cardrails.adapter.secondary.fees.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FeeResponse {

  private BigDecimal amount;
  private String description;
  private String feeCurrencyCode;
  private String parentTransactionId;
  private String status;
  private String subscriptionKey;
  private BigDecimal taxAmount;
  private String transactionCode;
  private String transactionCorrelationId;
  private String transactionDate;
  private String transactionId;
  private String valueDateTime;
}

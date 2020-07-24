package com.tenxbanking.cardrails.adapter.secondary.fees.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FeeTransactionRequest {

  private String subscriptionKey;
  private String transactionCode;
  private BigDecimal transactionAmount;
  private String merchantCategoryCode;
  private String authenticationMethod;
  private String transactionId;
  private String transactionCorrelationId;
  private String currency;
  private String transactionDate;
  private String transactionType;
  private String amountQualifier;
}

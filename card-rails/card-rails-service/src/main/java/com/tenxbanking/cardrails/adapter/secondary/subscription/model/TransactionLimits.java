package com.tenxbanking.cardrails.adapter.secondary.subscription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TransactionLimits {

  @NonNull
  private String transactionName;
  private String minimumAmount;
  private String maximumAmount;
  private String resetPeriod;
}

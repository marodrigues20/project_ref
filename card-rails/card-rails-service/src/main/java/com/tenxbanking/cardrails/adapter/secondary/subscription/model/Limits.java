package com.tenxbanking.cardrails.adapter.secondary.subscription.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Limits {

  private List<TransactionLimits> transactionLimits;
}

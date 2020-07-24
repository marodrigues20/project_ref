package com.tenxbanking.cardrails.adapter.secondary.ledger.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BalanceResponse {

  private BalanceDto balance;

}
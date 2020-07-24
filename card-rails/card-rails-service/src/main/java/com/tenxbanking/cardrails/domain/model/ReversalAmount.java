package com.tenxbanking.cardrails.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ReversalAmount {

  private Money transaction;
  private Money billing;
  private Money settlement;

  public static ReversalAmount of(Money transaction, Money billing, Money settlement) {
    return new ReversalAmount(transaction, billing, settlement);
  }

  public static ReversalAmount of(Money transaction, Money billing) {
    return new ReversalAmount(transaction, billing, null);
  }

  public static ReversalAmount of(Money billing) {
    return new ReversalAmount(null, billing, null);
  }

}

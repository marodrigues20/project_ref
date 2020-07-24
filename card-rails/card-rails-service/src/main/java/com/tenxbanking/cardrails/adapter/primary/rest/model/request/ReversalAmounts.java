package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.ReversalAmount;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReversalAmounts {

  @JsonProperty("transaction")
  private BigDecimal transaction;

  @JsonProperty("settlement")
  private BigDecimal settlement;

  @JsonProperty("billing")
  private BigDecimal billing;

  public ReversalAmount toDomain(Amounts amounts) {
    if (amounts.getSettlement() != null && settlement != null && amounts.getTransaction() != null && transaction != null) {
      return ReversalAmount.of(
          Money.of(transaction, amounts.getTransaction().getCurrency()),
          Money.of(billing, amounts.getBilling().getCurrency()),
          Money.of(settlement, amounts.getSettlement().getCurrency()));
    } if(amounts.getTransaction() != null && transaction != null) {
      return ReversalAmount.of(
          Money.of(transaction, amounts.getTransaction().getCurrency()),
          Money.of(billing, amounts.getBilling().getCurrency()));
    }else{
      return ReversalAmount.of(
          Money.of(billing, amounts.getBilling().getCurrency()));
    }
  }
}


package com.tenx.universalbanking.transactionmanager.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)
public class SettlementResponse {

  @NotNull
  private String settlementStatus;

  public SettlementResponse(){}
  public SettlementResponse(String paymentStatus) {
    this.settlementStatus = paymentStatus;
  }

  public String getSettlementStatus() {
    return settlementStatus;
  }

  public void setSettlementStatus(String settlementStatus) {
    this.settlementStatus = settlementStatus;
  }
}
package com.tenx.universalbanking.transactionmanager.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)
public class CardAuthResponse {

  private ReasonDto reason;

  @NotNull
  private String cardAuthStatus;

  @NotNull
  private TransactionMessage cain002Response;

  public CardAuthResponse() {
  }

  public CardAuthResponse(String cardAuthStatus, TransactionMessage cain002Response) {
    this.cardAuthStatus = cardAuthStatus;
    this.cain002Response = cain002Response;
  }

  public void setReason(ReasonDto reason) {
    this.reason = reason;
  }

  public void setCardAuthStatus(String cardAuthStatus) {
    this.cardAuthStatus = cardAuthStatus;
  }

  public void setCain002Response(
      TransactionMessage cain002Response) {
    this.cain002Response = cain002Response;
  }

  public String getCardAuthStatus() {
    return cardAuthStatus;
  }

  public ReasonDto getReason() {
    return reason;
  }

  public TransactionMessage getCain002Response() {
    return cain002Response;
  }
}

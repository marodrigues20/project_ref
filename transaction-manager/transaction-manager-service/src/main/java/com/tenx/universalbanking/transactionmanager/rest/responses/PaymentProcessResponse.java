package com.tenx.universalbanking.transactionmanager.rest.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)
public class PaymentProcessResponse {

  @NotNull
  private String paymentStatus;

  private ReasonDto reason;

  private TransactionMessage transactionMessage;

  public PaymentProcessResponse() {
  }

  public PaymentProcessResponse(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public PaymentProcessResponse(String paymentStatus, ReasonDto reason) {
    this.paymentStatus = paymentStatus;
    this.reason = reason;
  }

  public PaymentProcessResponse(String paymentStatus, ReasonDto reason,
                                TransactionMessage transactionMessage) {
    this.paymentStatus = paymentStatus;
    this.reason = reason;
    this.transactionMessage = transactionMessage;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public ReasonDto getReason() {
    return reason;
  }

  public void setReason(ReasonDto reason) {
    this.reason = reason;
  }

  public TransactionMessage getTransactionMessage() {
    return transactionMessage;
  }

  public void setTransactionMessage(
      TransactionMessage transactionMessage) {
    this.transactionMessage = transactionMessage;
  }
}

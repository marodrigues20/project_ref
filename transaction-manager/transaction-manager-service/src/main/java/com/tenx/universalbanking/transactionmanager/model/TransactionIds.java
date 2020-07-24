package com.tenx.universalbanking.transactionmanager.model;

class TransactionIds {

  private String transactionId;

  private String correlationId;

  public TransactionIds() {
  }

  public TransactionIds(String transactionId, String correlationId) {
    this.transactionId = transactionId;
    this.correlationId = correlationId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }
}

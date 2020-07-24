package com.tenx.universalbanking.transactionmanager.model;

import java.math.BigDecimal;
import java.util.Date;

public class ApplicationAdjustmentMessageData {

  private final Long transactionId;
  private final Long partyKey;
  private final String productKey;
  private final String correlationId;
  private final String subscriptionKey;
  private final Long tenantPartyKey;
  private final BigDecimal amount;
  private final Date transactionDate;
  private final Date transactionValueDate;

  public ApplicationAdjustmentMessageData(Long transactionId, Long partyKey, String productKey,
      String subscriptionKey, Long tenantPartyKey, BigDecimal amount, Date transactionDate, Date transactionValueDate,
      String correlationId) {
    this.transactionId = transactionId;
    this.partyKey = partyKey;
    this.productKey = productKey;
    this.subscriptionKey = subscriptionKey;
    this.tenantPartyKey = tenantPartyKey;
    this.amount = amount;
    this.transactionDate = transactionDate;
    this.transactionValueDate = transactionValueDate;
    this.correlationId = correlationId;
  }

  public Long getTransactionId() {
    return transactionId;
  }

  public Long getPartyKey() {
    return partyKey;
  }

  public String getSubscriptionKey() {
    return subscriptionKey;
  }

  public Long getTenantPartyKey() {
    return tenantPartyKey;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Date getTransactionValueDate() { return transactionValueDate; }

  public Date getTransactionDate() { return transactionDate; }

  public String getCorrelationId() {
    return correlationId;
  }

  public String getProductKey() {
    return productKey;
  }
}

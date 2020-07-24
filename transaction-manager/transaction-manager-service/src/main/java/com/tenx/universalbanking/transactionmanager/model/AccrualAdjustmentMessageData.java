package com.tenx.universalbanking.transactionmanager.model;

import java.math.BigDecimal;
import java.util.Date;

public class AccrualAdjustmentMessageData {

  private final Long transactionId;
  private final Long partyKey;
  private final String productKey;
  private final String subscriptionKey;
  private final String correlationId;
  private final Date accruedDate;
  private final Date transactionValueDate;
  private final Date transactionDate;
  private final BigDecimal amount;
  private final Date interestCompoundDate;
  private final Date interestApplicationDate;
  private final Boolean isCreateJournal;
  private final Long tenantPartyKey;

  public AccrualAdjustmentMessageData(Long transactionId, Long partyKey, String productKey,
      String subscriptionKey, Long tenantPartyKey, Date accruedDate, Date transactionValueDate, Date transactionDate,
      BigDecimal amount, Date interestCompoundDate, Date interestApplicationDate, Boolean isCreateJournal,
      String correlationId) {
    this.transactionId = transactionId;
    this.partyKey = partyKey;
    this.productKey = productKey;
    this.subscriptionKey = subscriptionKey;
    this.tenantPartyKey = tenantPartyKey;
    this.accruedDate = accruedDate;
    this.transactionValueDate = transactionValueDate;
    this.transactionDate = transactionDate;
    this.amount = amount;
    this.interestCompoundDate = interestCompoundDate;
    this.interestApplicationDate = interestApplicationDate;
    this.isCreateJournal = isCreateJournal;
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

  public Date getAccruedDate() {
    return accruedDate;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Date getInterestCompoundDate() {
    return interestCompoundDate;
  }

  public Date getInterestApplicationDate() {
    return interestApplicationDate;
  }

  public Boolean isCreateJournal() {
    return isCreateJournal;
  }

  public Date getTransactionDate() { return transactionDate; }

  public Date getTransactionValueDate() { return transactionValueDate; }

  public String getCorrelationId() {
    return correlationId;
  }

  public String getProductKey() {
    return productKey;
  }
}

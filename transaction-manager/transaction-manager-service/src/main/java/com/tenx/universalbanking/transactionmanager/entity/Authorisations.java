package com.tenx.universalbanking.transactionmanager.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Authorisations {

  @EmbeddedId
  private AuthorisationId id;

  @Column(nullable = false, length = 64)
  private String transactionId;

  @Column(nullable = false, length = 64)
  private String correlationId;

  @Column(nullable = false)
  private String tsysAccountId;

  @Column(nullable = false)
  @JsonFormat
  private BigDecimal transactionAmount;

  @Column(nullable = false)
  private String mcc;

  @Column(nullable = false)
  private String transactionType;

  @Column
  private boolean matched;

  @Column
  private boolean expired;

  public AuthorisationId getId() {
    return id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public String getTsysAccountId() {
    return tsysAccountId;
  }

  public BigDecimal getTransactionAmount() {
    return transactionAmount;
  }

  public void setId(AuthorisationId id) {
    this.id = id;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public void setTsysAccountId(String tsysAccountId) {
    this.tsysAccountId = tsysAccountId;
  }

  public void setTransactionAmount(BigDecimal transactionAmount) {
    this.transactionAmount = transactionAmount;
  }

  public void setMcc(String mcc) {
    this.mcc = mcc;
  }

  public void setMatched(boolean matched) {
    this.matched = matched;
  }

  public String getMcc() {

    return mcc;
  }

  public boolean isMatched() {
    return matched;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public boolean isExpired() {
    return expired;
  }

  public void setExpired(boolean expired) {
    this.expired = expired;
  }

  public String getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(String transactionType) {
    this.transactionType = transactionType;
  }
}

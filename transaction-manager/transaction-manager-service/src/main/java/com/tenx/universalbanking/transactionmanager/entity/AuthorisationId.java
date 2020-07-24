package com.tenx.universalbanking.transactionmanager.entity;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class AuthorisationId implements Serializable {

  @Column(nullable = false, length = 9)
  private String bankNetReferenceNumber;

  @Column(nullable = false)
  private LocalDate transactionDate;

  @Column(nullable = false, length = 9)
  private String authorisationCode;

  public String getBankNetReferenceNumber() {
    return bankNetReferenceNumber;
  }

  public LocalDate getTransactionDate() {
    return transactionDate;
  }

  public String getAuthorisationCode() {
    return authorisationCode;
  }

  public void setBankNetReferenceNumber(String bankNetReferenceNumber) {
    this.bankNetReferenceNumber = bankNetReferenceNumber;
  }

  public void setTransactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
  }

  public void setAuthorisationCode(String authorisationCode) {
    this.authorisationCode = authorisationCode;
  }
}

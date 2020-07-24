package com.tenx.universalbanking.transactionmanager.enums;

public enum TransactionType {

  PURCHASE_CASH_BACK("CSHW"),
  CASH_WITHDRAWAL("CHWD"),
  QUASI_CASH("QUCH"),
  PURCHASE("CRDP"),
  CASH_DEPOSIT("CSHD"),
  REFUND("RFND");

  private final String code;

  TransactionType(String code) {
    this.code = code;
  }

  public String getTransactionCode() {
    return code;
  }
}

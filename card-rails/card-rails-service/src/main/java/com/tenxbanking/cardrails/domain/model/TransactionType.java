package com.tenxbanking.cardrails.domain.model;

import lombok.Getter;

@Getter
public enum TransactionType {

  PURCHASE("00", "CRDP"),
  WITHDRAWAL("01", "CHWD"),
  DEBIT_ADJUSTMENT("02", "DADJ"),
  PURCHASE_WITH_CASHBACK("09", "CSHB"),
  VISA_ONLY_FUNDING("10", "LOAD"),
  CASH_DISBURSEMENT("17", "CSHW"),
  SCRIPT_ISSUE("18", "Not Applicable"),
  PURCHASE_REFUND("20", "RFND"),
  DEPOSIT("21", "CSHD"),
  CREDIT_ADJUSTMENT("22", "CADJ"),
  CHEQUE_DEPOSIT_GUARANTEE("23", "Not Applicable"),
  CHEQUE_DEPOSIT("24", "Not Applicable"),
  PAYMENT("28", "CRDP"),
  BALANCE_ENQUIRY("30", "BALC"),
  ACCOUNT_TRANSFER("40", "Not Applicable"),
  RESERVED_FOR_FUTURE_USE("90", "Not Applicable"),
  PIN_UNBLOCK("91", "PINU"),
  PIN_CHANGE("92", "PINC");

  private String id;
  private String code;

  TransactionType(String id, String code) {
    this.id = id;
    this.code = code;
  }

  public String getTransactionId() {
    return id;
  }

}

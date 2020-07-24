package com.tenx.universalbanking.transactionmanager.enums;

import static com.tenx.universalbanking.transactionmanager.constants.TransactionManagerResponseCodes.GENERIC_FAILURE_CODE;

public enum TransactionReason {

  GENERIC_FAILURE(GENERIC_FAILURE_CODE,"Transaction failed");

  private final int failureCode;

  private final String failureMessage;

  public int getFailureCode() {
    return failureCode;
  }

  public String getFailureMessage() {
    return failureMessage;
  }

  TransactionReason(int code, String message) {
    this.failureCode = code;
    this.failureMessage = message;
  }

}

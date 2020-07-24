package com.tenx.universalbanking.transactionmanager.exception;

public class NoValidTransactionHandlerException extends RuntimeException {

  public NoValidTransactionHandlerException(String message) {
    super(message);
  }
}

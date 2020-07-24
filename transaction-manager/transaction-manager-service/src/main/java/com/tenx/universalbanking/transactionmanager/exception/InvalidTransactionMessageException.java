package com.tenx.universalbanking.transactionmanager.exception;

public class InvalidTransactionMessageException extends RuntimeException {

  public InvalidTransactionMessageException(String message) {
    super(message);
  }

  public InvalidTransactionMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}

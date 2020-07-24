package com.tenx.universalbanking.transactionmanager.exception;

public class RequestValidationFailureException extends RuntimeException {

  public RequestValidationFailureException(String message) {
    super(message);
  }
}

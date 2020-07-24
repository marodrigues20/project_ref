package com.tenxbanking.cardrails.adapter.primary.rest.exception;

public class RequestValidationFailureException extends RuntimeException {

  public RequestValidationFailureException(String message) {
    super(message);
  }
}

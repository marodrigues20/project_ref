package com.tenxbanking.cardrails.domain.exception;

public class LimitConstraintException extends RuntimeException {

  public LimitConstraintException(String message) {
    super(message);
  }
}

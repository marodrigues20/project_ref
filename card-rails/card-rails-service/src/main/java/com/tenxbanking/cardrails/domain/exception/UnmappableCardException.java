package com.tenxbanking.cardrails.domain.exception;

import lombok.NonNull;

public class UnmappableCardException extends RuntimeException {

  public UnmappableCardException(@NonNull final String message, Throwable cause) {
    super(message, cause);
  }
}
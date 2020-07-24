package com.tenxbanking.cardrails.domain.exception;

import lombok.NonNull;

public class InvalidSubscriptionStateException extends RuntimeException {

  public InvalidSubscriptionStateException(@NonNull final String message) {
    super(message);
  }
}

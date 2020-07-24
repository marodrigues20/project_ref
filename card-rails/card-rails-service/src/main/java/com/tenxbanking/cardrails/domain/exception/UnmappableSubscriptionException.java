package com.tenxbanking.cardrails.domain.exception;

import lombok.NonNull;

public class UnmappableSubscriptionException extends RuntimeException {

  public UnmappableSubscriptionException(@NonNull final String message) {
    super(message);
  }
}

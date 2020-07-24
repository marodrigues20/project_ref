package com.tenx.universalbanking.transactionmanager.exception;

import lombok.NonNull;

public class UnmappableCardException extends RuntimeException {

  public UnmappableCardException(@NonNull final String message) {
    super(message);
  }
}
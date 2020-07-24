package com.tenx.universalbanking.transactionmanager.exception;

import lombok.NonNull;

public class CAIN002NotReceivedException extends RuntimeException {

  public CAIN002NotReceivedException(@NonNull final String message) {
    super(message);
  }
}
package com.tenx.universalbanking.transactionmanager.exception;

import lombok.NonNull;

public class CardNotFoundException extends RuntimeException {

  public CardNotFoundException(@NonNull final String message) {
    super(message);
  }
}
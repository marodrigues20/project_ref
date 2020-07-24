package com.tenxbanking.cardrails.domain.exception;

import lombok.NonNull;

//TODO: clarify with @Ell/@Adam - should we promote these domain exceptions to be checked exceptions, instead of unchecked?
public class InvalidCardStateException extends RuntimeException {

  public InvalidCardStateException(@NonNull final String message) {
    super(message);
  }
}

package com.tenxbanking.cardrails.domain.exception;

public class CardNotFoundException extends RuntimeException {

  private static final String MESSAGE = "Card cannot be found";

  public CardNotFoundException() {
    super(MESSAGE);
  }

}

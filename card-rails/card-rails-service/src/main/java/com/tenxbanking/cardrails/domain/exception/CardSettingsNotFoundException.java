package com.tenxbanking.cardrails.domain.exception;

public class CardSettingsNotFoundException extends RuntimeException {

  private static final String MESSAGE = "Card settings cannot be found";

  public CardSettingsNotFoundException() {
    super(MESSAGE);
  }

}

package com.tenxbanking.cardrails.domain.exception;

public class CardClearingSubscriptionNotFoundException extends RuntimeException {

  private static final String MESSAGE = "Subscription cannot be found";

  public CardClearingSubscriptionNotFoundException() {
    super(MESSAGE);
  }
}

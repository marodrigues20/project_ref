package com.tenxbanking.cardrails.domain.exception;

public class SubscriptionNotFoundException extends RuntimeException {

  private static final String MESSAGE = "Subscription cannot be found";

  public SubscriptionNotFoundException() {
    super(MESSAGE);
  }

}

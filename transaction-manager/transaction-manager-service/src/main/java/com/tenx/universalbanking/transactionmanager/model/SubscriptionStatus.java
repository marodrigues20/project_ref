package com.tenx.universalbanking.transactionmanager.model;

import lombok.NonNull;

public enum SubscriptionStatus {
  ACTIVE,
  CLOSURE_PENDING,
  CLOSED;

  public static SubscriptionStatus fromString(@NonNull final String value) {

    for (SubscriptionStatus enumValue : SubscriptionStatus.values()) {
      if (enumValue.name().equalsIgnoreCase(value)) {
        return enumValue;
      }
    }
    return null;
  }
}

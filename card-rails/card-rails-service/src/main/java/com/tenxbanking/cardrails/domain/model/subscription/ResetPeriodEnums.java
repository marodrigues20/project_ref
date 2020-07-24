package com.tenxbanking.cardrails.domain.model.subscription;

import lombok.NonNull;

public enum ResetPeriodEnums {
  DAY,
  MONTH,
  TRANSACTION;

  public static ResetPeriodEnums fromString(@NonNull final String value) {

    for (ResetPeriodEnums enumValue : ResetPeriodEnums.values()) {
      if (enumValue.name().equalsIgnoreCase(value)) {
        return enumValue;
      }
    }
    return null;
  }

  public boolean isTransaction() {
    return TRANSACTION.equals(this);
  }
}

package com.tenxbanking.cardrails.domain.model.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum TransactionNameEnums {

  ATMWITHDRAWAL("ATMwithdrawal"),
  TRANSFERIN("transferIn"),
  TRANSFEROUT("transferOut");

  private final String value;

  public static TransactionNameEnums fromString(@NonNull final String value) {

    for (TransactionNameEnums enumValue : TransactionNameEnums.values()) {
      if (enumValue.value.equalsIgnoreCase(value)) {
        return enumValue;
      }
    }
    return null;
  }

  public boolean isAtmWithdrawal() {
    return ATMWITHDRAWAL.equals(this);
  }
}

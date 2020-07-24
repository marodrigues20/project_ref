package com.tenxbanking.cardrails.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum FundingMechanismEnums {

  CARD("CARD"),
  TRANSFER("TRANSFER");

  private final String value;

  public static FundingMechanismEnums fromString(@NonNull final String value) {

    for (FundingMechanismEnums enumValue : FundingMechanismEnums.values()) {
      if (enumValue.value.equalsIgnoreCase(value)) {
        return enumValue;
      }
    }
    return null;
  }
}
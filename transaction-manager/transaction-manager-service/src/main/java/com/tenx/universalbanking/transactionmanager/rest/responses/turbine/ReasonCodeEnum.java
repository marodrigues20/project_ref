package com.tenx.universalbanking.transactionmanager.rest.responses.turbine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReasonCodeEnum {
  _00("00"),
  _05("05");

  private final String value;

  ReasonCodeEnum(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return value;
  }

  @JsonCreator
  public static ReasonCodeEnum fromValue(String value) {
    for (ReasonCodeEnum enumValue : ReasonCodeEnum.values()) {
      if (enumValue.value.equalsIgnoreCase(value)) {
        return enumValue;
      }
    }
    return null;
  }
}

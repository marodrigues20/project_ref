package com.tenxbanking.cardrails.adapter.primary.rest.model.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RejectReasonEnum {
  _0000("0000"),
  _0997("0997"),
  _1000("1000"),
  _1001("1001"),
  _1002("1002");

  private String value;

  RejectReasonEnum(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static RejectReasonEnum fromValue(String text) {
    for (RejectReasonEnum b : RejectReasonEnum.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

package com.tenx.universalbanking.transactionmanager.rest.request.turbine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageTypeEnum {
  AUTHORISATION("Authorisation"),

  ADVICE("Advice"),

  REVERSAL("Reversal");

  private String value;

  MessageTypeEnum(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageTypeEnum fromValue(String text) {
    for (MessageTypeEnum b : MessageTypeEnum.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

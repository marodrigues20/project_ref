package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MessageTypeEnum {
  AUTHORISATION("Authorisation"),

  ADVICE("Advice"),

  REVERSAL("Reversal");

  private String value;

  MessageTypeEnum(String value) {
    this.value = value;
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

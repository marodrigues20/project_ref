package com.tenxbanking.cardrails.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum AuthResponseCode {

  _00("00", true),
  _01("01"),
  _02("02"),
  _03("03"),
  _04("04"),
  _05("05"),
  _06("06"),
  _07("07"),
  _08("08"),
  _09("09"),
  _10("10"),
  _11("11"),
  _12("12"),
  _13("13"),
  _14("14"),
  _15("15"),
  _16("16"),
  _17("17"),
  _19("19"),
  _20("20"),
  _21("21"),
  _22("22"),
  _25("25"),
  _28("28"),
  _30("30"),
  _41("41"),
  _43("43"),
  _51("51"),
  _52("52"),
  _53("53"),
  _54("54"),
  _55("55"),
  _57("57"),
  _58("58"),
  _59("59"),
  _61("61"),
  _62("62"),
  _63("63"),
  _65("65"),
  _68("68"),
  _75("75"),
  _76("76"),
  _77("77"),
  _78("78"),
  _80("80"),
  _81("81"),
  _82("82"),
  _83("83"),
  _85("85"),
  _91("91"),
  _92("92"),
  _93("93"),
  _94("94"),
  _95("95"),
  _96("96");

  private final static Map<String, AuthResponseCode> VALUE_MAP;

  private final String value;
  private final boolean isSuccess;

  AuthResponseCode(
      String value,
      boolean isSuccess) {
    this.value = value;
    this.isSuccess = isSuccess;
  }

  AuthResponseCode(String value) {
    this.value = value;
    this.isSuccess = false;
  }

  @Override
  @JsonValue
  public String toString() {
    return value;
  }

  public static AuthResponseCode getByValue(String value) {
    return VALUE_MAP.get(value);
  }

  static {
    VALUE_MAP = Stream.of(values())
        .collect(Collectors.toMap(AuthResponseCode::getValue, e -> e));
  }

  public boolean isSuccess() {
    return isSuccess;
  }
}


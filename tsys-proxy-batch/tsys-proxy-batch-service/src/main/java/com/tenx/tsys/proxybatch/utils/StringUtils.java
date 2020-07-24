package com.tenx.tsys.proxybatch.utils;

public class StringUtils {

  private StringUtils() {
  }

  public static String maskMessage(String messageToMask) {
    if (messageToMask != null) {
      return messageToMask.replaceAll("\\d(?=\\d{4})", "X");
    } else {
      return null;
    }
  }
}

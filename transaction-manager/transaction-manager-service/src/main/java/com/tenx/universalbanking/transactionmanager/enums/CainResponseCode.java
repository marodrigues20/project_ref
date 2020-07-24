package com.tenx.universalbanking.transactionmanager.enums;

public enum CainResponseCode {

  APPR,
  DECL;

  public String value() {
    return this.name();
  }

  public static CainResponseCode fromValue(String v) {
    return valueOf(v);
  }
}

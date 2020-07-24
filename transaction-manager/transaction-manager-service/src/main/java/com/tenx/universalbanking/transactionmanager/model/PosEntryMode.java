package com.tenx.universalbanking.transactionmanager.model;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

public enum PosEntryMode {

  UNKNOWN("00", "UNKW"),
  MANUAL_KEY_ENTRY("01", "PHYS"),
  MAG_STRIPE("02", "MGST"),
  PAN_ENTRY_VIA_BAR_CODE("03", "BRCD"),
  PAN_ENTRY_VIA_OCR("04", "PHYS"),
  ICC_READ("05", "CICC"),
  PAN_ENTRY_VIA_CHIP("06", "PHYS"),
  ICC_CONTACTLESS("07", "CICC"),
  PAN_ENTRY_VIA_CONTACTLESS("08", "CTLS"),
  HYBRID_TERMINAL("79", "UNKW"),
  MAG_STRIPE_FALL_BACK("80", "MGST"),
  PAN_VIA_ECOMMERCE("81", "PHYS"),
  MAG_STRIPE_TRACK("90", "MGST"),
  CONTACTLESS_USING_TRACK_RULES("91", "CTLS"),
  CONTACTLESS_INPUT("92", "CTLS"),
  ICC_NO_CVV_CHECKING("95", "CICC");

  private String tisoCode;
  private String cainCode;

  private static final Map<String, String> posEntryMap;

  PosEntryMode(String tisoCode, String cainCode) {
    this.tisoCode = tisoCode;
    this.cainCode = cainCode;
  }

  public String getTisoCode() {
    return tisoCode;
  }

  private String getCainCode() {
    return cainCode;
  }

  public static String convertPosEntryCode(String tisoCode) {
    return posEntryMap.get(tisoCode);
  }

  public static String convertTransactionTypeToId(String transactionCode) {
    final String[] tisoCode = new String[1];
    posEntryMap.entrySet()
      .stream()
      .filter(transactionType -> transactionCode.equalsIgnoreCase(transactionType.getValue()))
      .findFirst()
      .ifPresent(posEntry -> tisoCode[0] = posEntry.getKey());

    return tisoCode[0];
  }

  static {
    posEntryMap = stream(values())
      .collect(toMap(PosEntryMode::getTisoCode, PosEntryMode::getCainCode));
  }
}

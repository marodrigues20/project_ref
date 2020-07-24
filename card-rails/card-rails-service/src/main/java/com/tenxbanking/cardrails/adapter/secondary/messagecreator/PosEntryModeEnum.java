package com.tenxbanking.cardrails.adapter.secondary.messagecreator;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PosEntryModeEnum {
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


  private final String posCode;
  private final String cainCode;
  public static Map<String, String> posEntryMap = (Map) Arrays.stream(values()).collect(Collectors.toMap(PosEntryModeEnum::getPosCode, PosEntryModeEnum::getCainCode));

  PosEntryModeEnum(String posCode, String cainCode) {
    this.posCode = posCode;
    this.cainCode = cainCode;
  }

  public String getPosCode() {
    return this.posCode;
  }

  public String getCainCode() {
    return this.cainCode;
  }

  public static String convertPosEntryCode(String posCode) {
    return posEntryMap.get(posCode);
  }
}
package com.tenx.tsys.proxybatch.utils;

import static java.util.Currency.getAvailableCurrencies;
import static java.util.Currency.getInstance;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class CurrencyUtil {

  private static Map<Integer, Currency> currencyMap = new HashMap<>();

  static {
    getAvailableCurrencies()
        .forEach(currency -> currencyMap.put(currency.getNumericCode(), currency));
  }

  private CurrencyUtil() {
  }

  public static String getCurrencyCode(int numericCode) {
    return currencyMap.get(numericCode) == null ? null
        : currencyMap.get(numericCode).getCurrencyCode();
  }

  public static Currency getCurrencyInstance(int numericCode) {
    return currencyMap.get(numericCode);
  }

  public static int getCurrencyNumericCode(String currencyCode) {
    return getInstance(currencyCode).getNumericCode();
  }

}

package com.tenxbanking.cardrails.domain.util;

import com.tenxbanking.cardrails.domain.exception.CurrencyValidationException;
import java.util.Currency;

public class CurrencyHelper {

  public static Currency findCurrencyFromCode(int code) {

    return Currency.getAvailableCurrencies().stream().filter(
        (Currency c) -> currencyFromCode(c, code)).findAny().orElseThrow(() ->
        new CurrencyValidationException("no currency found for code[" + code + "]"));
  }

  public static Currency findCurrencyFromCode(String code) {
    try {
      return Currency.getInstance(code);
    } catch (Exception e) {
      throw new CurrencyValidationException("invalid currency code value[" + code + "]");
    }
  }

  private static boolean currencyFromCode(Currency c, int code) {
    return c.getNumericCode() == code;
  }

}

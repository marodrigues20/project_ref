package com.tenxbanking.cardrails.domain.model;

import com.tenxbanking.cardrails.domain.util.CurrencyHelper;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@ToString
@Getter
@Builder
@AllArgsConstructor
public class Money {

  @NonNull
  private final BigDecimal amount;
  @NonNull
  private final Currency currency;

  public static Money of(@NonNull BigDecimal amount, int currency) {
    return Money.of(amount, CurrencyHelper.findCurrencyFromCode(currency));
  }

  public static Money of(@NonNull BigDecimal amount, @NonNull String currency) {
    if (isNumeric(currency)) {
      return new Money(amount, CurrencyHelper.findCurrencyFromCode(Integer.parseInt(currency)));
    } else {
      return Money.of(amount, CurrencyHelper.findCurrencyFromCode(currency));
    }
  }

  public static Money of(double amount, @NonNull String currency) {
    return of(BigDecimal.valueOf(amount), CurrencyHelper.findCurrencyFromCode(currency));
  }

  public static Money of(@NonNull BigDecimal amount, @NonNull Currency currency) {
    return new Money(amount, currency);
  }

  public static Money of(double amount, @NonNull Currency currency) {
    return new Money(BigDecimal.valueOf(amount), currency);
  }

  public String getCurrencyCode() {
    return currency.getCurrencyCode();
  }

  private static boolean isNumeric(String currency) {
    try {
      Integer.parseInt(currency);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Money money = (Money) o;
    return amount.compareTo(money.amount) == 0
        && Objects.equals(currency, money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }

}

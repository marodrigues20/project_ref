package com.tenxbanking.cardrails.domain.model;

import static com.tenxbanking.cardrails.domain.model.AuthResponseCode._00;
import static com.tenxbanking.cardrails.domain.model.AuthResponseCode._05;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.security.SecureRandom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class Cain002 {

  private final String authCode;
  private final Money updatedBalance;
  @NonNull
  private final AuthResponseCode authResponseCode;
  private final boolean isSuccess;

  public static Cain002 unsuccessful() {
    return new Cain002(
        null,
        null,
        AuthResponseCode._05,
        false
    );
  }

  public static Cain002 unsuccessful(Cain001 cain001) {
    return new Cain002(
        ofNullable(cain001.getAuthCode()).orElse(null),
        null,
        ofNullable(cain001.getAuthResponseCode()).orElse(_05),
        false
    );
  }

  public static Cain002 successful(Cain001 cain001, BigDecimal updatedBalance) {
    return new Cain002(
        ofNullable(cain001.getAuthCode()).orElse(generate6DigitAuthCode()),
        Money.of(updatedBalance, cain001.getCurrency()),
        ofNullable(cain001.getAuthResponseCode()).orElse(_00),
        true
    );
  }

  private static String generate6DigitAuthCode() {
    return String.valueOf(100000 + new SecureRandom().nextInt(900000));
  }
}

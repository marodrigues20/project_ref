package com.tenxbanking.cardrails.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenxbanking.cardrails.domain.exception.CurrencyValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class MoneyTest {

  @Test
  void comparesBigDecimalForEqualTo() {
    Money oneGbp = Money.of(BigDecimal.valueOf(1), 826);
    Money onePointZeroGbp = Money.of(BigDecimal.valueOf(1.0), 826);

    assertThat(oneGbp).isEqualTo(onePointZeroGbp);
  }

  @Test
  void getCurrencyCode() {
    Money oneGbp = Money.of(BigDecimal.valueOf(1), 826);

    assertThat(oneGbp.getCurrencyCode()).isEqualTo("GBP");
  }

  @Test
  void getBadCurrencyCode() {
    Assertions.assertThrows(CurrencyValidationException.class, () -> {
      Money oneGbp = Money.of(BigDecimal.valueOf(1), "XYZ");
    });
  }

}
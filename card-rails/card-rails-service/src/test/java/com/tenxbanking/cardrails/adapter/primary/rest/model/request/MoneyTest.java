package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void toDomain() {
    Money dto = new Money(BigDecimal.valueOf(1), "GBP");

    com.tenxbanking.cardrails.domain.model.Money domain = dto.toDomain();

    assertThat(domain.getCurrency().getNumericCode()).isEqualTo(826);
    assertThat(domain.getAmount()).isEqualTo(BigDecimal.valueOf(1));
  }

  @Test
  void fromDomain() {
    com.tenxbanking.cardrails.domain.model.Money domain = com.tenxbanking.cardrails.domain.model.Money.of(BigDecimal.valueOf(1), "GBP");

    Money dto = Money.fromDomain(domain);

    assertThat(dto.getCurrency()).isEqualTo("826");
    assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(1));
  }

}
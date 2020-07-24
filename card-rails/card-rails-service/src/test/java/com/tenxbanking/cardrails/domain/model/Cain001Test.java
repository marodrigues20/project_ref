package com.tenxbanking.cardrails.domain.model;

import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_001;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class Cain001Test {

  @Test
  void shouldAddFee() {
    Fee fee = Fee.builder().build();

    Cain001 newCain001 = CAIN_001.addFee(fee);

    assertThat(newCain001.getFee()).isPresent();
    assertThat(newCain001.getFee().get()).isEqualTo(fee);
  }

  @Test
  void shouldAddTransactionId() {
    UUID transactionId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();

    Cain001 newCain001 = CAIN_001.addTransactionIds(transactionId, correlationId);

    assertThat(newCain001.getTransactionId()).isEqualTo(transactionId);
    assertThat(newCain001.getCorrelationId()).isEqualTo(correlationId);
  }

  @Test
  void getCurrencyCode() {
    Cain001 cain001 = CAIN_001.toBuilder()
        .transactionAmount(Money.of(12, "EUR"))
        .build();
    assertThat(cain001.getCurrencyCode()).isEqualTo("EUR");
  }

  @Test
  void getCurrency() {
    Cain001 cain001 = CAIN_001.toBuilder()
        .transactionAmount(Money.of(12, "EUR"))
        .build();
    assertThat(cain001.getCurrency()).isEqualTo(Currency.getInstance("EUR"));
  }

  @Test
  void shouldReturnTrueWhenSettlementIsPresent() {
    Money money = Money.of(BigDecimal.ONE, 826);
    Cain001 cain001 = CAIN_001.toBuilder().reversalAmount(ReversalAmount.of(money, money, money))
        .build();

    assertThat(cain001.isThereSettlement()).isTrue();
  }

  @Test
  void shouldReturnFalseWhenSettlementIsPresent() {
    Money money = Money.of(BigDecimal.ONE, 826);
    Cain001 cain001 = CAIN_001.toBuilder().reversalAmount(ReversalAmount.of(money, money)).build();

    assertThat(cain001.isThereSettlement()).isFalse();
  }

}
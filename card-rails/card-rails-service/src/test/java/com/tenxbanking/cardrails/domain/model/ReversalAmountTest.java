package com.tenxbanking.cardrails.domain.model;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReversalAmountTest {

  private static final Money TRANSACTION = Money.of(ONE, 826);
  private static final Money BILLING = Money.of(TEN, 826);
  private static final Money SETTLEMENT = Money.of(ZERO, 826);

  @Test
  void shouldCreateWithTransactionBillingAndNullSettlement() {

    ReversalAmount actual = ReversalAmount.of(TRANSACTION, BILLING);
    assertThat(actual.getTransaction()).isEqualTo(TRANSACTION);
    assertThat(actual.getBilling()).isEqualTo(BILLING);
    assertThat(actual.getSettlement()).isNull();
  }

  @Test
  void shouldCreateWithTransactionBillingAndSettlement() {

    ReversalAmount actual = ReversalAmount.of(TRANSACTION, BILLING, SETTLEMENT);
    assertThat(actual.getTransaction()).isEqualTo(TRANSACTION);
    assertThat(actual.getBilling()).isEqualTo(BILLING);
    assertThat(actual.getSettlement()).isEqualTo(SETTLEMENT);
  }

}
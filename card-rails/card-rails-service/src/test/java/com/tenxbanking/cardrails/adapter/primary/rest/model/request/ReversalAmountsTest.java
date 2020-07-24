package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import static com.tenxbanking.cardrails.domain.TestConstant.GBP;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

import com.tenxbanking.cardrails.domain.model.ReversalAmount;
import org.junit.jupiter.api.Test;

class ReversalAmountsTest {

  @Test
  void shouldCreateDomainObject() {
    ReversalAmounts reversalAmounts = new ReversalAmounts(ONE, TEN, ZERO);

    ReversalAmount reversalAmount = reversalAmounts.toDomain(createAmounts(new Money(TEN, "GBP")));

    assertThat(reversalAmount.getTransaction().getAmount()).isOne();
    assertThat(reversalAmount.getTransaction().getCurrency()).isEqualTo(GBP);
    assertThat(reversalAmount.getSettlement().getAmount()).isEqualTo(TEN);
    assertThat(reversalAmount.getSettlement().getCurrency()).isEqualTo(GBP);
    assertThat(reversalAmount.getBilling().getAmount()).isEqualTo(ZERO);
    assertThat(reversalAmount.getBilling().getCurrency()).isEqualTo(GBP);
  }

  @Test
  void shouldCreateDomainObjectWhenNoSettlement() {
    ReversalAmounts reversalAmounts = new ReversalAmounts(ONE, null, ZERO);

    ReversalAmount reversalAmount = reversalAmounts.toDomain(createAmounts(new Money(TEN, "GBP")));

    assertThat(reversalAmount.getTransaction().getAmount()).isOne();
    assertThat(reversalAmount.getTransaction().getCurrency()).isEqualTo(GBP);
    assertThat(reversalAmount.getSettlement()).isNull();
    assertThat(reversalAmount.getSettlement()).isNull();
    assertThat(reversalAmount.getBilling().getAmount()).isEqualTo(ZERO);
    assertThat(reversalAmount.getBilling().getCurrency()).isEqualTo(GBP);
  }

  @Test
  void shouldCreateDomainObjectWhenNoSettlementOnAmounts() {
    ReversalAmounts reversalAmounts = new ReversalAmounts(ONE, null, ZERO);

    ReversalAmount reversalAmount = reversalAmounts.toDomain(createAmounts(null));

    assertThat(reversalAmount.getTransaction().getAmount()).isOne();
    assertThat(reversalAmount.getTransaction().getCurrency()).isEqualTo(GBP);
    assertThat(reversalAmount.getSettlement()).isNull();
    assertThat(reversalAmount.getSettlement()).isNull();
    assertThat(reversalAmount.getBilling().getAmount()).isEqualTo(ZERO);
    assertThat(reversalAmount.getBilling().getCurrency()).isEqualTo(GBP);
  }


  private Amounts createAmounts(Money settlement) {
    Amounts amounts = new Amounts();
    amounts.setBilling(new Money(ONE, "GBP"));
    amounts.setTransaction(new Money(ZERO, "GBP"));
    amounts.setSettlement(settlement);
    return amounts;
  }

}
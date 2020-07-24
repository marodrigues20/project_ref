package com.tenxbanking.cardrails.domain.model.subscription;

import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.DAY;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.MONTH;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.TRANSACTION;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResetPeriodEnumsTest {

  @Test
  void isTransactionForTransactionValue() {
    assertThat(TRANSACTION.isTransaction()).isTrue();
  }

  @Test
  void isTransactionForDayValue() {
    assertThat(DAY.isTransaction()).isFalse();
  }

  @Test
  void isTransactionForMonthValue() {
    assertThat(MONTH.isTransaction()).isFalse();
  }
}
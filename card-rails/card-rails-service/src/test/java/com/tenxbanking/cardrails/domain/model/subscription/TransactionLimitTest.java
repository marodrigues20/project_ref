package com.tenxbanking.cardrails.domain.model.subscription;

import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.DAY;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.MONTH;
import static com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums.ATMWITHDRAWAL;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransactionLimitTest {


  @Test
  void shouldReturnDayWhenResetPeriodIsDay() {

    TransactionLimit transactionLimit = TransactionLimit.builder()
        .transactionName(ATMWITHDRAWAL)
        .resetPeriod(DAY)
        .build();

    assertThat(transactionLimit.getResetPeriod()).isEqualTo(DAY);
  }

  @Test
  void shouldReturnMonthWhenResetPeriodIsMonth() {

    TransactionLimit transactionLimit = TransactionLimit.builder()
        .transactionName(ATMWITHDRAWAL)
        .resetPeriod(MONTH)
        .build();

    assertThat(transactionLimit.getResetPeriod()).isEqualTo(MONTH);
  }
}
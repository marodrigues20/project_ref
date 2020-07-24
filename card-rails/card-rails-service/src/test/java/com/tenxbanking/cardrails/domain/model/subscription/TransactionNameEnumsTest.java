package com.tenxbanking.cardrails.domain.model.subscription;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransactionNameEnumsTest {

  @Test
  void isAtmWithdrawalForAtmWithdrawalValue() {
    assertThat(TransactionNameEnums.ATMWITHDRAWAL.isAtmWithdrawal()).isTrue();
  }

  @Test
  void isAtmWithdrawalForTransferInValue() {
    assertThat(TransactionNameEnums.TRANSFERIN.isAtmWithdrawal()).isFalse();
  }

  @Test
  void isAtmWithdrawalForTransferOutValue() {
    assertThat(TransactionNameEnums.TRANSFEROUT.isAtmWithdrawal()).isFalse();
  }
}
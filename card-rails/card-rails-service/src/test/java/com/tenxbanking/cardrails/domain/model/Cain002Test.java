package com.tenxbanking.cardrails.domain.model;

import static com.tenxbanking.cardrails.domain.TestConstant.GBP;
import static com.tenxbanking.cardrails.domain.model.AuthResponseCode._00;
import static com.tenxbanking.cardrails.domain.model.AuthResponseCode._05;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Cain002Test {

  @Test
  void unsuccessful() {
    Cain002 cain002 = Cain002.unsuccessful();

    assertThat(cain002.getUpdatedBalance()).isNull();
    assertThat(cain002.getAuthCode()).isNull();
    assertThat(cain002.getAuthResponseCode()).isEqualTo(AuthResponseCode._05);
    assertThat(cain002.isSuccess()).isFalse();
  }


  @Test
  void unsuccessful_givenAuthCain001() {
    Cain002 cain002 = Cain002.unsuccessful(mock(Cain001.class));

    assertThat(cain002.getUpdatedBalance()).isNull();
    assertThat(cain002.getAuthCode()).isNull();
    assertThat(cain002.getAuthResponseCode()).isEqualTo(AuthResponseCode._05);
    assertThat(cain002.isSuccess()).isFalse();
  }

  @Test
  void unsuccessful_givenAdviceOrReversalCain001() {
    Cain001 cain001 = mock(Cain001.class);
    when(cain001.getAuthCode()).thenReturn("AUTH_CODE");
    when(cain001.getAuthResponseCode()).thenReturn(_00);
    Cain002 cain002 = Cain002.unsuccessful(cain001);

    assertThat(cain002.getUpdatedBalance()).isNull();
    assertThat(cain002.getAuthCode()).isEqualTo("AUTH_CODE");
    assertThat(cain002.getAuthResponseCode()).isEqualTo(_00);
    assertThat(cain002.isSuccess()).isFalse();
  }

  @Test
  void successful_givenAuthCain001() {
    Cain001 cain001 = mock(Cain001.class);
    when(cain001.getCurrency()).thenReturn(GBP);
    Cain002 cain002 = Cain002.successful(cain001, BigDecimal.valueOf(12));

    assertThat(cain002.getUpdatedBalance()).isEqualTo(Money.of(12, GBP));
    assertThat(cain002.getAuthCode()).isNotNull();
    assertThat(cain002.getAuthCode().length()).isEqualTo(6);
    assertThat(cain002.getAuthResponseCode()).isEqualTo(_00);
    assertThat(cain002.isSuccess()).isTrue();
  }

  @Test
  void successful_givenAdviceOrReversalCain001() {
    Cain001 cain001 = mock(Cain001.class);
    when(cain001.getAuthCode()).thenReturn("AUTH_CODE");
    when(cain001.getAuthResponseCode()).thenReturn(_05);
    when(cain001.getCurrency()).thenReturn(GBP);
    Cain002 cain002 = Cain002.successful(cain001, BigDecimal.valueOf(12));

    assertThat(cain002.getUpdatedBalance()).isEqualTo(Money.of(12, GBP));
    assertThat(cain002.getAuthCode()).isEqualTo("AUTH_CODE");
    assertThat(cain002.getAuthResponseCode()).isEqualTo(_05);
    assertThat(cain002.isSuccess()).isTrue();
  }

}
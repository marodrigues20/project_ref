package com.tenxbanking.cardrails.adapter.primary.rest.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SchemeMessageResponseTest {

  @Test
  void shouldReturnNegativeResponseFromCain002() {
    Cain002 cain002 = new Cain002("123456", Money.of(BigDecimal.ONE, "EUR"), AuthResponseCode._05, false);
    SchemeMessageResponse response200 = SchemeMessageResponse.of(cain002);

    assertThat(response200.getReasonCode()).isEqualTo(ReasonCodeEnum._05.toString());
    assertThat(response200.getUpdatedBalance().getAmount()).isEqualTo(BigDecimal.ONE);
  }

}
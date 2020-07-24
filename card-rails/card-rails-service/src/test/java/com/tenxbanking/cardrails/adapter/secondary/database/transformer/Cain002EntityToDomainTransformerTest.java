package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Money;
import java.math.BigDecimal;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Cain002EntityToDomainTransformerTest {

  private Cain002EntityToDomainTransformer underTest;

  @BeforeEach
  void setup() {
    underTest = new Cain002EntityToDomainTransformer();
  }

  @Test
  void transform() {
    CardTransactionEntity entity = mock(CardTransactionEntity.class);
    when(entity.getAuthCode()).thenReturn("AUTH_CODE");
    when(entity.getUpdatedBalance()).thenReturn(BigDecimal.valueOf(123));
    when(entity.getTransactionCurrency()).thenReturn("GBP");
    when(entity.isSuccess()).thenReturn(true);
    when(entity.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    Cain002 cain002 = underTest.transform(entity);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(cain002.getAuthCode()).isEqualTo("AUTH_CODE");
      soft.assertThat(cain002.getAuthResponseCode()).isEqualTo(AuthResponseCode._00);
      soft.assertThat(cain002.getUpdatedBalance()).isEqualTo(Money.of(123, "GBP"));
      soft.assertThat(cain002.isSuccess()).isTrue();
    });
  }

  @Test
  void whenIsSuccessFalse_thenDoNotPopulateUpdatedBalance() {
    CardTransactionEntity entity = mock(CardTransactionEntity.class);
    when(entity.getAuthCode()).thenReturn("AUTH_CODE");
    when(entity.isSuccess()).thenReturn(false);
    when(entity.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    Cain002 cain002 = underTest.transform(entity);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(cain002.getUpdatedBalance()).isNull();
      soft.assertThat(cain002.isSuccess()).isFalse();
    });
  }

}
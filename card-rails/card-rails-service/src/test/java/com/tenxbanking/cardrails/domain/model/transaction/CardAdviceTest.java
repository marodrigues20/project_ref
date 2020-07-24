package com.tenxbanking.cardrails.domain.model.transaction;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.GBP;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.model.AuthResponseCode._05;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.TestConstant;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import org.junit.jupiter.api.Test;

class CardAdviceTest {

  @Test
  void getAuthCode_usesCain001() {
    Cain001 cain001 = mock(Cain001.class);
    when(cain001.getAuthCode()).thenReturn("AUTHCODE");
    CardAdvice transaction = CardAdvice
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .cain002(mock(Cain002.class))
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();

    String returned = transaction.getAuthCode().get();

    assertThat(returned).isEqualTo("AUTHCODE");
  }

  @Test
  void getAuthResponseCode_usesCain001() {
    Cain001 cain001 = mock(Cain001.class);
    when(cain001.getAuthResponseCode()).thenReturn(_05);
    CardAdvice transaction = CardAdvice
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .cain002(mock(Cain002.class))
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();

    AuthResponseCode authResponseCode = transaction.getAuthResponseCode();

    assertThat(authResponseCode).isEqualTo(_05);
  }

  @Test
  void getAmount() {

    Cain001 cain001 = mock(Cain001.class);
    when(cain001.getTransactionAmount()).thenReturn(Money.of(12, GBP));
    when(cain001.getBillingAmount()).thenReturn(Money.of(10, GBP));

    CardAdvice transaction = CardAdvice
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();

    assertThat(transaction.getTransactionAmount()).isEqualTo(Money.of(12, GBP));
    assertThat(transaction.getBillingAmount()).isEqualTo(Money.of(10, GBP));

  }

  @Test
  void getType() {
    CardAdvice transaction = CardAdvice
        .builder()
        .cardId(CARD_ID)
        .cain001(mock(Cain001.class))
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();

    assertThat(transaction.getType()).isEqualTo(ADVICE);
  }

  @Test
  void withFee() {
    Fee fee = TestConstant.FEE;
    Cain001 cain001 = mock(Cain001.class);
    Cain001 cain001WithFee = mock(Cain001.class);
    when(cain001.addFee(fee)).thenReturn(cain001WithFee);
    CardAdvice transaction = CardAdvice
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();

    CardAdvice cardAuth = transaction.withFee(fee);
    assertThat(cardAuth).isEqualTo(
        new CardAdvice(
            CARD_ID,
            SUBSCRIPTION_KEY,
            PARTY_KEY,
            PRODUCT_KEY,
            TENANT_KEY,
            cain001WithFee
        ));
  }

}
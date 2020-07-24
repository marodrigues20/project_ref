package com.tenxbanking.cardrails.domain.model.transaction;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static com.tenxbanking.cardrails.domain.model.transaction.CreditDebit.CREDIT;
import static com.tenxbanking.cardrails.domain.model.transaction.CreditDebit.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import org.junit.jupiter.api.Test;


class AuthTransactionTest {

  @Test
  void getCreditDebit_reversal() {
    Cain001 cain001 = mock(Cain001.class);
    CardAuth transaction = CardAuth
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .cain002(mock(Cain002.class))
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();
    when(cain001.getCardTransactionType()).thenReturn(REVERSAL);

    CreditDebit returned = transaction.getCreditDebit();

    assertThat(returned).isEqualTo(CREDIT);
  }

  @Test
  void getCreditDebit_auth() {
    Cain001 cain001 = mock(Cain001.class);
    CardAuth transaction = CardAuth
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .cain002(mock(Cain002.class))
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();
    when(cain001.getCardTransactionType()).thenReturn(AUTH);

    CreditDebit returned = transaction.getCreditDebit();

    assertThat(returned).isEqualTo(DEBIT);
  }

  @Test
  void getCreditDebit_advice() {
    Cain001 cain001 = mock(Cain001.class);
    CardAuth transaction = CardAuth
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .cain002(mock(Cain002.class))
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();

    when(cain001.getCardTransactionType()).thenReturn(ADVICE);

    CreditDebit returned = transaction.getCreditDebit();

    assertThat(returned).isEqualTo(DEBIT);
  }

}
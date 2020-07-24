package com.tenxbanking.cardrails.domain.model.transaction;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.GBP;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.model.AuthResponseCode._05;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.ReversalAmount;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CardAuthReversalTest {

  @Test
  void getAmount() {

    Cain001 cain001 = mock(Cain001.class);
    when(cain001.getTransactionAmount()).thenReturn(Money.of(12, GBP));
    when(cain001.getReversalAmount()).thenReturn(Optional.of(ReversalAmount.builder()
        .billing(Money.of(10, GBP))
        .settlement(Money.of(15, GBP))
        .transaction(Money.of(13, GBP))
        .build()));

    CardAuthReversal transaction = CardAuthReversal
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();

    assertThat(transaction.getTransactionAmount()).isEqualTo(Money.of(13, GBP));
    assertThat(transaction.getBillingAmount()).isEqualTo(Money.of(10, GBP));

  }

  @Test
  void getAuthCode_usesCain001() {
    Cain001 cain001 = mock(Cain001.class);
    when(cain001.getAuthCode()).thenReturn("AUTHCODE");
    CardAuthReversal transaction = CardAuthReversal
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
    CardAuthReversal transaction = CardAuthReversal
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
  void getType() {
    CardAuthReversal transaction = CardAuthReversal
        .builder()
        .cardId(CARD_ID)
        .cain001(mock(Cain001.class))
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .partyKey(PARTY_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .build();

    assertThat(transaction.getType()).isEqualTo(REVERSAL);
  }

}
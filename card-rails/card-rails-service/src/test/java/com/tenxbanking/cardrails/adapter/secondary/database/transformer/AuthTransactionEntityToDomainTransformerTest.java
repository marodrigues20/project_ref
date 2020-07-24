package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.CLEARING;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAdvice;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuthReversal;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthTransactionEntityToDomainTransformerTest {

  @Mock
  private Cain001EntityToDomainTransformer cain001Transformer;
  @Mock
  private Cain002EntityToDomainTransformer cain002Transformer;
  @InjectMocks
  private AuthTransactionEntityToDomainTransformer underTest;

  @Mock
  private CardTransactionEntity entity;

  @Mock
  private Cain001 cain001;
  @Mock
  private Cain002 cain002;

  @Test
  void transformAuth() {
    when(entity.getCardTransactionType()).thenReturn(AUTH);
    when(entity.getCardId()).thenReturn(CARD_ID);
    when(entity.getSubscriptionKey()).thenReturn(SUBSCRIPTION_KEY);
    when(entity.getPartyKey()).thenReturn(PARTY_KEY);
    when(entity.getProductKey()).thenReturn(PRODUCT_KEY);
    when(entity.getTenantKey()).thenReturn(TENANT_KEY);
    when(cain001Transformer.transform(entity)).thenReturn(cain001);
    when(cain002Transformer.transform(entity)).thenReturn(cain002);

    AuthTransaction returned = underTest.transform(entity);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(returned).isInstanceOf(CardAuth.class);
      soft.assertThat(returned.getCardId()).isEqualTo(CARD_ID);
      soft.assertThat(returned.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
      soft.assertThat(returned.getPartyKey()).isEqualTo(PARTY_KEY);
      soft.assertThat(returned.getProductKey()).isEqualTo(PRODUCT_KEY);
      soft.assertThat(returned.getTenantKey()).isEqualTo(TENANT_KEY);
      soft.assertThat(returned.getCain001()).isEqualTo(cain001);
      soft.assertThat(returned.getCain002()).isEqualTo(cain002);
    });

    verify(cain001Transformer).transform(entity);
    verify(cain002Transformer).transform(entity);
  }

  @Test
  void transformAdvice() {
    when(entity.getCardTransactionType()).thenReturn(ADVICE);
    when(entity.getCardId()).thenReturn(CARD_ID);
    when(entity.getSubscriptionKey()).thenReturn(SUBSCRIPTION_KEY);
    when(entity.getPartyKey()).thenReturn(PARTY_KEY);
    when(entity.getProductKey()).thenReturn(PRODUCT_KEY);
    when(entity.getTenantKey()).thenReturn(TENANT_KEY);
    when(cain001Transformer.transform(entity)).thenReturn(cain001);
    when(cain002Transformer.transform(entity)).thenReturn(cain002);

    AuthTransaction returned = underTest.transform(entity);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(returned).isInstanceOf(CardAdvice.class);
      soft.assertThat(returned.getCardId()).isEqualTo(CARD_ID);
      soft.assertThat(returned.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
      soft.assertThat(returned.getPartyKey()).isEqualTo(PARTY_KEY);
      soft.assertThat(returned.getProductKey()).isEqualTo(PRODUCT_KEY);
      soft.assertThat(returned.getTenantKey()).isEqualTo(TENANT_KEY);
      soft.assertThat(returned.getCain001()).isEqualTo(cain001);
      soft.assertThat(returned.getCain002()).isEqualTo(cain002);
    });

    verify(cain001Transformer).transform(entity);
    verify(cain002Transformer).transform(entity);
  }

  @Test
  void transformReversal() {
    when(entity.getCardTransactionType()).thenReturn(REVERSAL);
    when(entity.getCardId()).thenReturn(CARD_ID);
    when(entity.getSubscriptionKey()).thenReturn(SUBSCRIPTION_KEY);
    when(entity.getPartyKey()).thenReturn(PARTY_KEY);
    when(entity.getProductKey()).thenReturn(PRODUCT_KEY);
    when(entity.getTenantKey()).thenReturn(TENANT_KEY);
    when(cain001Transformer.transform(entity)).thenReturn(cain001);
    when(cain002Transformer.transform(entity)).thenReturn(cain002);

    AuthTransaction returned = underTest.transform(entity);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(returned).isInstanceOf(CardAuthReversal.class);
      soft.assertThat(returned.getCardId()).isEqualTo(CARD_ID);
      soft.assertThat(returned.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
      soft.assertThat(returned.getPartyKey()).isEqualTo(PARTY_KEY);
      soft.assertThat(returned.getProductKey()).isEqualTo(PRODUCT_KEY);
      soft.assertThat(returned.getTenantKey()).isEqualTo(TENANT_KEY);
      soft.assertThat(returned.getCain001()).isEqualTo(cain001);
      soft.assertThat(returned.getCain002()).isEqualTo(cain002);
    });

    verify(cain001Transformer).transform(entity);
    verify(cain002Transformer).transform(entity);
  }

  @Test
  void unknownTransactionType() {
    when(entity.getCardTransactionType()).thenReturn(CLEARING);

    assertThrows(RuntimeException.class,
        () -> underTest.transform(entity),
        "No AuthTransaction transformation implemented for cardTransactionType=CLEARING"
    );
  }

}
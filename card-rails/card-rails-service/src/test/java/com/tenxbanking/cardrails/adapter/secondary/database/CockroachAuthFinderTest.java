package com.tenxbanking.cardrails.adapter.secondary.database;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.transformer.AuthTransactionEntityToDomainTransformer;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CockroachAuthFinderTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();
  private static final String RETRIEVAL_REFERENCE_NUMBER = "RETRIEVAL_REFERENCE_NUMBER";
  private static final String BANKNET_REFERENCE_NUMBER = "BANKNET_REFERENCE_NUMBER";
  private static final String AUTH_CODE = "AUTH_CODE";


  @Mock
  private CardTransactionCockroachRepository cardTransactionCockroachRepository;
  @Mock
  private AuthTransactionEntityToDomainTransformer authTransactionEntityToDomainTransformer;
  @InjectMocks
  private CockroachAuthFinder underTest;

  @Mock
  private Cain001 cain001;
  @Mock
  private Cain002 cain002;

  @Mock
  private CardTransactionEntity cardTransactionEntity;

  private CardAuth cardAuth;

  @BeforeEach
  void setup() {
    cardAuth = cardAuth();
  }

  @Test
  void findByRetrievalReferenceNumberAndIsSuccess() {

    when(cardTransactionCockroachRepository
        .findByCardTransactionTypeInAndRetrievalReferenceNumberAndIsSuccess(anyList(), anyString(), anyBoolean()))
        .thenReturn(List.of(cardTransactionEntity));
    when(authTransactionEntityToDomainTransformer.transform(cardTransactionEntity))
        .thenReturn(cardAuth);

    Optional<AuthTransaction> returned = underTest.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER);

    assertThat(returned).contains(cardAuth);
    verify(cardTransactionCockroachRepository)
        .findByCardTransactionTypeInAndRetrievalReferenceNumberAndIsSuccess(
            List.of(AUTH, ADVICE),
            RETRIEVAL_REFERENCE_NUMBER,
            true);
    verify(authTransactionEntityToDomainTransformer).transform(cardTransactionEntity);
  }

  @Test
  void findByBanknetReferenceNumberAndIsSuccess() {

    when(cardTransactionCockroachRepository
        .findByCardTransactionTypeInAndBanknetReferenceNumberAndIsSuccess(anyList(), anyString(), anyBoolean()))
        .thenReturn(List.of(cardTransactionEntity));
    when(authTransactionEntityToDomainTransformer.transform(cardTransactionEntity))
        .thenReturn(cardAuth);

    Optional<AuthTransaction> returned = underTest.findMatchingAuthByBanknetReferenceNumber(BANKNET_REFERENCE_NUMBER);

    assertThat(returned).contains(cardAuth);
    verify(cardTransactionCockroachRepository)
        .findByCardTransactionTypeInAndBanknetReferenceNumberAndIsSuccess(
            List.of(AUTH, ADVICE),
            BANKNET_REFERENCE_NUMBER,
            true);
    verify(authTransactionEntityToDomainTransformer).transform(cardTransactionEntity);
  }

  @Test
  void findByAuthCodeAndIsSuccess() {

    when(cardTransactionCockroachRepository
        .findByCardTransactionTypeInAndAuthCodeAndIsSuccess(anyList(), anyString(), anyBoolean()))
        .thenReturn(List.of(cardTransactionEntity));
    when(authTransactionEntityToDomainTransformer.transform(cardTransactionEntity))
        .thenReturn(cardAuth);

    Optional<AuthTransaction> returned = underTest.findMatchingAuthByAuthCode(AUTH_CODE);

    assertThat(returned).contains(cardAuth);
    verify(cardTransactionCockroachRepository)
        .findByCardTransactionTypeInAndAuthCodeAndIsSuccess(
            List.of(AUTH, ADVICE),
            AUTH_CODE,
            true);
    verify(authTransactionEntityToDomainTransformer).transform(cardTransactionEntity);
  }

  private CardAuth cardAuth() {
    return CardAuth
        .builder()
        .partyKey(PARTY_KEY)
        .tenantKey(TENANT_KEY)
        .cardId(CARD_ID)
        .productKey(PRODUCT_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .cain001(cain001)
        .cain002(cain002)
        .build();
  }

}
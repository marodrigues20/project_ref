package com.tenxbanking.cardrails.domain.service.handler;

import static com.tenxbanking.cardrails.domain.TestConstant.BANKNET_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.CORRELATION_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.RETRIEVAL_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TRANSACTION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.primary.rest.exception.UnsolicitedReversalException;
import com.tenxbanking.cardrails.domain.exception.CardNotFoundException;
import com.tenxbanking.cardrails.domain.exception.SubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuthReversal;
import com.tenxbanking.cardrails.domain.port.CardSettingsService;
import com.tenxbanking.cardrails.domain.port.DebitCardService;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import com.tenxbanking.cardrails.domain.port.finder.AuthFinder;
import com.tenxbanking.cardrails.domain.port.sender.AuthReversalTransactionSender;
import com.tenxbanking.cardrails.domain.port.store.AuthTransactionStore;
import com.tenxbanking.cardrails.domain.service.PanHashingService;
import com.tenxbanking.cardrails.domain.service.TimeService;
import com.tenxbanking.cardrails.domain.validator.CardTransactionValidator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardAuthReversalHandlerTest {

  private static final String AUTH_CODE = "AUTH_CODE";

  @Mock
  private TimeService timeService;
  @Mock
  private DebitCardService debitCardService;
  @Mock
  private CardSettingsService cardSettingsService;
  @Mock
  private SubscriptionService subscriptionService;
  @Mock
  private AuthReversalTransactionSender transactionSender;
  @Mock
  private PanHashingService panHashingService;
  @Mock
  private AuthTransactionStore authTransactionStore;
  @Mock
  private AuthFinder authFinder;
  @Mock
  private Supplier<UUID> uuidSupplier;
  @Mock
  private CardTransactionValidator validator;
  @InjectMocks
  private CardAuthReversalHandler underTest;

  @Mock
  private Cain001 cain001;
  @Mock
  private Cain001 cainWithIds;

  @Mock
  private Cain002 cain002;

  @Mock
  private CardAuth cardAuth;
  @Mock
  private CardSettings cardSettings;

  @BeforeEach
  void setup() {
    when(timeService.now()).thenReturn(Instant.now());
    when(cain001.getCardId()).thenReturn(CARD_ID);
  }

  @Test
  void throwsExceptionIfCardNotFound() {

    assertThrows(CardNotFoundException.class,
        () -> underTest.auth(cain001));
  }

  @Test
  void findsCard() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(debitCardService).findByCardIdHash(PAN_HASH);
    verify(panHashingService).hashCardId(CARD_ID);
  }

  @Test
  void findsSubscription() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(subscriptionService).findById(SUBSCRIPTION_KEY);
  }

  @Test
  void throwsExceptionIfSubscriptionNotFound() {
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));

    assertThrows(SubscriptionNotFoundException.class,
        () -> underTest.auth(cain001));
  }

  @Test
  void matchesByRetrievalReferenceNumber() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(authFinder).findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER);
    verifyNoMoreInteractions(authFinder);
  }

  @Test
  void matchesByBanknetReferenceNumber() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(cain001.getBanknetReferenceNumber()).thenReturn(BANKNET_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.empty());
    when(authFinder.findMatchingAuthByBanknetReferenceNumber(BANKNET_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(authFinder).findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER);
    verify(authFinder).findMatchingAuthByBanknetReferenceNumber(BANKNET_REFERENCE_NUMBER);
    verifyNoMoreInteractions(authFinder);
  }

  @Test
  void matchesByAuthCode() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(cain001.getBanknetReferenceNumber()).thenReturn(BANKNET_REFERENCE_NUMBER);
    when(cain001.getAuthCode()).thenReturn(AUTH_CODE);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.empty());
    when(authFinder.findMatchingAuthByBanknetReferenceNumber(BANKNET_REFERENCE_NUMBER)).thenReturn(Optional.empty());
    when(authFinder.findMatchingAuthByAuthCode(AUTH_CODE)).thenReturn(Optional.of(cardAuth));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    ;
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(authFinder).findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER);
    verify(authFinder).findMatchingAuthByBanknetReferenceNumber(BANKNET_REFERENCE_NUMBER);
    verify(authFinder).findMatchingAuthByAuthCode(AUTH_CODE);
    verifyNoMoreInteractions(authFinder);
  }

  @Test
  void throwsExceptionWhenAuthCannotBeMatched() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(cain001.getBanknetReferenceNumber()).thenReturn(BANKNET_REFERENCE_NUMBER);
    when(cain001.getAuthCode()).thenReturn(AUTH_CODE);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.empty());
    when(authFinder.findMatchingAuthByBanknetReferenceNumber(BANKNET_REFERENCE_NUMBER)).thenReturn(Optional.empty());
    when(authFinder.findMatchingAuthByAuthCode(AUTH_CODE)).thenReturn(Optional.empty());
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));

    assertThrows(UnsolicitedReversalException.class,
        () -> underTest.auth(cain001));

  }

  @Test
  void addsTransactionIdAndCorrelationIdToCain001() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.getTransactionId()).thenReturn(TRANSACTION_ID);
    when(cain001.getCorrelationId()).thenReturn(CORRELATION_ID);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(cain001).addTransactionIds(TRANSACTION_ID, CORRELATION_ID);

  }

  @Test
  void validatesTransactionType() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.getCorrelationId()).thenReturn(CORRELATION_ID);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);


    underTest.auth(cain001);

    verify(validator).validate(new CardAuthReversal(
            CARD_ID,
            SUBSCRIPTION_KEY,
            PARTY_KEY,
            PRODUCT_KEY,
            TENANT_KEY,
            cainWithIds
        ),
        CARD,
        SUBSCRIPTION,
        cardSettings);
  }

  @Test
  void validatesCardTransaction() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.getCorrelationId()).thenReturn(CORRELATION_ID);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(validator).validate(new CardAuthReversal(
            CARD_ID,
            SUBSCRIPTION_KEY,
            PARTY_KEY,
            PRODUCT_KEY,
            TENANT_KEY,
            cainWithIds
        ),
        CARD,
        SUBSCRIPTION,
        cardSettings);
  }

  @Test
  void sendsCain001Message() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(transactionSender).reverse(new CardAuthReversal(
        cain001.getCardId(),
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cainWithIds,
        null));

  }

  @Test
  void savesCain002Message() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    underTest.auth(cain001);

    verify(authTransactionStore).save(new CardAuthReversal(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cainWithIds,
        cain002));
  }

  @Test
  void returnsCain002Message() {

    when(cain001.getRetrievalReferenceNumber()).thenReturn(RETRIEVAL_REFERENCE_NUMBER);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(authFinder.findMatchingAuthByRetrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)).thenReturn(Optional.of(cardAuth));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reverse(any())).thenReturn(cain002);

    Cain002 returned = underTest.auth(cain001);

    assertThat(returned).isEqualTo(cain002);

  }

}
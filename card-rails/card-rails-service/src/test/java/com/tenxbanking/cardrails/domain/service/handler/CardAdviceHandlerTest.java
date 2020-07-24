package com.tenxbanking.cardrails.domain.service.handler;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.CORRELATION_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.FEE;
import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TRANSACTION_ID;
import static com.tenxbanking.cardrails.domain.model.AuthResponseCode._05;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.exception.CardNotFoundException;
import com.tenxbanking.cardrails.domain.exception.SubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAdvice;
import com.tenxbanking.cardrails.domain.port.CardSettingsService;
import com.tenxbanking.cardrails.domain.port.DebitCardService;
import com.tenxbanking.cardrails.domain.port.FeesCheckerService;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import com.tenxbanking.cardrails.domain.port.sender.AuthReserveTransactionSender;
import com.tenxbanking.cardrails.domain.port.store.CardTransactionStore;
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
class CardAdviceHandlerTest {

  @Mock
  private TimeService timeService;
  @Mock
  private SubscriptionService subscriptionService;
  @Mock
  private FeesCheckerService feesChecker;
  @Mock
  private AuthReserveTransactionSender transactionSender;
  @Mock
  private Supplier<UUID> uuidSupplier;
  @Mock
  private DebitCardService debitCardService;
  @Mock
  private CardSettingsService cardSettingsService;
  @Mock
  private PanHashingService panHashingService;
  @Mock
  private CardTransactionStore<AuthTransaction> authTransactionStore;
  @Mock
  private CardTransactionValidator cardTransactionValidator;

  @Mock
  private Cain001 cain001;
  @Mock
  private Cain001 cainWithIds;
  @Mock
  private Cain001 cain001WithFees;
  @InjectMocks
  private CardAdviceHandler underTest;

  @Mock
  private Cain002 cain002;
  @Mock
  private CardSettings cardSettings;

  @BeforeEach
  void setup() {
    when(timeService.now()).thenReturn(Instant.now());
    when(cain001.getCardId()).thenReturn(CARD_ID);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(PAN_HASH);
  }

  @Test
  void throwsExceptionIfCardNotFound() {

    assertThrows(CardNotFoundException.class,
        () -> underTest.auth(cain001));
  }

  @Test
  void findsCard() {

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reserve(any())).thenReturn(cain002);
    when(cainWithIds.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    underTest.auth(cain001);

    verify(debitCardService).findByCardIdHash(PAN_HASH);
    verify(panHashingService).hashCardId(CARD_ID);
  }

  @Test
  void findsSubscription() {

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reserve(any())).thenReturn(cain002);
    when(cainWithIds.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    underTest.auth(cain001);

    verify(subscriptionService).findById(SUBSCRIPTION_KEY);
  }

  @Test
  void throwsExceptionIfSubscriptionNotFound() {
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));

    assertThrows(SubscriptionNotFoundException.class,
        () -> underTest.auth(cain001));
  }

  @Test
  void addsTransactionIdAndCorrelationIdToCain001() {

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(uuidSupplier.get())
        .thenReturn(TRANSACTION_ID)
        .thenReturn(CORRELATION_ID);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reserve(any())).thenReturn(cain002);
    when(cainWithIds.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    underTest.auth(cain001);

    verify(cain001).addTransactionIds(TRANSACTION_ID, CORRELATION_ID);
  }

  @Test
  void validatesCardTransaction() {

    Subscription subscription = SUBSCRIPTION;
    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(subscription));
    when(uuidSupplier.get())
        .thenReturn(TRANSACTION_ID)
        .thenReturn(CORRELATION_ID);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reserve(any())).thenReturn(cain002);
    when(cainWithIds.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    underTest.auth(cain001);

    verify(cardTransactionValidator).validate(new CardAdvice(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cainWithIds
    ), CARD, subscription, cardSettings);
  }

  @Test
  void addsFeeToCain001WhenPresent() {

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(of(SUBSCRIPTION));
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(feesChecker.check(cainWithIds, SUBSCRIPTION_KEY)).thenReturn(of(FEE));
    when(cainWithIds.addFee(FEE)).thenReturn(cain001WithFees);
    when(transactionSender.reserve(any())).thenReturn(cain002);
    when(cain001WithFees.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    Cain002 cain002 = underTest.auth(cain001);

    verify(transactionSender).reserve(new CardAdvice(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain001WithFees));
    verify(authTransactionStore).save(new CardAdvice(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain001WithFees,
        cain002));

    assertThat(cain002).isEqualTo(cain002);
  }

  @Test
  void sendsCain001Message() {

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(uuidSupplier.get()).thenReturn(TRANSACTION_ID);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reserve(any())).thenReturn(cain002);
    when(cainWithIds.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    underTest.auth(cain001);

    verify(transactionSender).reserve(new CardAdvice(
        cain001.getCardId(),
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cainWithIds));
  }

  @Test
  void doesNotSendCain001MessageIfUnsuccessfulAuthResponseCode() {

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(uuidSupplier.get()).thenReturn(TRANSACTION_ID);
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(cainWithIds.getAuthResponseCode()).thenReturn(_05);

    underTest.auth(cain001);

    verifyZeroInteractions(transactionSender);
  }

  @Test
  void savesCain002Message() {

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reserve(any())).thenReturn(cain002);
    when(cainWithIds.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    underTest.auth(cain001);

    verify(authTransactionStore).save(new CardAdvice(
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

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(transactionSender.reserve(any())).thenReturn(cain002);
    when(cainWithIds.getAuthResponseCode()).thenReturn(AuthResponseCode._00);

    Cain002 returned = underTest.auth(cain001);

    assertThat(returned).isEqualTo(cain002);
  }

  @Test
  void returnsCain002WhenFailedAdvice() {

    when(debitCardService.findByCardIdHash(PAN_HASH)).thenReturn(Optional.of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(Optional.of(SUBSCRIPTION));
    when(cain001.addTransactionIds(any(), any())).thenReturn(cainWithIds);
    when(cainWithIds.getAuthResponseCode()).thenReturn(_05);

    Cain002 returned = underTest.auth(cain001);

    assertThat(returned.isSuccess()).isEqualTo(false);
    assertThat(returned.getAuthResponseCode()).isEqualTo(_05);
  }

}
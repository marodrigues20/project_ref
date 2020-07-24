package com.tenxbanking.cardrails.domain.service.handler;

import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_OO2;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.FEE;
import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.ACTIVE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.TestConstant;
import com.tenxbanking.cardrails.domain.exception.CardNotFoundException;
import com.tenxbanking.cardrails.domain.exception.LimitConstraintException;
import com.tenxbanking.cardrails.domain.exception.SubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.port.CardSettingsService;
import com.tenxbanking.cardrails.domain.port.DebitCardService;
import com.tenxbanking.cardrails.domain.port.FeesCheckerService;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import com.tenxbanking.cardrails.domain.port.sender.AuthReserveTransactionSender;
import com.tenxbanking.cardrails.domain.port.store.CardTransactionStore;
import com.tenxbanking.cardrails.domain.service.PanHashingService;
import com.tenxbanking.cardrails.domain.service.TimeService;
import com.tenxbanking.cardrails.domain.validator.CardTransactionValidator;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardAuthHandlerTest {

  private static final UUID TRANSACTION_ID = UUID.randomUUID();
  private static final UUID CORRELATION_KEY = UUID.randomUUID();
  private static final Card CARD = TestConstant.CARD;
  private static final Instant NOW = Instant.now();

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
  private CardTransactionValidator validator;
  @Mock
  private Cain001 cain001;
  @Mock
  private Cain001 cain001WithIds;
  @Mock
  private Cain001 cain001WithFees;
  @Mock
  private CardSettings cardSettings;
  @InjectMocks
  private CardAuthHandler unit;

  @Test
  void shouldThrowExceptionWhenCardIsNotFound() {
    when(timeService.now()).thenReturn(NOW);
    when(cain001.getCardId()).thenReturn(CARD_ID);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(CARD_ID);
    when(debitCardService.findByCardIdHash(CARD_ID)).thenReturn(empty());

    assertThatThrownBy(() -> unit.auth(cain001))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  void shouldThrowExceptionWhenSubscriptionIsNotFound() {
    when(timeService.now()).thenReturn(NOW);
    when(cain001.getCardId()).thenReturn(CARD_ID);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(CARD_ID);
    when(debitCardService.findByCardIdHash(CARD_ID)).thenReturn(of(CARD));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(empty());

    assertThatThrownBy(() -> unit.auth(cain001))
        .isInstanceOf(SubscriptionNotFoundException.class);
  }

  @Test
  void shouldValidateCard() {

    Subscription subscription = getSubscription(true);
    when(timeService.now()).thenReturn(NOW);
    when(cain001.getCardId()).thenReturn(CARD_ID);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(CARD_ID);
    when(cardSettingsService.findByCardIdOrPanHash(any(), any())).thenReturn(Optional.of(cardSettings));
    when(debitCardService.findByCardIdHash(CARD_ID)).thenReturn(of(CARD));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(of(subscription));
    when(cain001.addTransactionIds(any(), any())).thenReturn(cain001WithIds);

    doThrow(LimitConstraintException.class).when(validator)
        .validate(new CardAuth(
                CARD_ID,
                SUBSCRIPTION_KEY,
                PARTY_KEY,
                PRODUCT_KEY,
                TENANT_KEY,
                cain001WithIds
            ),
            CARD,
            subscription,
            cardSettings);

    assertThatThrownBy(() -> unit.auth(cain001))
        .isInstanceOf(LimitConstraintException.class);
  }

  @Test
  void shouldAddFeeToCain001WhenPresentAndSend() {

    Subscription subscription = getSubscription(true);
    when(timeService.now()).thenReturn(NOW);
    when(cain001.getCardId()).thenReturn(CARD_ID);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(CARD_ID);
    when(timeService.now()).thenReturn(NOW);
    when(cain001.getCardId()).thenReturn(CARD_ID);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(CARD_ID);
    when(debitCardService.findByCardIdHash(CARD_ID)).thenReturn(of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(CARD_ID, PAN_HASH)).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(of(subscription));
    when(feesChecker.check(cain001WithIds, SUBSCRIPTION_KEY)).thenReturn(of(FEE));
    when(cain001WithIds.addFee(FEE)).thenReturn(cain001WithFees);
    when(transactionSender.reserve(any())).thenReturn(CAIN_OO2);
    when(uuidSupplier.get()).thenReturn(TRANSACTION_ID, CORRELATION_KEY);
    when(cain001.addTransactionIds(TRANSACTION_ID, CORRELATION_KEY)).thenReturn(cain001WithIds);

    Cain002 cain002 = unit.auth(cain001);

    verify(transactionSender).reserve(new CardAuth(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain001WithFees,
        null));
    verify(authTransactionStore).save(new CardAuth(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain001WithFees,
        CAIN_OO2));
    assertThat(cain002).isEqualTo(CAIN_OO2);
  }

  @Test
  void shouldNotAddFeeToCain001WhenEmptyAndSend() {

    Subscription subscription = getSubscription(false);
    when(timeService.now()).thenReturn(NOW);
    when(cain001.getCardId()).thenReturn(CARD_ID);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(CARD_ID);
    when(debitCardService.findByCardIdHash(CARD_ID)).thenReturn(of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(CARD_ID, PAN_HASH)).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(of(subscription));
    when(transactionSender.reserve(any())).thenReturn(CAIN_OO2);
    when(uuidSupplier.get()).thenReturn(TRANSACTION_ID, CORRELATION_KEY);
    when(cain001.addTransactionIds(TRANSACTION_ID, CORRELATION_KEY)).thenReturn(cain001WithIds);

    Cain002 cain002 = unit.auth(cain001);

    verify(transactionSender).reserve(new CardAuth(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain001WithIds,
        null));
    verify(authTransactionStore).save(new CardAuth(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain001WithIds,
        CAIN_OO2));
    assertThat(cain002).isEqualTo(CAIN_OO2);
  }

  @Test
  void shouldReturnCain002CreatedFromTheResultFromTransactionSender() {

    Subscription subscription = getSubscription(false);
    when(timeService.now()).thenReturn(NOW);
    when(cain001.getCardId()).thenReturn(CARD_ID);
    when(panHashingService.hashCardId(CARD_ID)).thenReturn(CARD_ID);
    when(debitCardService.findByCardIdHash(CARD_ID)).thenReturn(of(CARD));
    when(cardSettingsService.findByCardIdOrPanHash(CARD_ID, PAN_HASH)).thenReturn(Optional.of(cardSettings));
    when(subscriptionService.findById(SUBSCRIPTION_KEY)).thenReturn(of(subscription));
    when(transactionSender.reserve(any())).thenReturn(CAIN_OO2);
    when(uuidSupplier.get()).thenReturn(TRANSACTION_ID, CORRELATION_KEY);
    when(cain001.addTransactionIds(TRANSACTION_ID, CORRELATION_KEY)).thenReturn(cain001WithIds);

    Cain002 cain002 = unit.auth(cain001);

    assertThat(cain002.isSuccess()).isTrue();
    assertThat(cain002.getAuthCode()).isNotNull();
    assertThat(cain002.getUpdatedBalance().getAmount()).isEqualTo(BigDecimal.TEN);

    verify(authTransactionStore).save(new CardAuth(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain001WithIds,
        CAIN_OO2));
    assertThat(cain002).isEqualTo(CAIN_OO2);
  }

  @Test
  void shouldReturnAuth() {
    CardTransactionType cardTransactionType = unit.handlesCardTransactionType();
    assertThat(cardTransactionType).isEqualTo(CardTransactionType.AUTH);
  }

  private Subscription getSubscription(boolean hasFees) {

    SubscriptionSettings settings = SubscriptionSettings.builder()
        .effectiveDate(NOW.minus(Duration.ofDays(1)))
        .productKey(PRODUCT_KEY)
        .productVersion(1)
        .hasFees(hasFees)
        .transactionLimits(emptyList())
        .build();

    return Subscription.builder()
        .subscriptionKey(SUBSCRIPTION_KEY)
        .settings(singletonList(settings))
        .status(ACTIVE)
        .build();
  }

}

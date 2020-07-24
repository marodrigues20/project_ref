package com.tenxbanking.cardrails.domain.service.clearing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.CreditDebitEnum;
import com.tenxbanking.cardrails.adapter.secondary.cards.DebitCardManager;
import com.tenxbanking.cardrails.domain.exception.CardClearingDebitCardManagementException;
import com.tenxbanking.cardrails.domain.exception.CardClearingSubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.model.card.Merchant;
import com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionLimit;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums;
import com.tenxbanking.cardrails.domain.model.transaction.Address;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import com.tenxbanking.cardrails.domain.port.FeesCheckerService;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import com.tenxbanking.cardrails.domain.port.store.CardClearingTransactionStore;
import com.tenxbanking.cardrails.domain.service.PanHashingService;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardClearingServiceTest {

  private static final UUID TRANSACTION_ID = UUID
      .fromString("123e4567-e89b-42d3-a456-556642440000");
  private static final UUID CORRELATION_ID = UUID
      .fromString("123e4567-e89b-42d3-a456-556642440001");
  private static final UUID PRODUCT_KEY = UUID.fromString("123e4567-e89b-42d3-a456-556642440002");
  private static final String TENANT_KEY = "10000";
  private static final String CARD_ID = "4d4d8f3b-3b81-44f3-968d-d1c1a48b4ac9";
  private static final String REFERENCE_NUMBER = "4535";
  private static final String HASH_CARD_ID = "1234";
  private static final UUID SUBSCRIPTION_KEY = UUID
      .fromString("4d4d8f3b-3b81-44f3-968d-d1c1a48b4a10");
  private static final UUID PARTY_KEY = UUID.fromString("4d4d8f3b-3b81-44f3-968d-d1c1a48b4a11");
  private static final String DEBT_CARD_MGN_ERROR = "Failed to retrieve Card. ";
  private static final String SUBSCRIPTION_ERROR = "Subscription cannot be found";

  @Mock
  private CardClearingTransactionStore cardClearingStoreService;
  @Mock
  private PanHashingService panHashingService;
  @Mock
  private FeesCheckerService feesCheckerService;
  @Mock
  private DebitCardManager debitCardManagerService;
  @Mock
  private Supplier<UUID> uuidSupplier;
  @Mock
  private SubscriptionService subscriptionService;
  @Mock
  private ClearingMatchLogic clearingMatchLogic;
  @Mock
  private TimeService timeService;
  @Captor
  private ArgumentCaptor<CardClearing> cardClearingArgumentCaptor;
  @InjectMocks
  private CardClearingImpl unit;

  @Test
  @DisplayName("Matching Logic has not been succeeded.")
  void authNotFoundWithFee() {
    //given
    Cain003 cain003 = mock(Cain003.class);
    Cain003 cain003WithIds = mock(Cain003.class);
    Cain003 cain003WithFee = mock(Cain003.class);
    Fee fee = buildFee();
    Optional<Subscription> subscription = buildSubscription();
    Card card = buildCard();

    given(cain003.getCardId()).willReturn(CARD_ID);
    given(cain003WithFee.getCardId()).willReturn(CARD_ID);
    given(cain003WithIds.addFee(fee)).willReturn(cain003WithFee);
    given(cain003.addTransactionIds(TRANSACTION_ID, CORRELATION_ID)).willReturn(cain003WithIds);
    given(uuidSupplier.get()).willReturn(TRANSACTION_ID, CORRELATION_ID);
    given(panHashingService.hashCardId(cain003.getCardId())).willReturn(HASH_CARD_ID);
    given(debitCardManagerService.findByCardIdHash(HASH_CARD_ID)).willReturn(Optional.of(card));
    given(timeService.now()).willReturn(Instant.now());
    given(subscriptionService.findById(SUBSCRIPTION_KEY)).willReturn(subscription);
    given(feesCheckerService.check(cain003WithIds, SUBSCRIPTION_KEY)).willReturn(Optional.of(fee));

    //when
    unit.process(cain003);
    //then
    then(cardClearingStoreService).should().save(cardClearingArgumentCaptor.capture());

    CardClearing value = cardClearingArgumentCaptor.getValue();

    assertThat(value.getCardId()).isEqualTo(CARD_ID);
    assertThat(value.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(value.getProductKey()).isEqualTo(PRODUCT_KEY);
    assertThat(value.getPartyKey()).isEqualTo(PARTY_KEY);
    assertThat(value.getCain003()).isEqualTo(cain003WithFee);
    assertThat(value.getTenantKey()).isEqualTo(TENANT_KEY);
  }

  @Test
  @DisplayName("Auth Found with no fee")
  void authNotFoundWithNoFee() {
    //given
    Cain003 cain003 = mock(Cain003.class);
    Cain003 cain003WithIds = mock(Cain003.class);
    Optional<Subscription> subscription = buildSubscription();
    Card card = buildCard();

    given(cain003.getCardId()).willReturn(CARD_ID);
    given(cain003WithIds.getCardId()).willReturn(CARD_ID);
    given(cain003.addTransactionIds(TRANSACTION_ID, CORRELATION_ID)).willReturn(cain003WithIds);
    given(uuidSupplier.get()).willReturn(TRANSACTION_ID, CORRELATION_ID);
    given(panHashingService.hashCardId(cain003.getCardId())).willReturn(HASH_CARD_ID);
    given(debitCardManagerService.findByCardIdHash(HASH_CARD_ID)).willReturn(Optional.of(card));
    given(timeService.now()).willReturn(Instant.now());
    given(subscriptionService.findById(SUBSCRIPTION_KEY)).willReturn(subscription);
    given(feesCheckerService.check(cain003WithIds, SUBSCRIPTION_KEY)).willReturn(Optional.empty());

    unit.process(cain003);

    then(cardClearingStoreService).should().save(cardClearingArgumentCaptor.capture());
    CardClearing value = cardClearingArgumentCaptor.getValue();

    assertThat(value.getCardId()).isEqualTo(CARD_ID);
    assertThat(value.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(value.getProductKey()).isEqualTo(PRODUCT_KEY);
    assertThat(value.getPartyKey()).isEqualTo(PARTY_KEY);
    assertThat(value.getCain003()).isEqualTo(cain003WithIds);
    assertThat(value.getTenantKey()).isEqualTo(TENANT_KEY);
  }

  @Test
  @DisplayName("Matching logic has been succeeded and the transaction amount is different.")
  void matchingLogicPassedDifferentTransactionAmount() {

    Cain003 cain003 = mock(Cain003.class);
    Cain003 cain003WithIds = mock(Cain003.class);
    Cain003 cain003WithFee = mock(Cain003.class);
    Fee fee = buildFee();
    Optional<Subscription> subscription = buildSubscription();
    Card card = buildCard();
    AuthTransaction authTransaction = buildAuthTransaction(new BigDecimal(200));
    Cain001 cain001 = authTransaction.getCain001();

    given(cain003.getCardId()).willReturn(CARD_ID);
    given(cain003WithFee.getCardId()).willReturn(CARD_ID);
    given(cain003WithIds.addFee(fee)).willReturn(cain003WithFee);
    given(cain003.addTransactionIds(cain001.getTransactionId(), cain001.getCorrelationId())).willReturn(cain003WithIds);
    given(clearingMatchLogic.matchingLogic(cain003)).willReturn(Optional.of(authTransaction));
    given(panHashingService.hashCardId(CARD_ID)).willReturn(HASH_CARD_ID);
    given(debitCardManagerService.findByCardIdHash(HASH_CARD_ID)).willReturn(Optional.of(card));
    given(timeService.now()).willReturn(Instant.now());
    given(subscriptionService.findById(SUBSCRIPTION_KEY)).willReturn(subscription);
    given(feesCheckerService.check(cain003WithIds, SUBSCRIPTION_KEY)).willReturn(Optional.of(fee));

    unit.process(cain003);

    then(cardClearingStoreService).should().save(cardClearingArgumentCaptor.capture());
    CardClearing value = cardClearingArgumentCaptor.getValue();

    assertThat(value.getCardId()).isEqualTo(CARD_ID);
    assertThat(value.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(value.getProductKey()).isEqualTo(PRODUCT_KEY);
    assertThat(value.getPartyKey()).isEqualTo(PARTY_KEY);
    assertThat(value.getCain003()).isEqualTo(cain003WithFee);
    assertThat(value.getTenantKey()).isEqualTo(TENANT_KEY);
  }

  @Test
  @DisplayName("Matching logic has been succeeded and the transaction amount is different No Fee")
  void matchingLogicPassedDifferentTransactionAmountWithNoFee() {

    Cain003 cain003 = mock(Cain003.class);
    Cain003 cain003WithIds = mock(Cain003.class);
    Optional<Subscription> subscription = buildSubscription();
    Card card = buildCard();
    AuthTransaction authTransaction = buildAuthTransaction(new BigDecimal(200));
    Cain001 cain001 = authTransaction.getCain001();

    given(cain003.getCardId()).willReturn(CARD_ID);
    given(cain003WithIds.getCardId()).willReturn(CARD_ID);
    given(cain003.addTransactionIds(cain001.getTransactionId(), cain001.getCorrelationId())).willReturn(cain003WithIds);
    given(clearingMatchLogic.matchingLogic(cain003)).willReturn(Optional.of(authTransaction));
    given(panHashingService.hashCardId(CARD_ID)).willReturn(HASH_CARD_ID);
    given(debitCardManagerService.findByCardIdHash(HASH_CARD_ID)).willReturn(Optional.of(card));
    given(timeService.now()).willReturn(Instant.now());
    given(subscriptionService.findById(SUBSCRIPTION_KEY)).willReturn(subscription);
    given(feesCheckerService.check(cain003WithIds, SUBSCRIPTION_KEY)).willReturn(Optional.empty());

    unit.process(cain003);

    then(cardClearingStoreService).should().save(cardClearingArgumentCaptor.capture());
    CardClearing value = cardClearingArgumentCaptor.getValue();

    assertThat(value.getCardId()).isEqualTo(CARD_ID);
    assertThat(value.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(value.getProductKey()).isEqualTo(PRODUCT_KEY);
    assertThat(value.getPartyKey()).isEqualTo(PARTY_KEY);
    assertThat(value.getCain003()).isEqualTo(cain003WithIds);
    assertThat(value.getTenantKey()).isEqualTo(TENANT_KEY);
  }

  @Test
  @DisplayName("Matching Logic with the same amount")
  void matchingLogicPassedSameTransactionAmount() {

    BigDecimal amount = new BigDecimal(100);
    Money money = Money.of(amount, Currency.getInstance(Locale.UK));
    Cain003 cain003 = mock(Cain003.class);
    Cain003 cain003WithIds = mock(Cain003.class);
    Card card = buildCard();
    Optional<Subscription> subscription = buildSubscription();
    AuthTransaction authTransaction = buildAuthTransaction(amount, false);
    Cain001 cain001 = authTransaction.getCain001();

    given(cain003.getCardId()).willReturn(CARD_ID);
    given(cain003WithIds.getCardId()).willReturn(CARD_ID);
    given(cain003WithIds.getBillingAmount()).willReturn(money);
    given(cain003.addTransactionIds(cain001.getTransactionId(), cain001.getCorrelationId())).willReturn(cain003WithIds);
    given(clearingMatchLogic.matchingLogic(cain003)).willReturn(Optional.of(authTransaction));
    given(panHashingService.hashCardId(cain003.getCardId())).willReturn(HASH_CARD_ID);
    given(debitCardManagerService.findByCardIdHash(HASH_CARD_ID)).willReturn(Optional.of(card));
    given(timeService.now()).willReturn(Instant.now());
    given(subscriptionService.findById(SUBSCRIPTION_KEY)).willReturn(subscription);

    //When
    unit.process(cain003);

    //Then
    then(cardClearingStoreService).should().save(cardClearingArgumentCaptor.capture());
    then(feesCheckerService).shouldHaveZeroInteractions();

    CardClearing value = cardClearingArgumentCaptor.getValue();
    assertThat(value.getCardId()).isEqualTo(CARD_ID);
    assertThat(value.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(value.getProductKey()).isEqualTo(PRODUCT_KEY);
    assertThat(value.getPartyKey()).isEqualTo(PARTY_KEY);
    assertThat(value.getCain003()).isEqualTo(cain003WithIds);
    assertThat(value.getTenantKey()).isEqualTo(TENANT_KEY);
  }

  @Test
  @DisplayName("Matching Logic with the same amount No Fee")
  void matchingLogicPassedSameTransactionAmountNoFee() {

    BigDecimal amount = new BigDecimal(100);
    Money money = Money.of(amount, Currency.getInstance(Locale.UK));
    Cain003 cain003 = mock(Cain003.class);
    Cain003 cain003WithIds = mock(Cain003.class);
    Cain003 cain003WithFee = mock(Cain003.class);
    Card card = buildCard();
    Optional<Subscription> subscription = buildSubscription();
    AuthTransaction authTransaction = buildAuthTransaction(amount);
    Cain001 cain001 = authTransaction.getCain001();

    given(cain003.getCardId()).willReturn(CARD_ID);
    given(cain003WithFee.getCardId()).willReturn(CARD_ID);
    given(cain003WithIds.getBillingAmount()).willReturn(money);
    given(cain003WithIds.addFee(cain001.getFee().get())).willReturn(cain003WithFee);
    given(cain003.addTransactionIds(cain001.getTransactionId(), cain001.getCorrelationId())).willReturn(cain003WithIds);
    given(clearingMatchLogic.matchingLogic(cain003)).willReturn(Optional.of(authTransaction));
    given(panHashingService.hashCardId(cain003.getCardId())).willReturn(HASH_CARD_ID);
    given(debitCardManagerService.findByCardIdHash(HASH_CARD_ID)).willReturn(Optional.of(card));
    given(timeService.now()).willReturn(Instant.now());
    given(subscriptionService.findById(SUBSCRIPTION_KEY)).willReturn(subscription);

    //When
    unit.process(cain003);

    //Then
    then(cardClearingStoreService).should().save(cardClearingArgumentCaptor.capture());
    then(feesCheckerService).shouldHaveZeroInteractions();

    CardClearing value = cardClearingArgumentCaptor.getValue();
    assertThat(value.getCardId()).isEqualTo(CARD_ID);
    assertThat(value.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(value.getProductKey()).isEqualTo(PRODUCT_KEY);
    assertThat(value.getPartyKey()).isEqualTo(PARTY_KEY);
    assertThat(value.getCain003()).isEqualTo(cain003WithFee);
    assertThat(value.getTenantKey()).isEqualTo(TENANT_KEY);
  }

  @Test
  @DisplayName("Matching Logic Passed with different amount transaction but throwing exception when try to retrieve subscription.")
  void matchingLogicPassedDifferentTransactionAmountSubscriptionError() {

    //given
    Cain003 cain003 = mock(Cain003.class);
    AuthTransaction authTransaction = buildAuthTransaction(new BigDecimal(200));

    given(cain003.getCardId()).willReturn(CARD_ID);
    given(clearingMatchLogic.matchingLogic(cain003)).willReturn(Optional.of(authTransaction));
    given(panHashingService.hashCardId(CARD_ID)).willReturn("HASH_CARD_ID");
    Card card = buildCard();
    given(debitCardManagerService.findByCardIdHash(anyString())).willReturn(Optional.of(card));
    given(subscriptionService.findById(SUBSCRIPTION_KEY)).willReturn(Optional.empty());

    assertThatThrownBy(() -> unit.process(cain003))
        .isInstanceOf(CardClearingSubscriptionNotFoundException.class)
        .hasMessage(SUBSCRIPTION_ERROR);
  }


  @Test
  @DisplayName("Matching Logic Passed with different amount transaction but throwing exception when try to retrieve Debt Card Management.")
  void matchingLogicPassedDifferentTransactionAmountDebtCardManagementError() {

    //given
    Cain003 cain003 = mock(Cain003.class);
    AuthTransaction authTransaction = buildAuthTransaction(
        new BigDecimal(200));

    given(cain003.getCardId()).willReturn(CARD_ID);
    given(clearingMatchLogic.matchingLogic(cain003)).willReturn(Optional.of(authTransaction));
    given(panHashingService.hashCardId(CARD_ID)).willReturn(HASH_CARD_ID);
    given(debitCardManagerService.findByCardIdHash(HASH_CARD_ID)).willReturn(Optional.empty());

    assertThatThrownBy(() -> unit.process(cain003))
        .isInstanceOf(CardClearingDebitCardManagementException.class)
        .hasMessage(DEBT_CARD_MGN_ERROR);
  }

  private Fee buildFee() {
    return Fee.builder()
        .id(UUID.randomUUID())
        .transactionId(UUID.randomUUID().toString())
        .amount(new BigDecimal(200))
        .description("teste")
        .transactionCode("TSX")
        .tax(Tax.builder().statementDescription("test").parentTransactionId("132")
            .taxAmount(new BigDecimal(123)).build())
        .build();
  }

  private AuthTransaction buildAuthTransaction(BigDecimal amount, boolean withFee) {
    return CardAuth.builder()
        .cardId(CardClearingServiceTest.CARD_ID)
        .partyKey(PARTY_KEY)
        .productKey(PRODUCT_KEY)
        .tenantKey(TENANT_KEY)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .cain001(buildCain001(amount, withFee))
        .build();
  }

  private AuthTransaction buildAuthTransaction(BigDecimal amount) {
    return buildAuthTransaction(amount, true);
  }

  private Cain001 buildCain001(BigDecimal amount, boolean withFee) {
    return Cain001.builder()
        .transactionAmount(Money.builder()
            .amount(amount)
            .currency(Currency.getInstance(Locale.UK))
            .build())
        .billingAmount(Money.builder()
            .amount(amount)
            .currency(Currency.getInstance(Locale.UK))
            .build())
        .merchantCategoryCode("teste")
        .transactionDate(Instant.now())
        .accountQualifier("teste")
        .cardId(CARD_ID)
        .transactionId(UUID.randomUUID())
        .correlationId(UUID.randomUUID())
        .pointOfServiceEntryMode("XP")
        .pointOfServiceConditionCode("XPTO")
        .networkId("3231")
        .cardAcceptorCountryCode("432432")
        .banknetReferenceNumber(REFERENCE_NUMBER)
        .cardTransactionType(CardTransactionType.AUTH)
        .processingCode("12312")
        .fee(withFee ? buildFee() : null)
        .paymentMethodType(PaymentMethodType.CONTACTLESS)
        .build();
  }

  private Card buildCard() {
    return Card.builder()
        .subscriptionKey(SUBSCRIPTION_KEY)
        .partyKey(PARTY_KEY)
        .tenantKey(TENANT_KEY)
        .cardEffectiveDate(Instant.now())
        .cardExpiryDate(Instant.parse("2022-12-11T10:12:35Z"))
        .processorAccountId("12345")
        .subscriptionStatus(SubscriptionStatus.ACTIVE)
        .cardStatus(CardStatus.ACTIVE)
        .panHash("3123131231").build();
  }

  private Optional<Subscription> buildSubscription() {

    List<TransactionLimit> transactionLimits = new ArrayList<>();
    transactionLimits.add(TransactionLimit.builder()
        .maximumAmount(new BigDecimal(1000))
        .minimumAmount(new BigDecimal(100))
        .resetPeriod(ResetPeriodEnums.TRANSACTION)
        .transactionName(TransactionNameEnums.ATMWITHDRAWAL).build());

    List<SubscriptionSettings> subscriptionSettingsList = new ArrayList<>();
    subscriptionSettingsList.add(SubscriptionSettings.builder()
        .effectiveDate(Instant.now())
        .productKey(PRODUCT_KEY)
        .hasFees(false)
        .transactionLimits(transactionLimits)
        .build());

    return Optional.of(Subscription.builder()
        .subscriptionKey(SUBSCRIPTION_KEY)
        .settings(subscriptionSettingsList)
        .status(SubscriptionStatus.ACTIVE)
        .build());
  }

}
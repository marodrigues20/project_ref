package com.tenxbanking.cardrails.domain.service.clearing;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.CreditDebitEnum;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.card.Merchant;
import com.tenxbanking.cardrails.domain.model.transaction.Address;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.port.finder.AuthFinder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClearingMatchLogicTest {

  private static String CARD_ID = "12312343423243";
  private static Instant CREATED_DATE = Instant.parse("2019-10-25T10:12:35Z");
  private static String AUTH_CODE = "4847293472";
  private static boolean IS_SUCCESS = true;
  private static final String SUBSCRIPTION_KEY = "4d4d8f3b-3b81-44f3-968d-d1c1a48b4a10";
  private static final String REFERENCE_NUMBER = "4535";
  private static final String TRANSACTION_LIFE_CYCLE_ID = "132143423123123";


  @Mock
  private AuthFinder authFinder;
  @InjectMocks
  private ClearingMatchLogic clearingMatchLogic;

  @Test
  @DisplayName("Matching Logic has not been succeeded.")
  void noMatchingLogic() {

    Cain003 cain003 = buildCain003(CARD_ID, TRANSACTION_LIFE_CYCLE_ID, CREATED_DATE,
        new BigDecimal(200), AUTH_CODE);

    Optional<AuthTransaction> authTransaction = Optional.empty();

    given(authFinder
        .findByCardIdAndTransactionId(cain003.getCardId(),
            cain003.getTransactionLifeCycleID()))
        .willReturn(authTransaction);

    given(authFinder
        .findByCardIdAndCreatedDateAndCardAmountAndCardAuth(cain003.getCardId(),
            cain003.getCreatedDate(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransaction);

    given(authFinder
        .findByCardIdAndTransactionAmountAndTransactionAuthCode(cain003.getCardId(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransaction);

    given(authFinder
        .findByCardIdAndTransactionDateAndTransactionAuthCode(cain003.getCardId(),
            cain003.getCreatedDate(),
            cain003.getAuthCode())).willReturn(authTransaction);

    given(authFinder
        .findByCardIdAndTransactionDate(cain003.getCardId(), cain003.getCreatedDate()))
        .willReturn(authTransaction);

    clearingMatchLogic.matchingLogic(cain003);

    assertNotNull(authTransaction);


  }

  @Test
  @DisplayName("Find by CardId and TransactionLifeCycleID")
  void findByCardIdAndTransactionIdAndIsSuccess() {

    Cain003 cain003 = buildCain003(CARD_ID, TRANSACTION_LIFE_CYCLE_ID, CREATED_DATE,
        new BigDecimal(200), AUTH_CODE);

    Optional<AuthTransaction> authTransactionNoResult = Optional.empty();
    Optional<AuthTransaction> authTransaction = buildAuthTransaction(CARD_ID,
        new BigDecimal(100), AUTH_CODE);

    given(authFinder
        .findByCardIdAndTransactionId(cain003.getCardId(),
            cain003.getTransactionLifeCycleID()))
        .willReturn(authTransaction);

    clearingMatchLogic.matchingLogic(cain003);

    assertNotNull(authTransaction);
    assertNotNull(authTransaction.get().getCardId());
    assertNotNull(authTransaction.get().getCain001().getTransactionAmount());
    assertNotNull(authTransaction.get().getCain001().getFee());
    assertNotNull(authTransaction.get().getCain001().getFee().get().getTax());
  }


  @Test
  @DisplayName("Find by CardId and CreatedDate and CardAmount adn CardAuth")
  void findByCardIdAndCreatedDateAndCardAmountAndCardAuthAndIsSuccess() {

    Cain003 cain003 = buildCain003(CARD_ID, TRANSACTION_LIFE_CYCLE_ID, CREATED_DATE,
        new BigDecimal(100), AUTH_CODE);

    Optional<AuthTransaction> authTransactionNoResult = Optional.empty();
    Optional<AuthTransaction> authTransaction = buildAuthTransaction(CARD_ID,
        new BigDecimal(100), AUTH_CODE);

    given(authFinder
        .findByCardIdAndTransactionId(cain003.getCardId(),
            cain003.getTransactionLifeCycleID()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndCreatedDateAndCardAmountAndCardAuth(cain003.getCardId(),
            cain003.getCreatedDate(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransaction);

    clearingMatchLogic.matchingLogic(cain003);

    assertNotNull(authTransaction);
    assertNotNull(authTransaction.get().getCardId());
    assertNotNull(authTransaction.get().getCain001().getTransactionAmount());
    assertNotNull(authTransaction.get().getCain001().getFee());
    assertNotNull(authTransaction.get().getCain001().getFee().get().getTax());


  }

  @Test
  @DisplayName("Find by CardId and TransactionAmount and TransactionAuthCode")
  void findByCardIdAndTransactionAmountAndTransactionAuthCodeAndIsSuccess() {

    Cain003 cain003 = buildCain003(CARD_ID, TRANSACTION_LIFE_CYCLE_ID, CREATED_DATE,
        new BigDecimal(100), AUTH_CODE);

    Optional<AuthTransaction> authTransactionNoResult = Optional.empty();
    Optional<AuthTransaction> authTransaction = buildAuthTransaction(CARD_ID,
        new BigDecimal(100), AUTH_CODE);

    given(authFinder
        .findByCardIdAndTransactionId(cain003.getCardId(),
            cain003.getTransactionLifeCycleID()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndCreatedDateAndCardAmountAndCardAuth(cain003.getCardId(),
            cain003.getCreatedDate(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndTransactionAmountAndTransactionAuthCode(cain003.getCardId(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransaction);

    clearingMatchLogic.matchingLogic(cain003);

    assertNotNull(authTransaction);
    assertNotNull(authTransaction.get().getCardId());
    assertNotNull(authTransaction.get().getCain001().getTransactionAmount());
    assertNotNull(authTransaction.get().getCain001().getFee());
    assertNotNull(authTransaction.get().getCain001().getFee().get().getTax());
  }

  @Test
  @DisplayName("Find by CardId and CreatedDate and TransactionAuthCode ")
  void findByCardIdAndTransactionDateAndTransactionAuthCodeAndIsSuccess() {

    Cain003 cain003 = buildCain003(CARD_ID, TRANSACTION_LIFE_CYCLE_ID, CREATED_DATE,
        new BigDecimal(200), AUTH_CODE);

    Optional<AuthTransaction> authTransactionNoResult = Optional.empty();
    Optional<AuthTransaction> authTransaction = buildAuthTransaction(CARD_ID,
        new BigDecimal(100), AUTH_CODE);

    given(authFinder
        .findByCardIdAndTransactionId(cain003.getCardId(),
            cain003.getTransactionLifeCycleID()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndCreatedDateAndCardAmountAndCardAuth(cain003.getCardId(),
            cain003.getCreatedDate(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndTransactionAmountAndTransactionAuthCode(cain003.getCardId(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndTransactionDateAndTransactionAuthCode(cain003.getCardId(),
            cain003.getCreatedDate(),
            cain003.getAuthCode())).willReturn(authTransaction);

    clearingMatchLogic.matchingLogic(cain003);

    assertNotNull(authTransaction);
    assertNotNull(authTransaction.get().getCardId());
    assertNotNull(authTransaction.get().getCain001().getTransactionAmount());
    assertNotNull(authTransaction.get().getCain001().getFee());
    assertNotNull(authTransaction.get().getCain001().getFee().get().getTax());


  }

  @Test
  @DisplayName("Find by CardId and CreatedDate")
  void findByCardIdAndTransactionDateIsSuccess() {

    Cain003 cain003 = buildCain003(CARD_ID, TRANSACTION_LIFE_CYCLE_ID, CREATED_DATE,
        new BigDecimal(200), AUTH_CODE);

    Optional<AuthTransaction> authTransactionNoResult = Optional.empty();
    Optional<AuthTransaction> authTransaction = buildAuthTransaction(CARD_ID,
        new BigDecimal(100), AUTH_CODE);

    given(authFinder
        .findByCardIdAndTransactionId(cain003.getCardId(),
            cain003.getTransactionLifeCycleID()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndCreatedDateAndCardAmountAndCardAuth(cain003.getCardId(),
            cain003.getCreatedDate(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndTransactionAmountAndTransactionAuthCode(cain003.getCardId(),
            cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndTransactionDateAndTransactionAuthCode(cain003.getCardId(),
            cain003.getCreatedDate(),
            cain003.getAuthCode())).willReturn(authTransactionNoResult);

    given(authFinder
        .findByCardIdAndTransactionDate(cain003.getCardId(), cain003.getCreatedDate()))
        .willReturn(authTransaction);

    clearingMatchLogic.matchingLogic(cain003);

    assertNotNull(authTransaction);
    assertNotNull(authTransaction.get().getCardId());
    assertNotNull(authTransaction.get().getCain001().getTransactionAmount());
    assertNotNull(authTransaction.get().getCain001().getFee());
    assertNotNull(authTransaction.get().getCain001().getFee().get().getTax());
  }

  private Optional<AuthTransaction> buildAuthTransaction(String cardId, BigDecimal amount,
      String authCode) {

    return Optional.ofNullable(CardAuth.builder()
        .cardId(cardId)
        .partyKey(UUID.randomUUID())
        .productKey(UUID.randomUUID())
        .tenantKey(UUID.randomUUID().toString())
        .subscriptionKey(UUID.fromString(SUBSCRIPTION_KEY))
        .cain001(buildCain001(amount, authCode))
        .build());

  }

  private Cain001 buildCain001(BigDecimal amount, String authCode) {
    return Cain001.builder()
        .authCode(authCode)
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
        .pointOfServiceEntryMode("XP")
        .pointOfServiceConditionCode("XPTO")
        .networkId("3231")
        .cardAcceptorCountryCode("432432")
        .banknetReferenceNumber(REFERENCE_NUMBER)
        .cardTransactionType(CardTransactionType.AUTH)
        .processingCode("12312")
        .fee(buildFee())
        .paymentMethodType(PaymentMethodType.CONTACTLESS)
        .build();
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


  private Cain003 buildCain003(String cardId, String transactionLifeCycleId, Instant createdDate,
      BigDecimal transactionAmount, String authCode) {
    return Cain003.builder()
        .paymentMethodType(PaymentMethodType.CONTACTLESS)
        .conversionRate(new BigDecimal(2))
        .processingCode("1234")
        .cardId(cardId)
        .transactionLifeCycleID(transactionLifeCycleId)
        .transactionDate(Instant.now())
        .merchantCategoryCode("3423")
        .cardAcceptorCountryCode("1234")
        .banknetReferenceNumber("23123212")
        .accountQualifier("32123")
        .authCode(authCode)
        .billingAmount(
            Money.builder().amount(new BigDecimal(234)).currency(Currency.getInstance(Locale.UK))
                .build())
        .cardExpiryDate("11/22")
        .createdDate(Instant.now())
        .merchant(Merchant.builder().acceptorIdCode("321").categoryCode("213").name("XPTO")
            .terminalId("432").address(
                Address.builder().cityName("London").countryCode("123").postCode("w2453")
                    .stateCode("LD").build()).build())
        .messageType(CreditDebitEnum.DEBIT)
        .networkId("42342342")
        .pointOfServiceConditionCode("1321231")
        .pointOfServiceEntryMode("4342342")
        .retrievalReferenceNumber("3123141241")
        .settlementAmount(Money.builder().amount(new BigDecimal(200)).currency(Currency.getInstance(Locale.UK)).build())
        .transactionAmount(
            Money.builder().amount(transactionAmount).currency(Currency.getInstance(Locale.UK))
                .build())
        .transactionLifeCycleID("3213434")
        .cardTransactionType(CardTransactionType.CLEARING)
        .updatedBalance(Money.builder().amount(new BigDecimal(1231)).currency(Currency.getInstance(Locale.UK)).build())
        .build();

  }

}


package com.tenx.universalbanking.transactionmanager.service.turbine;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH_VIA_ADVICE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.model.CardAuth;
import com.tenx.universalbanking.transactionmanager.model.SubscriptionStatus;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.CreditDebitEnum;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CardAuthTransactionMessageBuilderTest {

  @InjectMocks
  private CardAuthTransactionMessageBuilder messageBuilder;
  @Mock
  private GeneratorUtil generatorUtil;
  private static final String REQUEST_ID = "123456";
  private static final UUID PARTY_KEY = UUID.randomUUID();
  private static final UUID PRODUCT_KEY = UUID.randomUUID();
  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();
  private static final Instant TRANSACTION_DATE = Instant.parse("2019-03-18T10:12:14.156Z");
  private final Clock fixedClock = Clock.fixed(TRANSACTION_DATE, UTC);

  @Before
  public void setupGeneratorUtil() {
    when(generatorUtil.generateRandomKey()).thenReturn(REQUEST_ID);
  }

  @Test
  public void shouldCreateMessageWhenAdviceTrue() {
    CardAuth build = createCardAuth();
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(createPaymentMessageAdditionalInfo());
    assertThat(paymentMessage.getMessage()).isEqualTo(createPaymentMessage());
  }

  @Test
  public void shouldCreateMessageWhenTransactionTypeTrue() {
    CardAuth build = createCardAuth();
    build.setTransactionType("01");
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(createPaymentMessageAdditionalInfo());
    Map<String, Object> map = createPaymentMessage();
    map.put("TRANSACTION_TYPE", "01");

    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  public void shouldCreateMessageWhenTransactionCardDataEntryModeTrue() {
    CardAuth build = createCardAuth();
    build.setTransactionType("01");
    build.setCardDataEntryMode("051");
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "INTERNATIONAL_CASH_WITHDRAWAL");
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);
    Map<String, Object> map = createPaymentMessage();
    map.put("TRANSACTION_TYPE", "01");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  public void shouldCreateMessageWhenIsPurchaseTrue() {
    CardAuth build = createCardAuth();
    build.setTransactionType("00");
    build.setCardDataEntryMode("051");
    build.setCardConditionCode("00");
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "INTERNATIONAL_POS_CHIP_AND_PIN");
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage();
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "00");
    map.put("TRANSACTION_TYPE", "00");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  public void shouldCreateMessageWhenIsPosMagStripeTrue() {
    CardAuth build = createCardAuth();
    build.setTransactionType("00");
    build.setCardDataEntryMode("02");
    build.setCardConditionCode("00");
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "POS_MAG_STRIPE");
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage();
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "00");
    map.put("TRANSACTION_TYPE", "00");
    map.put("CARD_DATA_ENTRY_MODE", "MGST");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  public void shouldCreateMessageWhenIsAtmMagStripeTrue() {
    CardAuth build = createCardAuth();
    build.setTransactionType("01");
    build.setCardDataEntryMode("02");
    build.setCardConditionCode("00");
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "ATM_MAG_STRIPE");
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage();
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "00");
    map.put("TRANSACTION_TYPE", "01");
    map.put("CARD_DATA_ENTRY_MODE", "MGST");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  public void shouldCreateMessageWhenIsMailTelephoneOrderTrue() {
    CardAuth build = createCardAuth();
    build.setCardDataEntryMode("01");
    build.setCardConditionCode("01");
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "MAIL_TELEPHONE_ORDER");

    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage();
    map.put("CARD_DATA_ENTRY_MODE", "PHYS");
    map.put("CARDHOLDER_PRESENT", false);
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  public void shouldCreateMessageWhenIsOnlineTrue() {
    CardAuth build = createCardAuth();
    build.setCardDataEntryMode("81");
    build.setCardConditionCode("08");
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "ONLINE");

    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage();
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "08");
    map.put("CARD_DATA_ENTRY_MODE", "PHYS");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  public void shouldCreateMessageWhenIsContactlessTrue() {
    CardAuth build = createCardAuth();
    build.setTransactionType("00");
    build.setCardDataEntryMode("07");
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, true);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "CONTACTLESS");

    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage();
    map.put("CARDHOLDER_PRESENT", true);
    map.put("TRANSACTION_TYPE", "00");
    map.put("CARD_DATA_ENTRY_MODE", "CICC");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }


  @Test
  public void shouldCreateMessageWhenAdviceFalse() {
    CardAuth build = createCardAuth();
    Card card = createCard();

    TransactionMessage transactionMessage = messageBuilder.create(build, card, false);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo());
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(createPaymentMessageAdditionalInfo());
    assertThat(paymentMessage.getMessage()).isEqualTo(createPaymentMessage());
  }

  private Map<String, Object> createPaymentMessage() {
    Map<String, Object> map = new HashMap<>();
    map.put("AMOUNT", "1");
    map.put("BANKNET_REFERENCE_NUMBER", "banknetReference");
    map.put("CARDHOLDER_PRESENT", true);
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "01");
    map.put("CARD_ACCEPTOR_ID", null);
    map.put("CARD_ACCEPTOR_NAME", null);
    map.put("CARD_DATA_ENTRY_MODE", null);
    map.put("CARD_TOKEN", "id");
    map.put("EXCHANGE_RATE", ONE);
    map.put("INITIATOR_PARTY_ID", null);
    map.put("INITIATOR_TRANSACTION_ID", null);
    map.put("MERCHANT_CATEGORY_CODE", "merchantCatCode");
    map.put("TOTAL_AMOUNT", "10");
    map.put("TRANSACTION_AMOUNT_QUALIFIER", "ACTL");
    map.put("TRANSACTION_CURRENCY_CODE", "currencyCode");
    map.put("TRANSACTION_DATE", "2019-03-18T10:12:14.156+0000");
    map.put("TRANSACTION_FEE_AMT", null);
    map.put("TRANSACTION_TIME", "10:12:14.156");
    map.put("TRANSACTION_TYPE", "transactionType");
    map.put("VALUE_DATE", "2019-03-18");
    return map;
  }

  private Map<String, Object> createPaymentMessageAdditionalInfo() {
    Map<String, Object> map = new HashMap<>();
    map.put("CARD_PROCESSOR_ACCOUNT_ID", null);
    map.put("DEBIT_CREDIT_INDICATOR", "CREDIT");
    map.put("PARTY_KEY", PARTY_KEY.toString());
    map.put("PAYMENT_METHOD_TYPE", "UNKNOWN");
    map.put("PRODUCT_KEY", PRODUCT_KEY.toString());
    map.put("SUBSCRIPTION_KEY", SUBSCRIPTION_KEY.toString());
    map.put("SUBSCRIPTION_STATUS", "ACTIVE");
    return map;
  }

  private Map<String, Object> createTransactionAdditionalInfo() {
    Map<String, Object> map = new HashMap<>();
    map.put("REQUEST_ID", "123456");
    map.put("TENANT_PARTY_KEY", "10000");
    return map;
  }

  private CardAuth createCardAuth() {
    return CardAuth.builder()
        .totalAmount(TEN)
        .amount(ONE)
        .transactionCurrencyCode("currencyCode")
        .merchantCategoryCode("merchantCatCode")
        .transactionDatetime(TRANSACTION_DATE)
        .transactionDate(LocalDate.now(fixedClock))
        .transactionTime("10:12:14.156")
        .creditDebit(CreditDebitEnum.CREDIT)
        .cardId("pan")
        .transactionType("transactionType")
        .exchangeRate(ONE)
        .cardDataEntryMode("posEntryMode")
        .cardConditionCode("01")
        .merchantCountryCode("99")
        .conversionRate(ONE)
        .banknetReference("banknetReference")
        .build();
  }

  private Card createCard() {
    return Card.builder()
        .id("id")
        .cardCountryCode("cardCountryCode")
        .cardCurrencyCode("cardCurrencyCode")
        .partyKey(PARTY_KEY)
        .tenantKey("10000")
        .subscriptionKey(SUBSCRIPTION_KEY)
        .subscriptionStatus(SubscriptionStatus.ACTIVE)
        .productKey(PRODUCT_KEY)
        .build();
  }

}
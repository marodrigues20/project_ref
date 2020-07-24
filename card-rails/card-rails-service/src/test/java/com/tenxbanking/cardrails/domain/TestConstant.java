package com.tenxbanking.cardrails.domain;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.card.Channel.ATM;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CARD_HOLDER_NOT_PRESENT;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.card.Channel.INTERNATIONAL;
import static com.tenxbanking.cardrails.domain.model.card.Channel.MAG_STRIPE;
import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.ACTIVE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.CreditDebitEnum;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.model.card.Channel;
import com.tenxbanking.cardrails.domain.model.card.ChannelSettings;
import com.tenxbanking.cardrails.domain.model.card.Merchant;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.transaction.Address;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;

public class TestConstant {

  public static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();
  public static final UUID TRANSACTION_ID = UUID.randomUUID();
  public static final UUID CORRELATION_ID = UUID.randomUUID();
  public static final UUID PARTY_KEY = UUID.randomUUID();
  public static final UUID PRODUCT_KEY = UUID.randomUUID();
  public static final String TENANT_KEY = "10000";
  public static final String CARD_ID = "aCardId";
  public static final String PAN_HASH = "aPanHash";
  public static final String BANKNET_REFERENCE_NUMBER = "banknetReferenceNumber";
  public static final String RETRIEVAL_REFERENCE_NUMBER = "retrievalReferenceNumber";
  public static final Currency GBP = Currency.getInstance("GBP");

  public static final Card CARD = Card.builder()
      .panHash(PAN_HASH)
      .tenantKey(TENANT_KEY)
      .partyKey(PARTY_KEY)
      .subscriptionKey(SUBSCRIPTION_KEY)
      .cardEffectiveDate(Instant.now())
      .cardExpiryDate(Instant.now())
      .subscriptionStatus(ACTIVE)
      .cardStatus(CardStatus.ACTIVE)
      .build();

  public static final Cain001 CAIN_001 = Cain001.builder()
      .transactionAmount(Money.of(TEN, "GBP"))
      .billingAmount(Money.of(ONE, "GBP"))
      .settlementAmount(Money.of(ZERO, "GBP"))
      .merchantCategoryCode("merchantCatCode")
      .transactionDate(Instant.now())
      .accountQualifier("accountQual")
      .cardId(CARD_ID)
      .processingCode("processingCode")
      .conversionRate(ONE)
      .cardExpiryDate("expiryDate")
      .pointOfServiceEntryMode("posEntryMode")
      .networkId("networkId")
      .transactionId(TRANSACTION_ID)
      .pointOfServiceConditionCode("01")
      .cardAcceptorCountryCode("99")
      .correlationId(CORRELATION_ID)
      .cardTransactionType(AUTH)
      .banknetReferenceNumber(BANKNET_REFERENCE_NUMBER)
      .retrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)
      .paymentMethodType(PaymentMethodType.CONTACTLESS)
      .build();

  public static final SubscriptionSettings SETTINGS = SubscriptionSettings.builder()
      .effectiveDate(Instant.now().minus(Duration.ofDays(1)))
      .productKey(PRODUCT_KEY)
      .productVersion(1)
      .hasFees(true)
      .transactionLimits(emptyList())
      .build();

  public static final Subscription SUBSCRIPTION = Subscription.builder()
      .subscriptionKey(SUBSCRIPTION_KEY)
      .settings(Collections.singletonList(SETTINGS))
      .status(ACTIVE)
      .build();

  public static final Cain002 CAIN_OO2 = new Cain002("123456", Money.of(TEN, "GBP"),
      AuthResponseCode._00, true);

  public static final Fee FEE = Fee.builder().amount(BigDecimal.ONE).build();

  public static final CardSettings CARD_SETTINGS = CardSettings
      .builder()
      .panHash(PAN_HASH)
      .channelSettings(
          ChannelSettings
              .builder()
              .settings(Map.of(
                  ATM, true,
                  CARD_HOLDER_NOT_PRESENT, true,
                  Channel.CONTACTLESS, true,
                  INTERNATIONAL, true,
                  MAG_STRIPE, true,
                  CHIP_AND_PIN, true
              )).build())
      .build();


  public static final Cain003 CAIN_003 = Cain003.builder()
      .transactionAmount(Money.of(TEN, "GBP"))
      .billingAmount(Money.of(ONE, "GBP"))
      .settlementAmount(Money.of(ZERO, "GBP"))
      .merchantCategoryCode("merchantCatCode")
      .transactionDate(Instant.parse("2019-11-23T10:12:35Z"))
      .accountQualifier("accountQual")
      .cardId(CARD_ID)
      .processingCode("processingCode")
      .conversionRate(ONE)
      .cardExpiryDate("expiryDate")
      .pointOfServiceEntryMode("posEntryMode")
      .networkId("networkId")
      .transactionId(TRANSACTION_ID)
      .pointOfServiceConditionCode("01")
      .cardAcceptorCountryCode("99")
      .correlationId(CORRELATION_ID)
      .cardTransactionType(AUTH)
      .banknetReferenceNumber(BANKNET_REFERENCE_NUMBER)
      .retrievalReferenceNumber(RETRIEVAL_REFERENCE_NUMBER)
      .paymentMethodType(PaymentMethodType.CONTACTLESS)
      .createdDate(Instant.parse("2019-11-23T10:12:35Z"))
      .messageType(CreditDebitEnum.DEBIT)
      .merchant(Merchant.builder().name("test").build())
      .transactionLifeCycleID("1231231")
      .authCode("Auth Code")
      .merchant(
          Merchant.builder()
              .categoryCode("Category Code Test")
              .acceptorIdCode("Test Id")
              .name("Shop Test")
              .address(Address.builder().countryCode("test 123").build())
              .build()).build();


  public static final Fee buildFee() {
    return Fee.builder()
        .id(UUID.randomUUID())
        .transactionId(UUID.randomUUID().toString())
        .amount(TEN)
        .description("teste")
        .transactionCode("TSX")
        .tax(Tax.builder().statementDescription("test").parentTransactionId("132")
            .taxAmount(new BigDecimal(123)).transactionId(UUID.randomUUID().toString()).build())
        .transactionDate("22/11/2019")
        .feeCurrencyCode("12312")
        .valueDateTime("22/11/2019")
        .transactionCorrelationId("33123121")
        .transactionId(UUID.randomUUID().toString())
        .build();
  }


}

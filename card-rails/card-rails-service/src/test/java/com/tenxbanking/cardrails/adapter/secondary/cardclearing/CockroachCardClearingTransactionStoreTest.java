package com.tenxbanking.cardrails.adapter.secondary.cardclearing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.CreditDebitEnum;
import com.tenxbanking.cardrails.adapter.secondary.cardclearing.util.Cain003MessageBuilder;
import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionMessageEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionMessageCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.transformer.CardClearingTransactionDomainToEntityTransformer;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.card.Merchant;
import com.tenxbanking.cardrails.domain.model.transaction.Address;
import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CockroachCardClearingTransactionStoreTest {

  private static final String CARD_ID = "3213143234132131";
  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();
  private static final UUID PARTY_KEY = UUID.randomUUID();
  private static final UUID PRODUCT_KEY = UUID.randomUUID();
  private static final String TENANT_KEY = "3212321432";


  @Mock
  private CardTransactionCockroachRepository cardTransactionCockroachRepository;
  @Spy
  private CardClearingTransactionDomainToEntityTransformer domainToEntityTransformer;
  @Mock
  private TransactionMessageCockroachRepository transactionMessageCockroachRepository;
  @Spy
  private Cain003MessageBuilder cain003MessageBuilder;
  @InjectMocks
  CockroachCardClearingTransactionStore cockroachCardClearingTransactionStore;

  @Test
  void saveCardClearingWithSuccessful() {
    //given
    CardClearing cardClearing = buildCardClearing();

    //when
    cockroachCardClearingTransactionStore.save(cardClearing);

    //then
    then(cardTransactionCockroachRepository).should(times(1))
        .save(any(CardTransactionEntity.class));
    then(transactionMessageCockroachRepository).should(times(1))
        .save(any(TransactionMessageEntity.class));

  }

  private CardClearing buildCardClearing() {

    final Cain003 cain003 = buildCain003();

    return new CardClearing(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain003.withFee(buildFee())
    );


  }


  private Cain003 buildCain003() {
    return Cain003.builder()
        .transactionId(UUID.randomUUID())
        .correlationId(UUID.randomUUID())
        .paymentMethodType(PaymentMethodType.CONTACTLESS)
        .conversionRate(new BigDecimal(2))
        .processingCode("1234")
        .cardId("4565542343546789")
        .transactionDate(Instant.now())
        .merchantCategoryCode("3423")
        .cardAcceptorCountryCode("1234")
        .banknetReferenceNumber("23123212")
        .accountQualifier("32123")
        .authCode("123124")
        .billingAmount(
            Money.builder().amount(new BigDecimal(234)).currency(Currency.getInstance(Locale.UK))
                .build())
        .cardExpiryDate("11/22")
        .createdDate(Instant.parse("1995-10-23T10:12:35Z"))
        .merchant(Merchant.builder().acceptorIdCode("321").categoryCode("213").name("XPTO")
            .terminalId("432").address(
                Address.builder().cityName("London").countryCode("123").postCode("w2453")
                    .stateCode("LD").build()).build())
        .messageType(CreditDebitEnum.DEBIT)
        .networkId("42342342")
        .pointOfServiceConditionCode("1321231")
        .pointOfServiceEntryMode("4342342")
        .retrievalReferenceNumber("3123141241")
        .settlementAmount(
            Money.builder().amount(new BigDecimal(200)).currency(Currency.getInstance(Locale.UK))
                .build())
        .transactionAmount(
            Money.builder().amount(new BigDecimal(122)).currency(Currency.getInstance(Locale.UK))
                .build())
        .transactionLifeCycleID("3213434")
        .cardTransactionType(CardTransactionType.CLEARING)
        .updatedBalance(Money.builder().amount(new BigDecimal(1231)).currency(Currency.getInstance(Locale.UK)).build())
        .build();

  }

  private Fee buildFee() {
    return Fee.builder()
        .id(UUID.randomUUID())
        .transactionCorrelationId(UUID.randomUUID().toString())
        .transactionId(UUID.randomUUID().toString())
        .amount(new BigDecimal(200))
        .feeCurrencyCode("GBP")
        .description("teste")
        .transactionCode("TSX")
        .valueDateTime(Instant.now().toString())
        .transactionDate(Instant.now().toString())
        .tax(Tax.builder()
            .transactionId(UUID.randomUUID().toString())
            .parentTransactionId(UUID.randomUUID().toString())
            .statementDescription("test")
            .parentTransactionId("132")
            .taxAmount(new BigDecimal(123))
            .build())
        .build();
  }

}

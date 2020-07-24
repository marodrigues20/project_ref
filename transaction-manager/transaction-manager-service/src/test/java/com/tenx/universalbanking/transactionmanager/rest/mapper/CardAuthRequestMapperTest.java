package com.tenx.universalbanking.transactionmanager.rest.mapper;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenx.universalbanking.transactionmanager.exception.RequestValidationFailureException;
import com.tenx.universalbanking.transactionmanager.model.CardAuth;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Amounts;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Card;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.CreditDebitEnum;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Merchant;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.MerchantAddress;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Money;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Pos;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.SchemeMessage;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Transaction;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.TransactionRelatedDates;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.Test;

public class CardAuthRequestMapperTest {

  private final CardAuthRequestMapper mapper = new CardAuthRequestMapper();

  private static final Instant INSTANT = Instant.parse("2019-03-18T10:12:14.156Z");
  private final Clock fixedClock = Clock.fixed(INSTANT, UTC);
  private static final LocalDate SETTLEMENT_DATE = LocalDate.now();
  private static final OffsetDateTime TRANSMISSION_DATETIME = OffsetDateTime.now();

  @Test
  public void shouldMapRequestToCardAuth() {
    CardAuth cardAuth = mapper.toDomain(createRequest());

    assertThat(cardAuth.getAmount()).isEqualTo(TEN);
    assertThat(cardAuth.getTotalAmount()).isEqualTo(ONE);
    assertThat(cardAuth.getCardDataEntryMode()).isEqualTo("entryMode");
    assertThat(cardAuth.getCardConditionCode()).isEqualTo("conditionCode");
    assertThat(cardAuth.getExchangeRate()).isEqualTo(ONE);
    assertThat(cardAuth.getMerchantCategoryCode()).isEqualTo("merchCatCode");
    assertThat(cardAuth.getMerchantCountryCode()).isEqualTo("GBR");
    assertThat(cardAuth.getMerchantName()).isEqualTo("name");
    assertThat(cardAuth.getCardId()).isEqualTo("1234");
    assertThat(cardAuth.getTransactionCurrencyCode()).isEqualTo("GBP");
    assertThat(cardAuth.getTransactionDatetime().toString()).isEqualTo("2019-03-18T10:12:14Z");
    assertThat(cardAuth.getTransactionDate().toString()).isEqualTo("2019-03-18");
    assertThat(cardAuth.getTransactionTime()).isEqualTo("101214");
    assertThat(cardAuth.getTransactionType()).isEqualTo("processingCode");
    assertThat(cardAuth.getCreditDebit()).isEqualTo(CreditDebitEnum.DEBIT);
    assertThat(cardAuth.getCardConditionCode()).isEqualTo("conditionCode");
    assertThat(cardAuth.getSystemTraceNumber()).isEqualTo("systemTraceNumber");
    assertThat(cardAuth.getNetworkCode()).isEqualTo("networkCode");
    assertThat(cardAuth.getCardAcceptorIdCode()).isEqualTo("acceptorIdCode");
    assertThat(cardAuth.getConversionRate()).isEqualTo(ONE);
  }

  @Test
  public void shouldMapRequestToCardAuthReversal() {

    CardAuth cardAuth = mapper.toDomainForReversal(createRequest());

    assertThat(cardAuth.getAmount()).isEqualTo(TEN);
    assertThat(cardAuth.getTotalAmount()).isEqualTo(ONE);
    assertThat(cardAuth.getCardDataEntryMode()).isEqualTo("entryMode");
    assertThat(cardAuth.getCardConditionCode()).isEqualTo("conditionCode");
    assertThat(cardAuth.getExchangeRate()).isEqualTo(ONE);
    assertThat(cardAuth.getMerchantCategoryCode()).isEqualTo("merchCatCode");
    assertThat(cardAuth.getMerchantCountryCode()).isEqualTo("GBR");
    assertThat(cardAuth.getMerchantName()).isEqualTo("name");
    assertThat(cardAuth.getCardId()).isEqualTo("1234");
    assertThat(cardAuth.getTransactionCurrencyCode()).isEqualTo("GBP");
    assertThat(cardAuth.getTransactionDatetime().toString()).isEqualTo("2019-03-18T10:12:14Z");
    assertThat(cardAuth.getTransactionDate().toString()).isEqualTo("2019-03-18");
    assertThat(cardAuth.getTransactionTime()).isEqualTo("101214");
    assertThat(cardAuth.getTransactionType()).isEqualTo("processingCode");
    assertThat(cardAuth.getCreditDebit()).isEqualTo(CreditDebitEnum.CREDIT);
    assertThat(cardAuth.getCardConditionCode()).isEqualTo("conditionCode");
    assertThat(cardAuth.getSystemTraceNumber()).isEqualTo("systemTraceNumber");
    assertThat(cardAuth.getNetworkCode()).isEqualTo("networkCode");
    assertThat(cardAuth.getCardAcceptorIdCode()).isEqualTo("acceptorIdCode");
    assertThat(cardAuth.getConversionRate()).isEqualTo(ONE);
  }

  @Test
  public void shouldThrowARequestValidationFailureException() {
    SchemeMessage request = createRequest();
    request.setProcessingCode(null);

    assertThatThrownBy(() -> mapper.toDomain(request))
        .isInstanceOf(RequestValidationFailureException.class)
        .hasMessage("transactionType is marked non-null but is null");
  }

  @Test
  public void shouldThrowAnExceptionWhenBillingCurrencyCodeIsEmptyString() {
    assertThatThrownBy(() -> mapper.toDomainForReversal(createRequest("")))
        .isInstanceOf(RequestValidationFailureException.class)
        .hasMessage("transaction currency code is empty and requires a value");
  }

  private SchemeMessage createRequest(String currency) {
    SchemeMessage schemeMessage = new SchemeMessage();
    schemeMessage.setProcessingCode("processingCode");
    schemeMessage.setSystemTraceNumber("systemTraceNumber");

    Card card = new Card();
    card.setId("1234");
    schemeMessage.setCard(card);

    Transaction transaction = new Transaction();
    Amounts amount = new Amounts();
    amount.setBilling(new Money(ONE, currency));
    amount.setSettlement(new Money(TEN, "826"));
    amount.setTransaction(new Money(TEN, "826"));
    amount.setConversionRate(ONE);
    amount.setFee(new Money(ONE, "GBP"));
    transaction.setAmounts(amount);
    transaction.setNetworkCode("networkCode");

    TransactionRelatedDates transactionRelatedDates = new TransactionRelatedDates();
    transactionRelatedDates.setTransactionDate(LocalDate.now(fixedClock));
    transactionRelatedDates.setSettlementDate(SETTLEMENT_DATE);
    transactionRelatedDates.setTransactionTime("101214");
    transactionRelatedDates.setTransmissionDateTime(TRANSMISSION_DATETIME);
    transaction.setTransactionRelatedDates(transactionRelatedDates);

    schemeMessage.setTransaction(transaction);

    Pos pos = new Pos();
    pos.setPosEntryMode("entryMode");
    pos.setConditionCode("conditionCode");
    schemeMessage.setPos(pos);

    Merchant merchant = new Merchant();
    merchant.setCategoryCode("merchCatCode");
    merchant.setName("name");
    merchant.setAcceptorIdCode("acceptorIdCode");
    MerchantAddress merchantAddress = new MerchantAddress();
    merchantAddress.setStateOrCountryCode("GBR");
    merchant.setAddress(merchantAddress);
    schemeMessage.setMerchant(merchant);

    return schemeMessage;
  }

  private SchemeMessage createRequest() {
    return createRequest("826");
  }

}
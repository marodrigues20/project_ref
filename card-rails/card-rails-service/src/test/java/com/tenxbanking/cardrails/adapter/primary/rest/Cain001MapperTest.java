package com.tenxbanking.cardrails.adapter.primary.rest;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.CHIP_PIN;
import static java.math.BigDecimal.ONE;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.primary.rest.exception.RequestValidationFailureException;
import com.tenxbanking.cardrails.adapter.primary.rest.mapper.Cain001Mapper;
import com.tenxbanking.cardrails.adapter.primary.rest.mapper.PaymentMethodTypeMapper;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Amounts;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Card;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Merchant;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.MerchantAddress;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.MessageTypeEnum;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Money;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Pos;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Transaction;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.TransactionRelatedDates;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Cain001MapperTest {

  private static final Instant INSTANT = Instant.parse("2019-03-18T10:12:14.156Z");
  private final Clock fixedClock = Clock.fixed(INSTANT, UTC);

  @Mock
  private PaymentMethodTypeMapper paymentMethodTypeMapper;
  @InjectMocks
  private Cain001Mapper mapper;

  @Test
  void shouldMapRequestBodyToCain001() {
    when(paymentMethodTypeMapper.map(any(), any(), any(), any())).thenReturn(CHIP_PIN);

    Cain001 cain001 = mapper.toDomain(createRequestBody(ONE), AUTH);

    assertThat(cain001.getTransactionAmount().getAmount()).isEqualTo(ONE);
    assertThat(cain001.getBillingAmount().getAmount()).isEqualTo(ONE);
    assertThat(cain001.getSettlementAmount().getAmount()).isEqualTo(ONE);
    assertThat(cain001.getCurrencyCode()).isEqualTo("USD");
    assertThat(cain001.getMerchantCategoryCode()).isEqualTo("merchantCode");
    assertThat(cain001.getTransactionDate()).isEqualTo(Instant.now(fixedClock));
    assertThat(cain001.getAccountQualifier()).isEqualTo("ACTL");
    assertThat(cain001.getCardId()).isEqualTo("123456789");
    assertThat(cain001.getProcessingCode()).isEqualTo("processingCode");
    assertThat(cain001.getConversionRate()).isEqualTo(ONE);
    assertThat(cain001.getCardExpiryDate()).isEqualTo("12/12/2022");
    assertThat(cain001.getPointOfServiceEntryMode()).isEqualTo("entryMode");
    assertThat(cain001.getPointOfServiceConditionCode()).isEqualTo("conditionCode");
    assertThat(cain001.getNetworkId()).isEqualTo("TSYS");
    assertThat(cain001.getCardAcceptorCountryCode()).isEqualTo("merchantAddressCountryCode");
    assertThat(cain001.getBanknetReferenceNumber()).isEqualTo("banknetRefNumber");
    assertThat(cain001.getCardTransactionType()).isEqualTo(AUTH);
    assertThat(cain001.getAuthCode()).isEqualTo("authCode");
    assertThat(cain001.getAuthResponseCode()).isEqualTo(AuthResponseCode._00);
    assertThat(cain001.getPaymentMethodType()).isEqualTo(CHIP_PIN);
  }

  @Test
  void transformsOptionalFields() {
    when(paymentMethodTypeMapper.map(any(), any(), any(), any())).thenReturn(CHIP_PIN);

    SchemeMessage requestBody = createRequestBody(ONE);
    requestBody.getTransaction().getAmounts().setSettlement(null);
    Cain001 cain001 = mapper.toDomain(requestBody, AUTH);

    assertThat(cain001.getSettlementAmount()).isNull();
  }

  @Test
  void shouldThrowRequestValidationException() {
    assertThatThrownBy(() -> mapper.toDomain(createRequestBody(null), AUTH))
        .isInstanceOf(RequestValidationFailureException.class)
        .hasMessage("amount is marked non-null but is null");
  }

  private SchemeMessage createRequestBody(BigDecimal transactionAmount) {
    Amounts amounts = new Amounts();

    Money transactionalAmount = new Money(transactionAmount, "USD");
    Money settlementAmount = new Money(transactionAmount, "USD");
    Money billingAmount = new Money(transactionAmount, "USD");
    amounts.setTransaction(transactionalAmount);
    amounts.setSettlement(settlementAmount);
    amounts.setBilling(billingAmount);
    amounts.setConversionRate(ONE);

    TransactionRelatedDates transactionRelatedDates = new TransactionRelatedDates();
    transactionRelatedDates.setTransactionDate(LocalDate.now(fixedClock));
    transactionRelatedDates.setTransactionTime("10:12:14.156");

    Transaction transaction = new Transaction();
    transaction.setBanknetReferenceNumber("banknetRefNumber");
    transaction.setAmounts(amounts);
    transaction.setTransactionRelatedDates(transactionRelatedDates);
    transaction.setRetrievalReferenceNumber("retrievalReferenceNumber");
    transaction.setNetworkCode("TSYS");

    Merchant merchant = new Merchant();
    merchant.setCategoryCode("merchantCode");
    MerchantAddress merchantAddress = new MerchantAddress();
    merchantAddress.setStateOrCountryCode("merchantAddressCountryCode");
    merchant.setAddress(merchantAddress);

    Pos pos = new Pos();
    pos.setPosEntryMode("entryMode");
    pos.setConditionCode("conditionCode");

    Card card = new Card();
    card.setId("123456789");
    card.setExpiryDate("12/12/2022");

    SchemeMessage schemeMessage = new SchemeMessage();
    schemeMessage.setProcessingCode("processingCode");
    schemeMessage.setTransaction(transaction);
    schemeMessage.setProcessingCode("processingCode");
    schemeMessage.setMerchant(merchant);
    schemeMessage.setPos(pos);
    schemeMessage.setCard(card);
    schemeMessage.setMessageType(MessageTypeEnum.AUTHORISATION);
    schemeMessage.setAuthCode("authCode");
    schemeMessage.setAuthResponseCode("00");

    return schemeMessage;
  }

}
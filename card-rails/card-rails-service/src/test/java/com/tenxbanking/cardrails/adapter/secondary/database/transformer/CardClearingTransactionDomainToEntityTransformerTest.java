package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import static com.tenxbanking.cardrails.domain.TestConstant.BANKNET_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.CORRELATION_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.RETRIEVAL_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TRANSACTION_ID;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.CONTACTLESS;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.CreditDebitEnum;
import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import com.tenxbanking.cardrails.domain.model.card.Merchant;
import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CardClearingTransactionDomainToEntityTransformerTest {

  @InjectMocks
  private CardClearingTransactionDomainToEntityTransformer underTest;

  @Test
  void transform() {
    CardClearing cardClearing = new CardClearing(
        PAN_HASH,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        CAIN_003
    );

    CardTransactionEntity entity = underTest.transform(cardClearing);

    assertSoftly(soft -> {
      soft.assertThat(entity.getId()).isNull();
      soft.assertThat(entity.getTransactionId()).isEqualTo(TRANSACTION_ID.toString());
      soft.assertThat(entity.getCorrelationId()).isEqualTo(CORRELATION_ID.toString());
      soft.assertThat(entity.getPartyKey()).isEqualTo(PARTY_KEY);
      soft.assertThat(entity.getProductKey()).isEqualTo(PRODUCT_KEY);
      soft.assertThat(entity.getTenantKey()).isEqualTo(TENANT_KEY);
      soft.assertThat(entity.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
      soft.assertThat(entity.getCreatedDate()).isEqualTo(CAIN_003.getTransactionDate().toString());
      soft.assertThat(entity.getAuthCode()).isEqualTo(CAIN_003.getAuthCode());
      soft.assertThat(entity.getCardId()).isEqualTo(CARD_ID);
      soft.assertThat(entity.getBanknetReferenceNumber()).isEqualTo(BANKNET_REFERENCE_NUMBER);
      soft.assertThat(entity.getCardTransactionType()).isEqualTo(AUTH);
      soft.assertThat(entity.getRetrievalReferenceNumber()).isEqualTo(RETRIEVAL_REFERENCE_NUMBER);
      soft.assertThat(entity.getTransactionAmount()).isEqualTo(BigDecimal.TEN);
      soft.assertThat(entity.getTransactionCurrency()).isEqualTo("GBP");
      soft.assertThat(entity.getBillingAmount()).isEqualTo(BigDecimal.ONE);
      soft.assertThat(entity.getBillingCurrency()).isEqualTo("GBP");
      soft.assertThat(entity.getSettlementAmount()).isEqualTo(BigDecimal.ZERO);
      soft.assertThat(entity.getSettlementCurrency()).isEqualTo("GBP");
      soft.assertThat(entity.getMerchantCategoryCode()).isEqualTo("merchantCatCode");
      soft.assertThat(entity.getAccountQualifier()).isEqualTo("accountQual");
      soft.assertThat(entity.getProcessingCode()).isEqualTo("processingCode");
      soft.assertThat(entity.getConversionRate()).isEqualTo(BigDecimal.ONE);
      soft.assertThat(entity.getCardExpiryDate()).isEqualTo("expiryDate");
      soft.assertThat(entity.getPointOfServiceEntryMode()).isEqualTo("posEntryMode");
      soft.assertThat(entity.getPointOfServiceConditionCode()).isEqualTo("01");
      soft.assertThat(entity.getNetworkId()).isEqualTo("networkId");
      soft.assertThat(entity.getCardAcceptorCountryCode()).isEqualTo("99");
      soft.assertThat(entity.getUpdatedBalance()).isEqualTo(BigDecimal.TEN);
      soft.assertThat(entity.isSuccess()).isTrue();
      soft.assertThat(entity.getFee()).isNull();
      soft.assertThat(entity.getPaymentMethodType()).isEqualTo(CONTACTLESS);

    });

  }


  private static final Cain003 CAIN_003 = Cain003.builder()
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
      .createdDate(Instant.parse("2019-11-23T10:12:35Z"))
      .messageType(CreditDebitEnum.DEBIT)
      .merchant(Merchant.builder().terminalId("123").name("test").build())
      .transactionLifeCycleID("123")
      .updatedBalance(Money.builder().amount(TEN).currency(Currency.getInstance(Locale.UK)).build())
      .build();

}

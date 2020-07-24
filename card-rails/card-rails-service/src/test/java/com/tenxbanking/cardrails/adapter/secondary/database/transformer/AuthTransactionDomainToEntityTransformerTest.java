package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import static com.tenxbanking.cardrails.domain.TestConstant.BANKNET_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_001;
import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_OO2;
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
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthTransactionDomainToEntityTransformerTest {

  @InjectMocks
  private AuthTransactionDomainToEntityTransformer underTest;

  @Test
  void transform() {
    CardAuth cardAuth = new CardAuth(
        PAN_HASH,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        CAIN_001,
        CAIN_OO2
    );

    CardTransactionEntity entity = underTest.transform(cardAuth);

    assertSoftly(soft -> {
      soft.assertThat(entity.getId()).isNull();
      soft.assertThat(entity.getTransactionId()).isEqualTo(TRANSACTION_ID.toString());
      soft.assertThat(entity.getCorrelationId()).isEqualTo(CORRELATION_ID.toString());
      soft.assertThat(entity.getPartyKey()).isEqualTo(PARTY_KEY);
      soft.assertThat(entity.getProductKey()).isEqualTo(PRODUCT_KEY);
      soft.assertThat(entity.getTenantKey()).isEqualTo(TENANT_KEY);
      soft.assertThat(entity.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
      soft.assertThat(entity.getCreatedDate()).isEqualTo(CAIN_001.getTransactionDate());
      soft.assertThat(entity.getTransactionMessages()).isEmpty();
      soft.assertThat(entity.getAuthCode()).isEqualTo(CAIN_OO2.getAuthCode());
      soft.assertThat(entity.getAuthResponseCode()).isEqualTo(AuthResponseCode._00);
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

  @Test
  void transformsOptionalFields() {

    CardAuth cardAuth = new CardAuth(
        PAN_HASH,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        CAIN_001
            .toBuilder()
            .settlementAmount(null)
            .build(),
        CAIN_OO2
            .toBuilder()
            .updatedBalance(null)
            .build()
    );

    CardTransactionEntity entity = underTest.transform(cardAuth);

    assertSoftly(soft -> {
      soft.assertThat(entity.getSettlementAmount()).isNull();
      soft.assertThat(entity.getSettlementCurrency()).isNull();
      soft.assertThat(entity.getUpdatedBalance()).isNull();
    });

  }

  @Test
  void transformsFee() {

    CardAuth cardAuth = new CardAuth(
        PAN_HASH,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        CAIN_001
            .toBuilder()
            .fee(new Fee(
                UUID.randomUUID(),
                BigDecimal.ONE,
                "desc",
                "GBP",
                "STATUS",
                new Tax(
                    BigDecimal.valueOf(12),
                    "PARENT_TRANSACTION_ID",
                    "TAX_TRANSACTION_ID",
                    "STATEMENT_DESCRIPTION"
                ),
                "CODE",
                "CORRELATION_ID",
                "TRANSACTION_DATE",
                "TRANSACTION_ID",
                "VALUE_DATE_TIME"
            ))
            .build(),
        CAIN_OO2
    );

    CardTransactionEntity entity = underTest.transform(cardAuth);

    assertSoftly(soft -> {
      soft.assertThat(entity.getFee().getId()).isEqualTo(cardAuth.getCain001().getFee().get().getId());
      soft.assertThat(entity.getFee().getAmount()).isEqualTo(BigDecimal.ONE);
      soft.assertThat(entity.getFee().getDescription()).isEqualTo("desc");
      soft.assertThat(entity.getFee().getFeeCurrencyCode()).isEqualTo("GBP");
      soft.assertThat(entity.getFee().getStatus()).isEqualTo("STATUS");
      soft.assertThat(entity.getFee().getTaxAmount()).isEqualTo(BigDecimal.valueOf(12));
      soft.assertThat(entity.getFee().getParentTransactionId()).isEqualTo("PARENT_TRANSACTION_ID");
      soft.assertThat(entity.getFee().getTaxTransactionId()).isEqualTo("TAX_TRANSACTION_ID");
      soft.assertThat(entity.getFee().getStatementDescription()).isEqualTo("STATEMENT_DESCRIPTION");
      soft.assertThat(entity.getFee().getTransactionCode()).isEqualTo("CODE");
      soft.assertThat(entity.getFee().getTransactionCorrelationId()).isEqualTo("CORRELATION_ID");
      soft.assertThat(entity.getFee().getTransactionId()).isEqualTo("TRANSACTION_ID");
      soft.assertThat(entity.getFee().getValueDateTime()).isEqualTo("VALUE_DATE_TIME");
    });

  }

}
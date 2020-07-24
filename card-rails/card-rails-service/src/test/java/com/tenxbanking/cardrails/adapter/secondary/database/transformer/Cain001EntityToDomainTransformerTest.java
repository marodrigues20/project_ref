package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import static com.tenxbanking.cardrails.domain.TestConstant.BANKNET_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.CORRELATION_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.GBP;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.RETRIEVAL_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TRANSACTION_ID;
import static com.tenxbanking.cardrails.domain.model.AuthResponseCode._00;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.CHIP_PIN;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.FeeEntity;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.ReversalAmount;
import com.tenxbanking.cardrails.domain.model.Tax;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Cain001EntityToDomainTransformerTest {

  private Cain001EntityToDomainTransformer underTest;

  @BeforeEach
  void setup() {
    underTest = new Cain001EntityToDomainTransformer();
  }

  @Test
  void transform() {
    CardTransactionEntity entity = entity();

    Cain001 cain001 = underTest.transform(entity);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(cain001.getTransactionAmount()).isEqualTo(Money.of(BigDecimal.valueOf(1), "GBP"));
      soft.assertThat(cain001.getBillingAmount()).isEqualTo(Money.of(BigDecimal.valueOf(2), "GBP"));
      soft.assertThat(cain001.getSettlementAmount()).isEqualTo(Money.of(BigDecimal.valueOf(3), "GBP"));
      soft.assertThat(cain001.getMerchantCategoryCode()).isEqualTo("merchantCategoryCode");
      soft.assertThat(cain001.getTransactionDate()).isEqualTo(entity.getCreatedDate());
      soft.assertThat(cain001.getAccountQualifier()).isEqualTo("accountQualifier");
      soft.assertThat(cain001.getCardId()).isEqualTo(CARD_ID);
      soft.assertThat(cain001.getProcessingCode()).isEqualTo("processingCode");
      soft.assertThat(cain001.getConversionRate()).isEqualTo(BigDecimal.valueOf(4));
      soft.assertThat(cain001.getPointOfServiceEntryMode()).isEqualTo("POSEM");
      soft.assertThat(cain001.getPointOfServiceConditionCode()).isEqualTo("POSCC");
      soft.assertThat(cain001.getFee()).isEmpty();
      soft.assertThat(cain001.getTransactionId()).isEqualTo(TRANSACTION_ID);
      soft.assertThat(cain001.getCorrelationId()).isEqualTo(CORRELATION_ID);
      soft.assertThat(cain001.getBanknetReferenceNumber()).isEqualTo(BANKNET_REFERENCE_NUMBER);
      soft.assertThat(cain001.getCardTransactionType()).isEqualTo(ADVICE);
      soft.assertThat(cain001.getRetrievalReferenceNumber()).isEqualTo(RETRIEVAL_REFERENCE_NUMBER);
      soft.assertThat(cain001.getAuthCode()).isEqualTo("AUTH_CODE");
      soft.assertThat(cain001.getAuthResponseCode()).isEqualTo(_00);
      soft.assertThat(cain001.getPaymentMethodType()).isEqualTo(CHIP_PIN);
      soft.assertThat(cain001.getReversalAmount()).isPresent();

      ReversalAmount reversalAmount = cain001.getReversalAmount().get();

      soft.assertThat(reversalAmount.getBilling().getAmount()).isEqualTo(ONE);
      soft.assertThat(reversalAmount.getBilling().getCurrency()).isEqualTo(GBP);
      soft.assertThat(reversalAmount.getSettlement().getAmount()).isEqualTo(TEN);
      soft.assertThat(reversalAmount.getSettlement().getCurrency()).isEqualTo(GBP);
      soft.assertThat(reversalAmount.getTransaction().getAmount()).isEqualTo(ZERO);
      soft.assertThat(reversalAmount.getTransaction().getCurrency()).isEqualTo(GBP);
    });

  }

  @Test
  void whenCashTransactionTypeAuth_thenDoesNotPopulateAuthCodeOrResponseCode() {
    CardTransactionEntity entity = entity()
        .toBuilder()
        .cardTransactionType(AUTH)
        .build();

    Cain001 cain001 = underTest.transform(entity);

    SoftAssertions.assertSoftly(soft -> {

      soft.assertThat(cain001.getCardTransactionType()).isEqualTo(AUTH);
      soft.assertThat(cain001.getRetrievalReferenceNumber()).isEqualTo(RETRIEVAL_REFERENCE_NUMBER);
      soft.assertThat(cain001.getAuthCode()).isNull();
      soft.assertThat(cain001.getAuthResponseCode()).isNull();
    });
  }


  @Test
  void transformsFee() {
    CardTransactionEntity entity = entity()
        .toBuilder()
        .fee(new FeeEntity(
            UUID.randomUUID(),
            BigDecimal.valueOf(3),
            "desc",
            "feeCurrencyCode",
            "STATUS",
            BigDecimal.valueOf(2),
            "parentTransactionId",
            "taxTransactionId",
            "TAXDESC",
            "FEE_T_CODE",
            "FEE_CORRELATIONID",
            "FEE_T_DATE",
            "feeTransactionId",
            "VALUE_DATE_TIME")
        ).build();

    Cain001 cain001 = underTest.transform(entity);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(cain001.getFee().map(Fee::getId)).contains(entity.getFee().getId());
      soft.assertThat(cain001.getFee().map(Fee::getAmount)).contains(BigDecimal.valueOf(3));
      soft.assertThat(cain001.getFee().map(Fee::getDescription)).contains("desc");
      soft.assertThat(cain001.getFee().map(Fee::getFeeCurrencyCode)).contains("feeCurrencyCode");
      soft.assertThat(cain001.getFee().map(Fee::getStatus)).contains("STATUS");
      soft.assertThat(cain001.getFee().flatMap(Fee::getTax).map(Tax::getTaxAmount)).contains(BigDecimal.valueOf(2));
      soft.assertThat(cain001.getFee().flatMap(Fee::getTax).map(Tax::getParentTransactionId)).contains("parentTransactionId");
      soft.assertThat(cain001.getFee().flatMap(Fee::getTax).map(Tax::getTransactionId)).contains("taxTransactionId");
      soft.assertThat(cain001.getFee().flatMap(Fee::getTax).map(Tax::getStatementDescription)).contains("TAXDESC");
      soft.assertThat(cain001.getFee().map(Fee::getTransactionCode)).contains("FEE_T_CODE");
      soft.assertThat(cain001.getFee().map(Fee::getTransactionCorrelationId)).contains("FEE_CORRELATIONID");
      soft.assertThat(cain001.getFee().map(Fee::getTransactionDate)).contains("FEE_T_DATE");
      soft.assertThat(cain001.getFee().map(Fee::getValueDateTime)).contains("VALUE_DATE_TIME");
    });

  }

  @Test
  void transformsWithNullSettlementAmount() {
    CardTransactionEntity entity = entity()
        .toBuilder()
        .settlementAmount(null)
        .build();

    Cain001 cain001 = underTest.transform(entity);

    assertThat(cain001.getSettlementAmount()).isNull();
  }

  private CardTransactionEntity entity() {
    return new CardTransactionEntity(
        null,
        TRANSACTION_ID.toString(),
        CORRELATION_ID.toString(),
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        Instant.now(),
        List.of(),
        "AUTH_CODE",
        _00,
        CARD_ID,
        BANKNET_REFERENCE_NUMBER,
        ADVICE,
        RETRIEVAL_REFERENCE_NUMBER,
        BigDecimal.valueOf(1),
        "GBP",
        BigDecimal.valueOf(2),
        "GBP",
        BigDecimal.valueOf(3),
        "GBP",
        BigDecimal.valueOf(0),
        "GBP",
        BigDecimal.valueOf(10),
        "GBP",
        BigDecimal.valueOf(1),
        "GBP",
        "merchantCategoryCode",
        "accountQualifier",
        "processingCode",
        BigDecimal.valueOf(4),
        "expiryDate",
        "POSEM",
        "POSCC",
        "networkId",
        "CACC",
        BigDecimal.valueOf(4),
        null,
        true,
        CHIP_PIN
    );
  }
}
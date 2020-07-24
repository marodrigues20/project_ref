package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.FeeEntity;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.ReversalAmount;
import com.tenxbanking.cardrails.domain.model.Tax;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class Cain001EntityToDomainTransformer {

  public Cain001 transform(CardTransactionEntity entity) {
    return new Cain001(
        Money.of(
            entity.getTransactionAmount(),
            entity.getTransactionCurrency()
        ),
        Money.of(
            entity.getBillingAmount(),
            entity.getBillingCurrency()
        ),
        entity.getSettlementAmount() == null
            ? null
            : Money.of(
                entity.getSettlementAmount(),
                entity.getSettlementCurrency()
            ),
        ReversalAmount.of(entity.getReversalTransactionAmount() == null ? null :  Money.of(
                entity.getReversalTransactionAmount(),
                entity.getReversalTransactionCurrency()
            ),
            entity.getReversalBillingAmount() == null ?  null :  Money.of(
                entity.getReversalBillingAmount(),
                entity.getReversalBillingCurrency()
            ),
            entity.getReversalSettlementAmount() == null ? null : Money.of(
                entity.getReversalSettlementAmount(),
                entity.getReversalSettlementCurrency()
            )),
        entity.getMerchantCategoryCode(),
        entity.getCreatedDate(),
        entity.getAccountQualifier(),
        entity.getCardId(),
        entity.getProcessingCode(),
        entity.getConversionRate(),
        entity.getCardExpiryDate(),
        entity.getPointOfServiceEntryMode(),
        entity.getPointOfServiceConditionCode(),
        entity.getNetworkId(),
        entity.getCardAcceptorCountryCode(),
        Optional.ofNullable(entity.getFee())
            .map(this::transform)
            .orElse(null),
        UUID.fromString(entity.getTransactionId()),
        UUID.fromString(entity.getCorrelationId()),
        entity.getBanknetReferenceNumber(),
        entity.getCardTransactionType(),
        entity.getRetrievalReferenceNumber(),
        AUTH == entity.getCardTransactionType() ? null : entity.getAuthCode(),
        AUTH == entity.getCardTransactionType() ? null : entity.getAuthResponseCode(),
        entity.getPaymentMethodType()
    );
  }

  private Fee transform(FeeEntity entity) {
    return new Fee(
        entity.getId(),
        entity.getAmount(),
        entity.getDescription(),
        entity.getFeeCurrencyCode(),
        entity.getStatus(),
        new Tax(
            entity.getTaxAmount(),
            entity.getParentTransactionId(),
            entity.getTaxTransactionId(),
            entity.getStatementDescription()
        ),
        entity.getTransactionCode(),
        entity.getTransactionCorrelationId(),
        entity.getTransactionDate(),
        entity.getTransactionId(),
        entity.getValueDateTime()
    );
  }

}

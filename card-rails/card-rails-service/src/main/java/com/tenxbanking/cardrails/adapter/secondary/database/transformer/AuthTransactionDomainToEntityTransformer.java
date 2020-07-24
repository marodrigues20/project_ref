package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.FeeEntity;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthTransactionDomainToEntityTransformer {

  public CardTransactionEntity transform(AuthTransaction authTransaction) {

    Cain001 cain001 = authTransaction.getCain001();
    Cain002 cain002 = authTransaction.getCain002();

    return new CardTransactionEntity(
        null,
        cain001.getTransactionId().toString(),
        cain001.getCorrelationId().toString(),
        authTransaction.getSubscriptionKey(),
        authTransaction.getPartyKey(),
        authTransaction.getProductKey(),
        authTransaction.getTenantKey(),
        authTransaction.getCain001().getTransactionDate(),
        List.of(),
        authTransaction.getAuthCode().orElse(null),
        cain002.getAuthResponseCode(),
        cain001.getCardId(),
        cain001.getBanknetReferenceNumber(),
        cain001.getCardTransactionType(),
        cain001.getRetrievalReferenceNumber(),
        cain001.getTransactionAmount().getAmount(),
        cain001.getTransactionAmount().getCurrencyCode(),
        cain001.getBillingAmount().getAmount(),
        cain001.getBillingAmount().getCurrencyCode(),
        Optional.ofNullable(cain001.getSettlementAmount()).map(Money::getAmount).orElse(null),
        Optional.ofNullable(cain001.getSettlementAmount()).map(Money::getCurrencyCode).orElse(null),
        cain001.getReversalAmount().map(map -> map.getTransaction().getAmount()).orElse(null),
        cain001.getReversalAmount().map(map -> String.valueOf(map.getTransaction().getCurrency().getNumericCode())).orElse(null),
        cain001.isThereSettlement() ? cain001.getReversalAmount().map(map -> map.getSettlement().getAmount()).orElse(null) : null,
        cain001.isThereSettlement() ? cain001.getReversalAmount().map(map -> String.valueOf(map.getSettlement().getCurrency().getNumericCode())).orElse(null) : null,
        cain001.getReversalAmount().map(map -> map.getBilling().getAmount()).orElse(null),
        cain001.getReversalAmount().map(map -> String.valueOf(map.getBilling().getCurrency().getNumericCode())).orElse(null),
        cain001.getMerchantCategoryCode(),
        cain001.getAccountQualifier(),
        cain001.getProcessingCode(),
        cain001.getConversionRate(),
        cain001.getCardExpiryDate(),
        cain001.getPointOfServiceEntryMode(),
        cain001.getPointOfServiceConditionCode(),
        cain001.getNetworkId(),
        cain001.getCardAcceptorCountryCode(),
        Optional.ofNullable(cain002.getUpdatedBalance()).map(Money::getAmount).orElse(null),
        cain001.getFee()
            .map(f -> new FeeEntity(
                f.getId(),
                f.getAmount(),
                f.getDescription(),
                f.getFeeCurrencyCode(),
                f.getStatus(),
                f.getTax().map(Tax::getTaxAmount).orElse(null),
                f.getTax().map(Tax::getParentTransactionId).orElse(null),
                f.getTax().map(Tax::getTransactionId).orElse(null),
                f.getTax().map(Tax::getStatementDescription).orElse(null),
                f.getTransactionCode(),
                f.getTransactionCorrelationId(),
                f.getTransactionDate(),
                f.getTransactionId(),
                f.getValueDateTime()
            )).orElse(null),
        cain002.isSuccess(),
        cain001.getPaymentMethodType()
    );
  }

}

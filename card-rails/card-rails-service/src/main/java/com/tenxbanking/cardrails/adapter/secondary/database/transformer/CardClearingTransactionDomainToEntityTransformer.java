package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.FeeEntity;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CardClearingTransactionDomainToEntityTransformer {

  public CardTransactionEntity transform(CardClearing cardClearing) {

    return CardTransactionEntity.builder()
        .banknetReferenceNumber(cardClearing.getCain003().getBanknetReferenceNumber())
        .transactionId(cardClearing.getCain003().getTransactionId().toString())
        .correlationId(cardClearing.getCain003().getCorrelationId().toString())
        .subscriptionKey(cardClearing.getSubscriptionKey())
        .partyKey(cardClearing.getPartyKey())
        .productKey(cardClearing.getProductKey())
        .tenantKey(cardClearing.getTenantKey())
        .transactionMessages(List.of())
        .retrievalReferenceNumber(cardClearing.getCain003().getRetrievalReferenceNumber())
        .transactionCurrency(cardClearing.getTransactionAmount().getCurrency().getCurrencyCode())
        .billingAmount(cardClearing.getCain003().getBillingAmount().getAmount())
        .billingCurrency(cardClearing.getCain003().getBillingAmount().getCurrency().getCurrencyCode())
        .settlementAmount(cardClearing.getCain003().getSettlementAmount().getAmount())
        .settlementCurrency(cardClearing.getCain003().getCurrency().getCurrencyCode())
        .accountQualifier(cardClearing.getCain003().getAccountQualifier())
        .cardExpiryDate(cardClearing.getCain003().getCardExpiryDate())
        .pointOfServiceConditionCode(cardClearing.getCain003().getPointOfServiceConditionCode())
        .networkId(cardClearing.getCain003().getNetworkId())
        .cardAcceptorCountryCode(cardClearing.getCain003().getCardAcceptorCountryCode())
        .updatedBalance(Optional.ofNullable(cardClearing.getCain003().getUpdatedBalance()).map(Money::getAmount).orElse(null))
        .isSuccess(true)
        .paymentMethodType(cardClearing.getPaymentMethodType())
        .processingCode(cardClearing.getCain003().getProcessingCode())
        .conversionRate(Optional.ofNullable(cardClearing.getCain003().getConversionRate()).orElse(null))
        .authCode(Optional.ofNullable(cardClearing.getCain003()).map(Cain003::getAuthCode).orElse(null))
        .cardId(cardClearing.getCain003().getCardId())
        .cardTransactionType(cardClearing.getCain003().getCardTransactionType())
        .pointOfServiceEntryMode(cardClearing.getCain003().getPointOfServiceEntryMode())
        .fee(createFeeToBePersisted(cardClearing.getCain003()))
        .transactionAmount(cardClearing.getCain003().getTransactionAmount().getAmount())
        .createdDate(cardClearing.getCain003().getTransactionDate())
        .merchantCategoryCode(cardClearing.getCain003().getMerchantCategoryCode()).build();


  }

  private FeeEntity createFeeToBePersisted(@NonNull final Cain003 cain003) {
    return cain003.getFee()
        .map(fee -> new FeeEntity(
            fee.getId(),
            fee.getAmount(),
            fee.getDescription(),
            fee.getFeeCurrencyCode(),
            fee.getStatus(),
            fee.getTax().map(Tax::getTaxAmount).orElse(null),
            fee.getTax().map(Tax::getParentTransactionId).orElse(null),
            fee.getTax().map(Tax::getTransactionId).orElse(null),
            fee.getTax().map(Tax::getStatementDescription).orElse(null),
            fee.getTransactionCode(),
            fee.getTransactionCorrelationId(),
            fee.getTransactionDate(),
            fee.getTransactionId(),
            fee.getValueDateTime()
        )).orElse(null);
  }

}

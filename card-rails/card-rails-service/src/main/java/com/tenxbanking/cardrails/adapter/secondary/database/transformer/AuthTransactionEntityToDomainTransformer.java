package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAdvice;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuthReversal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthTransactionEntityToDomainTransformer {

  private final Cain001EntityToDomainTransformer cain001Transformer;
  private final Cain002EntityToDomainTransformer cain002Transformer;

  @Autowired
  public AuthTransactionEntityToDomainTransformer(
      Cain001EntityToDomainTransformer cain001Transformer,
      Cain002EntityToDomainTransformer cain002Transformer) {
    this.cain001Transformer = cain001Transformer;
    this.cain002Transformer = cain002Transformer;
  }

  public AuthTransaction transform(CardTransactionEntity entity) {

    switch (entity.getCardTransactionType()) {
      case AUTH:
        return transformAuth(entity);
      case ADVICE:
        return transformAdvice(entity);
      case REVERSAL:
        return transformReversal(entity);
      default:
        throw new IllegalArgumentException(String.format("No AuthTransaction transformation implemented for cardTransactionType=%s", entity.getCardTransactionType()));
    }
  }

  private AuthTransaction transformAuth(CardTransactionEntity entity) {
    return new CardAuth(
        entity.getCardId(),
        entity.getSubscriptionKey(),
        entity.getPartyKey(),
        entity.getProductKey(),
        entity.getTenantKey(),
        cain001Transformer.transform(entity),
        cain002Transformer.transform(entity)
    );
  }

  private AuthTransaction transformAdvice(CardTransactionEntity entity) {
    return new CardAdvice(
        entity.getCardId(),
        entity.getSubscriptionKey(),
        entity.getPartyKey(),
        entity.getProductKey(),
        entity.getTenantKey(),
        cain001Transformer.transform(entity),
        cain002Transformer.transform(entity)
    );
  }

  private AuthTransaction transformReversal(CardTransactionEntity entity) {
    return new CardAuthReversal(
        entity.getCardId(),
        entity.getSubscriptionKey(),
        entity.getPartyKey(),
        entity.getProductKey(),
        entity.getTenantKey(),
        cain001Transformer.transform(entity),
        cain002Transformer.transform(entity)
    );
  }

}

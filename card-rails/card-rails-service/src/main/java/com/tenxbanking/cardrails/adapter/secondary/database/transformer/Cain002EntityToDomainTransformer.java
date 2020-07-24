package com.tenxbanking.cardrails.adapter.secondary.database.transformer;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class Cain002EntityToDomainTransformer {

  public Cain002 transform(CardTransactionEntity entity) {
    return new Cain002(
        entity.getAuthCode(),
        !entity.isSuccess()
            ? null
            : Money.of(
                entity.getUpdatedBalance(),
                entity.getTransactionCurrency()),
        entity.getAuthResponseCode(),
        entity.isSuccess()
    );
  }

}

package com.tenxbanking.cardrails.domain.port.store;

import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import lombok.NonNull;

public interface CardClearingTransactionStore extends CardTransactionStore<CardClearing> {

  void save(@NonNull final CardClearing cardClearing);
}



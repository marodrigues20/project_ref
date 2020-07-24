package com.tenxbanking.cardrails.domain.port.store;

import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import lombok.NonNull;

public interface CardTransactionStore<T extends CardTransaction> {

  void save(@NonNull final T cardTransaction);

}

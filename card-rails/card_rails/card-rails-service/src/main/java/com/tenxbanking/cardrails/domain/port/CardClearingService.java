package com.tenxbanking.cardrails.domain.port;

import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.card.Transaction;
import lombok.NonNull;

public interface CardClearingService {

  public void process(@NonNull final Transaction transaction);



}

package com.tenxbanking.cardrails.domain.service.handler;

import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;

public interface CardHandler {

  CardTransactionType handlesCardTransactionType();

  Cain002 auth(Cain001 cain001);

}

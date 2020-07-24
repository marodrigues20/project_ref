package com.tenxbanking.cardrails.domain.port.sender;

import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAdvice;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import lombok.NonNull;

public interface AuthReserveTransactionSender {

  Cain002 reserve(@NonNull AuthTransaction cardAuth);

}

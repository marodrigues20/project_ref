package com.tenxbanking.cardrails.domain.port.sender;

import com.tenxbanking.cardrails.domain.exception.CardAuthReservationException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuthReversal;
import java.util.UUID;
import lombok.NonNull;

public interface AuthReversalTransactionSender {

  Cain002 reverse(@NonNull CardAuthReversal cardAuthReversal) throws CardAuthReservationException;

}

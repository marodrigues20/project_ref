package com.tenxbanking.cardrails.data;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.ReserveResponse;
import java.math.BigDecimal;

public class LedgerManagerDataFactory {

  public static TransactionMessage getTransactionMessage() {
    return new TransactionMessage();
  }

  public static ReserveResponse getReserveResponse() {

    return ReserveResponse.builder()
        .availableBalance(BigDecimal.TEN)
        .result(true)
        .build();
  }
}

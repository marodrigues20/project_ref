package com.tenxbanking.cardrails.adapter.secondary.ledger;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.BalanceResponse;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.ReserveResponse;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${downstream.ledgermanager.host}", name = "ledgerManagerClient")
public interface LedgerManagerClient {

  @PostMapping(value = "/ledger-manager/v2/reserve",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<ReserveResponse> reserve(@NonNull final TransactionMessage transactionMessage);

  @PostMapping(value = "/ledger-manager/v1/processPayments",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity confirmReservation(@NonNull final TransactionMessage transactionMessage);

  @PostMapping(value = "/ledger-manager/v1/authorisations/reversal",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<BalanceResponse> reversal(@NonNull final TransactionMessage transactionMessage);

}

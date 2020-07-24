package com.tenx.universalbanking.transactionmanager.rest.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tenx.universalbanking.transactionmanager.rest.responses.dcm.GetCardResponse;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "debitCardManagerClient", url = "${downstream.debit-card-manager.rest.url}")
public interface DebitCardManagerClient {

  @GetMapping(value = "/v1/debit-cards/{cardId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<GetCardResponse> getCardById(@PathVariable("cardId") @NonNull final String cardId);
}

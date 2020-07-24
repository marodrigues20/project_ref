package com.tenxbanking.cardrails.adapter.secondary.cards;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "debitCardManagerClient", url = "${downstream.debitcardmanager.host}")
public interface DebitCardManagerClient {

  @PostMapping(value = "/v1/debit-cards/by-pan-hash",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<GetCardResponse> getCardByHash(@NonNull final GetCardRequest request);

  @GetMapping(value = "/v1/debit-cards/{panHash}/settings",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<DebitCardSettingsResponse> getCardSettings(@PathVariable(value = "panHash") String panHash);

}

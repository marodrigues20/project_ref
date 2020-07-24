package com.tenxbanking.cardrails.adapter.secondary.fees;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeResponse;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeTransactionRequest;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${downstream.feeschargescalculator.host}", name = "feesAndChargesClient")
public interface FeesAndChargesClient {

  @PostMapping(value = "/fees-charges-calculator/v2/transaction",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<FeeResponse> postTransaction(@NonNull final FeeTransactionRequest requestBody);
}

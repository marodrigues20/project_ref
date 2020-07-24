package com.tenxbanking.cardrails.adapter.secondary.fees;

import static java.util.Optional.empty;

import com.tenxbanking.cardrails.adapter.primary.rest.mapper.TransactionCodeMapper;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeResponse;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeTransactionRequest;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.Tax;
import com.tenxbanking.cardrails.domain.port.FeesCheckerService;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FeesAndChargesManager implements FeesCheckerService {

  private final FeesAndChargesClient feesAndChargesClient;
  private final TransactionCodeMapper codeMapper;
  private final TransactionCodeMapper transactionCodeMapper;
  private final TimeService timeService;

  public Optional<Fee> check(@NonNull final Cain001 cain001,
      @NonNull final UUID subscriptionKey) {

    FeeTransactionRequest request = FeeTransactionRequest.builder()
        .subscriptionKey(subscriptionKey.toString())
        .transactionType(cain001.getProcessingCode())
        .transactionCode(codeMapper
            .getTransactionCode(cain001.getPaymentMethodType()))
        .transactionAmount(cain001.getTransactionAmount().getAmount())
        .merchantCategoryCode(cain001.getMerchantCategoryCode())
        .transactionId(cain001.getTransactionId().toString())
        .transactionCorrelationId(cain001.getCorrelationId().toString())
        .currency(cain001.getCurrencyCode())
        .transactionDate(timeService.fromInstant(cain001.getTransactionDate()))
        .amountQualifier(cain001.getAccountQualifier())
        .build();

    ResponseEntity<FeeResponse> feeResponseResponseEntity = feesAndChargesClient
        .postTransaction(request);

    return handleResponse(feeResponseResponseEntity);
  }


  public Optional<Fee> check(@NonNull final Cain003 cain003, @NonNull final UUID subscriptionKey) {
    FeeTransactionRequest request = FeeTransactionRequest.builder()
        .subscriptionKey(subscriptionKey.toString())
        .transactionType(cain003.getProcessingCode())
        .transactionCode(transactionCodeMapper
            .getTransactionCode(cain003.getPaymentMethodType()))
        .transactionAmount(cain003.getTransactionAmount().getAmount())
        .merchantCategoryCode(cain003.getMerchantCategoryCode())
        .transactionId(cain003.getTransactionId().toString())
        .transactionCorrelationId(cain003.getCorrelationId().toString())
        .currency(cain003.getCurrencyCode())
        .transactionDate(timeService.fromInstant(cain003.getTransactionDate()))
        .amountQualifier(cain003.getAccountQualifier())
        .build();

    ResponseEntity<FeeResponse> feeResponseResponseEntity = feesAndChargesClient
        .postTransaction(request);

    return handleResponse(feeResponseResponseEntity);

  }


  private Optional<Fee> handleResponse(ResponseEntity<FeeResponse> responseEntity) {
    return responseEntity.getStatusCode() == HttpStatus.CREATED
        ? Optional.of(toFee(responseEntity.getBody()))
        : empty();
  }

  private Fee toFee(FeeResponse feeResponse) {
    return Fee.builder()
        .amount(feeResponse.getAmount())
        .description(feeResponse.getDescription())
        .feeCurrencyCode(feeResponse.getFeeCurrencyCode())
        .status(feeResponse.getStatus())
        .tax(Tax.builder().taxAmount(feeResponse.getTaxAmount()).build())
        .transactionCode(feeResponse.getTransactionCode())
        .transactionCorrelationId(feeResponse.getTransactionCorrelationId())
        .transactionDate(feeResponse.getTransactionDate())
        .transactionId(feeResponse.getTransactionId())
        .valueDateTime(feeResponse.getValueDateTime())
        .build();
  }

}

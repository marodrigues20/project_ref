package com.tenxbanking.cardrails.data;

import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeResponse;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeTransactionRequest;
import java.math.BigDecimal;
import java.util.UUID;

public class FeesAndChargesDataFactory {

  public static FeeResponse getFeeResponse(UUID subscriptionKey) {

    return FeeResponse.builder()
        .amount(BigDecimal.ONE)
        .description("description")
        .feeCurrencyCode("GBP")
        .parentTransactionId("parentTransactionId")
        .status("RESERVED")
        .subscriptionKey(subscriptionKey.toString())
        .taxAmount(BigDecimal.TEN)
        .transactionCode("transactionCode")
        .transactionCorrelationId("transactionCorrelationId")
        .transactionDate("2018-08-19T10:12:14.000+0000")
        .transactionId("transactionId")
        .valueDateTime("2018-08-19T10:12:14.000+0000")
        .build();
  }

  public static FeeTransactionRequest getFeeTransactionRequest(UUID subscriptionKey) {

    return FeeTransactionRequest.builder()
        .subscriptionKey(subscriptionKey.toString())
        .amountQualifier("amountQualifier")
        .transactionType("transactionType")
        .transactionDate("2018-08-19T10:12:14.000+0000")
        .currency("GBP")
        .transactionCorrelationId("correlationId")
        .transactionId("transactionId")
        .authenticationMethod("authenticationMethod")
        .merchantCategoryCode("merchantCategoryCode")
        .transactionAmount(BigDecimal.TEN)
        .transactionCode("transactionCode")
        .build();
  }
}

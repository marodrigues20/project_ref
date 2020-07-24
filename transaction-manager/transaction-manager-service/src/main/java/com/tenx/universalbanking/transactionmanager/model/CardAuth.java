package com.tenx.universalbanking.transactionmanager.model;

import com.tenx.universalbanking.transactionmanager.rest.request.turbine.CreditDebitEnum;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CardAuth {

  @NonNull
  String cardId;
  @NonNull
  BigDecimal totalAmount;
  @NonNull
  BigDecimal amount;
  @NonNull
  String transactionType;
  @NonNull
  String transactionCurrencyCode;
  @NonNull
  String merchantCategoryCode;
  String merchantCountryCode;
  String merchantName;
  @NonNull
  String cardDataEntryMode;
  @NonNull
  String cardConditionCode;
  @NonNull
  Instant transactionDatetime;
  @NonNull
  LocalDate transactionDate;
  @NonNull
  String transactionTime;
  BigDecimal exchangeRate;
  @NonNull
  CreditDebitEnum creditDebit;
  String banknetReference;
  BigDecimal feeAmount;
  @NonNull
  BigDecimal conversionRate;
  String systemTraceNumber;
  String networkCode;
  String cardAcceptorIdCode;

}

package com.tenx.universalbanking.transactionmanager.enums.errormapping;

import com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public enum PdfErrorMapping {

  DECISION_INSUFFICIENT_BALANCE(8002, TransactionManagerExceptionCodes.DECISION_INSUFFICIENT_FUNDS, HttpStatus.OK),
  DECISION_PAYER_ACCOUNT_INACTIVE(8019, TransactionManagerExceptionCodes.DECISION_PAYER_ACCOUNT_INACTIVE, HttpStatus.OK),
  DECISION_PAYMENT_LIMIT_EXCEED(8020, TransactionManagerExceptionCodes.DECISION_PAYMENT_LIMIT_EXCEED, HttpStatus.OK),
  DECISION_PAYEE_ACCOUNT_INACTIVE(8021, TransactionManagerExceptionCodes.DECISION_PAYEE_ACCOUNT_INACTIVE, HttpStatus.OK),
  DECISION_POOL_TRANSFER_ACCOUNT_INACTIVE(8022, TransactionManagerExceptionCodes.DECISION_POOL_TRANSFER_ACCOUNT_INACTIVE, HttpStatus.OK),
  DECISION_CARD_AUTH_PAYMENT_LIMIT_EXCEED(8024, TransactionManagerExceptionCodes.DECISION_CARD_AUTH_PAYMENT_LIMIT_EXCEED, HttpStatus.OK),
  DECISION_CARD_AUTH_ACCOUNT_INACTIVE(8023, TransactionManagerExceptionCodes.DECISION_CARD_AUTH_ACCOUNT_INACTIVE, HttpStatus.OK),
  DECISION_FUND_ACCOUNT_INACTIVE(8025, TransactionManagerExceptionCodes.DECISION_FUND_ACCOUNT_INACTIVE, HttpStatus.OK),
  DECISION_FUND_LIMIT_EXCEED(8026, TransactionManagerExceptionCodes.DECISION_FUND_LIMIT_EXCEED, HttpStatus.OK),
  DECISION_FPS_PAYMENT_LESS_THAN_ZERO(8027, TransactionManagerExceptionCodes.DECISION_FPS_PAYMENT_LESS_THAN_ZERO, HttpStatus.OK),
  DECISION_FPS_PAYMENT_GREATER_THAN_LIMIT(8028, TransactionManagerExceptionCodes.DECISION_FPS_PAYMENT_GREATER_THAN_LIMIT, HttpStatus.OK);

  private final int downstreamErrorCode;
  private final int transactionManagerErrorCode;
  private final HttpStatus status;

  PdfErrorMapping(int downstreamErrorCode, int transactionManagerErrorCode,
      HttpStatus status) {
    this.downstreamErrorCode = downstreamErrorCode;
    this.transactionManagerErrorCode = transactionManagerErrorCode;
    this.status = status;
  }

  public static PdfErrorMapping getEnum(int downstreamErrorCode) {

    Optional<PdfErrorMapping> errorMapping = Arrays.stream(PdfErrorMapping.values())
        .filter(emap -> emap.getDownstreamErrorCode() == downstreamErrorCode)
        .findAny();

    return errorMapping.orElse(null);

  }

  private int getDownstreamErrorCode() {
    return downstreamErrorCode;
  }

  public int getTransactionManagerErrorCode() {
    return transactionManagerErrorCode;
  }

  public HttpStatus getStatus() {
    return status;
  }
}

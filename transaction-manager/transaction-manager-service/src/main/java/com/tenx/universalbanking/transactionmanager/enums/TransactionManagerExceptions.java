package com.tenx.universalbanking.transactionmanager.enums;

import com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes;

public enum TransactionManagerExceptions {
  INTERNAL_SERVER_ERROR_FOR_BACS(TransactionManagerExceptionCodes.INTERNAL_SERVER_ERROR_NEW,"Transaction Status Not Found"),
  SUBSCRIPTION_KEY_NOTFOUND(TransactionManagerExceptionCodes.SUBSCRIPTION_KEY_CODE_NOT_FOUND, "Subscription key not found"),
  SUBSCRIPTION_STATUS_NOTFOUND(TransactionManagerExceptionCodes.SUBSCRIPTION_STATUS_NOT_FOUND, "Subscription status not found"),
  NEGATIVE_TRANSACTION_AMOUNT(TransactionManagerExceptionCodes.INVALID_MESSAGE_CODE,"Total amount must be greater than or equal to zero"),
  INTERNAL_SERVICE_UNAVAILABLE(TransactionManagerExceptionCodes.INTERNAL_SERVICE_UNAVAILABLE,"Internal Service unavailable, please retry after 5 mins"),
  INTERNAL_SERVER_ERROR_PDF_FAILURE(TransactionManagerExceptionCodes.INTERNAL_SERVER_ERROR_PDF_FAILURE,"Server error, request Timed out. Please retry after 4 secs"),
  INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE(TransactionManagerExceptionCodes.INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE,"Server error, request Timed out. Please retry after 4 secs"),
  INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE(TransactionManagerExceptionCodes.INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE,"Server error, request Timed out. Please retry after 4 secs");
  private final int statusCode;
  private final String message;

  TransactionManagerExceptions(int statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public String getMessage() {
    return this.message;
  }
}

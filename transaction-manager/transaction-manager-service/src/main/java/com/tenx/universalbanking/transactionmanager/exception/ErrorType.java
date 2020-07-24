package com.tenx.universalbanking.transactionmanager.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.http.HttpStatus;

public enum ErrorType {

  KAFKA_CONNECTION_TIMEOUT_EXP(1503, "Connection to Kafka Topic timed out", INTERNAL_SERVER_ERROR),
  TM_UNKNOWN_SETTLEMENT_EXP(1500, "An unknown error occurred in TM for Settlement request.", INTERNAL_SERVER_ERROR);

  private final HttpStatus httpStatus;
  private final Integer code;
  private final String message;

  ErrorType(int statusCode, String message, HttpStatus httpStatus) {
    this.httpStatus = httpStatus;
    this.code = statusCode;
    this.message = message;
  }

  public Integer getCode() {
    return this.code;
  }

  public String getMessage() {
    return this.message;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

}

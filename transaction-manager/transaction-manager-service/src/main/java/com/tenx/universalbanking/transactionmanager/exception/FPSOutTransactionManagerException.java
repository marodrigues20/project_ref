package com.tenx.universalbanking.transactionmanager.exception;

import org.springframework.http.HttpStatus;

public class FPSOutTransactionManagerException extends RuntimeException {

  private Integer errorCode;
  private String errorMessage;
  private String fieldName;
  private HttpStatus httpStatus;

  public Integer getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(HttpStatus httpStatus) {
    this.httpStatus = httpStatus;
  }
}

package com.tenx.universalbanking.transactionmanager.exception;

import java.util.Objects;

public class TMKafkaException extends RuntimeException {


  private final ErrorType errorType;

  private final String errorMessage;

  public TMKafkaException(ErrorType errorType, String errorMessage){
    super("errorType="+errorType+", errorMessage="+errorMessage);
    this.errorType = errorType;
    this.errorMessage = errorMessage;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    return "TMKafkaException{" +
        "errorType=" + errorType +
        ", errorMessage='" + errorMessage + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TMKafkaException that = (TMKafkaException) o;
    return errorType == that.errorType &&
        Objects.equals(errorMessage, that.errorMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorType, errorMessage);
  }
}

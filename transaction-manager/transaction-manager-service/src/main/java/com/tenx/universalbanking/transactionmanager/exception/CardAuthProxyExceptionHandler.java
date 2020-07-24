package com.tenx.universalbanking.transactionmanager.exception;

import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.ErrorResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@AllArgsConstructor
public class CardAuthProxyExceptionHandler {

  @ExceptionHandler(RequestValidationFailureException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleRequestValidationFailureException(RequestValidationFailureException e) {
    return new ErrorResponse(e.getMessage());
  }

  @ExceptionHandler(CardNotFoundException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ErrorResponse handleCardNotFound(CardNotFoundException e) {
    return new ErrorResponse(e.getMessage());
  }

}

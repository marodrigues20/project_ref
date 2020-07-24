package com.tenx.universalbanking.transactionmanager.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.ErrorResponse;
import org.junit.Test;

public class CardAuthProxyExceptionHandlerTest {

  private final CardAuthProxyExceptionHandler handler = new CardAuthProxyExceptionHandler();

  @Test
  public void shouldReturnErrorValidationFailureException() {
    ErrorResponse test = handler
        .handleRequestValidationFailureException(new RequestValidationFailureException("test"));
    assertThat(test.getMessage()).isEqualTo("test");
  }

  @Test
  public void shouldReturnErrorCardNotFoundException() {
    ErrorResponse test = handler.handleCardNotFound(new CardNotFoundException("test"));
    assertThat(test.getMessage()).isEqualTo("test");
  }

}
package com.tenxbanking.cardrails.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationExceptionTest {

  @Test
  void getMessage() {
    ValidationException validationException = new ValidationException(List.of(ValidationFailure.of("a failure"), ValidationFailure.of("another failure")));
    assertThat(validationException.getMessage()).isEqualTo("[a failure, another failure]");
  }

}
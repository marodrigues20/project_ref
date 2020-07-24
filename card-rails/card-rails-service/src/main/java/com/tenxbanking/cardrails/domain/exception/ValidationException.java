package com.tenxbanking.cardrails.domain.exception;

import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

  private final List<ValidationFailure> failures;

  public ValidationException(List<ValidationFailure> failures) {
    super(failures.stream().map(ValidationFailure::getMessage).collect(Collectors.joining(", ", "[", "]")));
    this.failures = failures;
  }
}

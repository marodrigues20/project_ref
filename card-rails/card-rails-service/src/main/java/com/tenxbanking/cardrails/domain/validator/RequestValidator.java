package com.tenxbanking.cardrails.domain.validator;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.domain.exception.ValidationException;
import com.tenxbanking.cardrails.domain.validator.rule.RequestValidationRule;
import com.tenxbanking.cardrails.domain.validator.rule.ValidationRule;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequestValidator {

  private final List<RequestValidationRule> rules;

  @Autowired
  public RequestValidator(List<RequestValidationRule> rules) {
    this.rules = rules;
  }

  public void validate(SchemeMessage schemeMessage) {
    List<ValidationFailure> failures = rules
        .stream()
        .filter(rule -> rule.getApplicableRequest().contains(
            ValidationRule.REVERSAL))
        .map(rule -> rule.validate(schemeMessage))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    if (!failures.isEmpty()) {
      throw new ValidationException(failures);
    }
  }

}

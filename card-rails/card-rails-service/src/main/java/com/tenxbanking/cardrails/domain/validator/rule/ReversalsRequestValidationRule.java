package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.REVERSAL;
import static java.util.Optional.of;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ReversalsRequestValidationRule implements RequestValidationRule {

  @Override
  public ValidationRule getRule() {
    return REVERSAL;
  }

  @Override
  public List<ValidationRule> getApplicableRequest() {
    return List.of(ValidationRule.REVERSAL);
  }

  @Override
  public Optional<ValidationFailure> validate(SchemeMessage schemeMessage) {
    if (schemeMessage.getReversalAmounts() == null) {
      return of(ValidationFailure.of("No Reversal Amount is defined"));
    }
    if (schemeMessage.getReversalAmounts().getBilling() == null) {
      return of(ValidationFailure.of("No Billing Amount defined in Reversal Amount"));
    }
    //if (schemeMessage.getReversalAmounts().getTransaction() == null) {
    //  return of(ValidationFailure.of("No Transaction Amount defined in Reversal Amount"));
    //}
    //TODO - validate settlement being null if we know from message why it should exist
    return Optional.empty();
  }
}

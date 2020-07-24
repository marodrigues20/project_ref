package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.REVERSAL;
import static org.assertj.core.api.Assertions.assertThat;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.ReversalAmounts;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReversalsRequestValidationRuleTest {

  private ReversalsRequestValidationRule rule = new ReversalsRequestValidationRule();

  @Test
  void shouldReturnRuleReversal() {
    assertThat(rule.getRule()).isEqualTo(REVERSAL);
  }

  @Test
  void shouldReturnListOfApplicableRequests() {
    assertThat(rule.getApplicableRequest()).size().isOne();
    assertThat(rule.getApplicableRequest().get(0)).isEqualTo(ValidationRule.REVERSAL);
  }

  @Test
  void shouldReturnErrorWhenReversalAmountsIsNull() {
    SchemeMessage message = new SchemeMessage();

    Optional<ValidationFailure> validate = rule.validate(message);

    assertThat(validate).isPresent();
    assertThat(validate.get().getMessage()).isEqualTo("No Reversal Amount is defined");
  }

  @Test
  void shouldReturnErrorWhenReversalAmountsHasNoBillingAmount() {
    SchemeMessage message = new SchemeMessage();
    message.setReversalAmounts(new ReversalAmounts(BigDecimal.ONE,BigDecimal.TEN, null));

    Optional<ValidationFailure> validate = rule.validate(message);

    assertThat(validate).isPresent();
    assertThat(validate.get().getMessage()).isEqualTo("No Billing Amount defined in Reversal Amount");
  }

  @Test
  void shouldReturnErrorWhenReversalAmountsHasNoTransactionAmount() {
    SchemeMessage message = new SchemeMessage();
    message.setReversalAmounts(new ReversalAmounts(null,BigDecimal.TEN, BigDecimal.ONE));

    Optional<ValidationFailure> validate = rule.validate(message);

    assertThat(validate).isPresent();
    assertThat(validate.get().getMessage()).isEqualTo("No Transaction Amount defined in Reversal Amount");
  }

  @Test
  void shouldEmptyWhenReversalAmountsIsComplete() {
    SchemeMessage message = new SchemeMessage();
    message.setReversalAmounts(new ReversalAmounts(BigDecimal.TEN,BigDecimal.TEN, BigDecimal.ONE));

    Optional<ValidationFailure> validate = rule.validate(message);

    assertThat(validate).isEmpty();
  }

}
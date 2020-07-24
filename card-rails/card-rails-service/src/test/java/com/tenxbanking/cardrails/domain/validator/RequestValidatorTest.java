package com.tenxbanking.cardrails.domain.validator;

import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.REVERSAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.primary.rest.model.request.ReversalAmounts;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.SchemeMessage;
import com.tenxbanking.cardrails.domain.exception.ValidationException;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.ReversalAmount;
import com.tenxbanking.cardrails.domain.validator.rule.ReversalsRequestValidationRule;
import com.tenxbanking.cardrails.domain.validator.rule.ValidationRule;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RequestValidatorTest {

  @InjectMocks
  private ReversalsRequestValidationRule reversalsValidationRule;

  @Mock
  private SchemeMessage schemeMessage;

  @Mock
  private ReversalAmounts reversalAmounts;

  private BigDecimal anAmount = new BigDecimal(1);

  private RequestValidator testValidator;

  @BeforeEach
  void setup() {
    testValidator = new RequestValidator(List.of(reversalsValidationRule));
  }


  @Test
  public void testValidatorAndValidationMessages() {
    Optional<ValidationFailure> validationFailure = reversalsValidationRule.validate(schemeMessage);

    assertThat(validationFailure).isNotEmpty();
    assertThat(validationFailure).contains(ValidationFailure.of("No Reversal Amount is defined"));

    when(schemeMessage.getReversalAmounts()).thenReturn(ReversalAmounts.builder()
        .settlement(BigDecimal.valueOf(1))
        //.transaction(BigDecimal.valueOf(1))
        .billing(BigDecimal.valueOf(1)).build());

    validationFailure = reversalsValidationRule.validate(schemeMessage);
    assertThat(validationFailure).isEmpty();

    when(schemeMessage.getReversalAmounts()).thenReturn(ReversalAmounts.builder()
        .billing(null)
        .transaction(null)
        .settlement(null)
        .build());

    validationFailure = reversalsValidationRule.validate(schemeMessage);
    assertThat(validationFailure).isNotEmpty();
    assertThat(validationFailure).contains(ValidationFailure.of("No Billing Amount defined in Reversal Amount"));

  }

  @Test
  void getRule() {
    assertThat(reversalsValidationRule.getRule()).isEqualTo(REVERSAL);
  }

  @Test
  void getApplicableTransactionTypes() {
    assertThat(reversalsValidationRule.getApplicableRequest()).containsExactly(ValidationRule.REVERSAL);
  }
}

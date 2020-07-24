package com.tenxbanking.cardrails.domain.validator;

import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.SUBSCRIPTION_STATUS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ValidationConfigurationTest {

  @Test
  void isActive() {
    ValidationConfiguration underTest = new ValidationConfiguration(false, Map.of(SUBSCRIPTION_STATUS, true));
    boolean returned = underTest.isActive(SUBSCRIPTION_STATUS);
    assertThat(returned).isTrue();
  }

  @Test
  void isNotActive() {
    ValidationConfiguration underTest = new ValidationConfiguration(false, Map.of(SUBSCRIPTION_STATUS, false));
    boolean returned = underTest.isActive(SUBSCRIPTION_STATUS);
    assertThat(returned).isFalse();
  }

  @Test
  void defaultsIsActive() {
    ValidationConfiguration underTest = new ValidationConfiguration(true, Map.of());
    boolean returned = underTest.isActive(SUBSCRIPTION_STATUS);
    assertThat(returned).isTrue();
  }

}
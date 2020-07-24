package com.tenxbanking.cardrails.domain.validator;

import com.tenxbanking.cardrails.domain.validator.rule.ValidationRule;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "validation")
public class ValidationConfiguration {

  private boolean defaultIsActive;
  private Map<ValidationRule, Boolean> rules = new HashMap<>();

  public boolean isActive(ValidationRule rule) {
    return rules.getOrDefault(rule, defaultIsActive);
  }

}

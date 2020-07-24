package com.tenxbanking.cardrails.domain.validator;

import com.tenxbanking.cardrails.domain.exception.ValidationException;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.rule.CardTransactionValidationRule;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardTransactionValidator {

  private final List<CardTransactionValidationRule> rules;
  private final ValidationConfiguration configuration;


  @Autowired
  public CardTransactionValidator(List<CardTransactionValidationRule> rules, ValidationConfiguration configuration) {
    this.rules = rules;
    this.configuration = configuration;
  }

  public void validate(CardTransaction cardTransaction, Card card, Subscription subscription, CardSettings cardSettings) {

    List<ValidationFailure> failures = rules
        .stream()
        .filter(rule -> rule.getApplicableTransactionTypes().contains(cardTransaction.getType()))
        .filter(rule -> configuration.isActive(rule.getRule()))
        .map(rule -> rule.validate(cardTransaction, card, subscription, cardSettings))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    if (!failures.isEmpty()) {
      throw new ValidationException(failures);
    }

  }

}

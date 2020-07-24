package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.CLEARING;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.SUBSCRIPTION_STATUS;

import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionStatusValidationRule implements CardTransactionValidationRule {

  private static final String MESSAGE = "Subscription status for card %s is not active, currentStatus=%s";

  @Override
  public Optional<ValidationFailure> validate(CardTransaction cardTransaction, Card card, Subscription subscription, CardSettings cardSettings) {
    SubscriptionStatus subscriptionStatus = subscription.getStatus();
    return SubscriptionStatus.CLOSED == subscriptionStatus
        ? Optional.of(ValidationFailure.of(String.format(MESSAGE, card.getPanHash(), subscriptionStatus)))
        : Optional.empty();
  }

  @Override
  public ValidationRule getRule() {
    return SUBSCRIPTION_STATUS;
  }

  @Override
  public List<CardTransactionType> getApplicableTransactionTypes() {
    return List.of(AUTH, ADVICE, REVERSAL, CLEARING);
  }

}

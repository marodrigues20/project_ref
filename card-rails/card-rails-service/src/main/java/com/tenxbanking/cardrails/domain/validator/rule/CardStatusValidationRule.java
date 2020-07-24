package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.CLEARING;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.CARD_STATUS;

import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CardStatusValidationRule implements CardTransactionValidationRule {

  private static final String MESSAGE = "Card %s status not in active status, currentStatus=%s";

  @Override
  public Optional<ValidationFailure> validate(CardTransaction cardTransaction, Card card, Subscription subscription, CardSettings cardSettings) {
    CardStatus cardStatus = card.getCardStatus();
    return CardStatus.ACTIVE != cardStatus
        ? Optional.of(ValidationFailure.of(String.format(MESSAGE, card.getPanHash(), cardStatus)))
        : Optional.empty();
  }

  @Override
  public ValidationRule getRule() {
    return CARD_STATUS;
  }

  @Override
  public List<CardTransactionType> getApplicableTransactionTypes() {
    return List.of(AUTH, ADVICE, REVERSAL, CLEARING);
  }
}

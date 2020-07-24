package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.CARD_EXPIRY_DATE;

import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.service.TimeService;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardExpiryDateValidationRule implements CardTransactionValidationRule {

  private static final String MESSAGE = "Card %s has expiry date in the past, cardExpiryDate=%s";

  private final TimeService timeService;

  @Autowired
  public CardExpiryDateValidationRule(TimeService timeService) {
    this.timeService = timeService;
  }

  @Override
  public Optional<ValidationFailure> validate(CardTransaction cardTransaction, Card card, Subscription subscription, CardSettings cardSettings) {
    @NonNull Instant expiryDate = card.getCardExpiryDate();
    return timeService.now().isAfter(expiryDate)
        ? Optional.of(ValidationFailure.of(String.format(MESSAGE, card.getPanHash(), timeService.fromInstant(expiryDate))))
        : Optional.empty();
  }

  @Override
  public ValidationRule getRule() {
    return CARD_EXPIRY_DATE;
  }

  @Override
  public List<CardTransactionType> getApplicableTransactionTypes() {
    return List.of(AUTH);
  }
}

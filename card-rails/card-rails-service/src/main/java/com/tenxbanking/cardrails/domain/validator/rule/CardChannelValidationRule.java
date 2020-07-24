package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.CARD_CHANNEL;

import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.card.Channel;
import com.tenxbanking.cardrails.domain.model.card.ChannelSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CardChannelValidationRule implements CardTransactionValidationRule {

  private static final String MESSAGE = "paymentMethodType %s requires channels %s to be active";

  @Override
  public Optional<ValidationFailure> validate(CardTransaction cardTransaction, Card card, Subscription subscription, CardSettings cardSettings) {

    List<Channel> inactiveRequiredChannels = findInactiveRequiredChannels(cardTransaction, cardSettings.getChannelSettings());

    return inactiveRequiredChannels.isEmpty()
        ? Optional.empty()
        : Optional.of(ValidationFailure.of(String.format(MESSAGE,
            cardTransaction.getPaymentMethodType(),
            inactiveRequiredChannels.stream().map(Channel::name).collect(Collectors.joining(",")))));

  }

  private List<Channel> findInactiveRequiredChannels(CardTransaction cardTransaction, ChannelSettings settings) {
    return cardTransaction
        .getPaymentMethodType()
        .getRequiredChannels()
        .stream()
        .filter(s -> !settings.isActive(s))
        .collect(Collectors.toList());
  }

  @Override
  public ValidationRule getRule() {
    return CARD_CHANNEL;
  }

  @Override
  public List<CardTransactionType> getApplicableTransactionTypes() {
    return List.of(AUTH);
  }
}

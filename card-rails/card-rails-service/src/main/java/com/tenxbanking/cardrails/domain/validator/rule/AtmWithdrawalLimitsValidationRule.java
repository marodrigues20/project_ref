package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;

import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionLimit;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.service.TimeService;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AtmWithdrawalLimitsValidationRule implements CardTransactionValidationRule {

  private final AtmWithdrawalLimitsValidationRuleProcessor processor;
  private final TimeService timeService;

  @Autowired
  public AtmWithdrawalLimitsValidationRule(AtmWithdrawalLimitsValidationRuleProcessor processor, TimeService timeService) {
    this.processor = processor;
    this.timeService = timeService;
  }

  @Override
  public Optional<ValidationFailure> validate(CardTransaction cardTransaction, Card card, Subscription subscription, CardSettings cardSettings) {

    final UUID subscriptionKey = card.getSubscriptionKey();
    final SubscriptionSettings settings = subscription.getSubscriptionSettings(timeService.now());
    return settings.getAtmWithdrawalLimits()
        .stream()
        .map(limit -> checkAtmWithdrawalLimit(cardTransaction.getTransactionAmount().getAmount(), subscriptionKey, limit))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  private Optional<ValidationFailure> checkAtmWithdrawalLimit(@NonNull final BigDecimal transactionAmount,
      @NonNull final UUID subscriptionKey,
      @NonNull final TransactionLimit limit) {

    return verifyMinimumAmount(transactionAmount, subscriptionKey, limit)
        .or(() -> verifyMaximumAmount(transactionAmount, subscriptionKey, limit));
  }

  private Optional<ValidationFailure> verifyMinimumAmount(@NonNull final BigDecimal transactionAmount,
      @NonNull final UUID subscriptionKey,
      @NonNull final TransactionLimit limit) {

    return limit.getMinimumAmount()
        .flatMap(limitMinimumAmount -> {

          if (limit.getResetPeriod().isTransaction()) {

            return processor
                .verifyMinimumAmountForTransactionResetPeriod(transactionAmount,
                    subscriptionKey,
                    limitMinimumAmount);
          }
          return Optional.empty();
        });
  }

  private Optional<ValidationFailure> verifyMaximumAmount(@NonNull final BigDecimal transactionAmount,
      @NonNull final UUID subscriptionKey,
      @NonNull final TransactionLimit limit) {

    return limit.getMaximumAmount()
        .flatMap(limitMaximumAmount -> {

          if (limit.getResetPeriod().isTransaction()) {
            return processor
                .verifyMaximumAmountForTransactionResetPeriod(transactionAmount,
                    subscriptionKey, limitMaximumAmount);
          } else {
            return processor
                .verifyMaximumAmountForNonTransactionResetPeriod(transactionAmount,
                    subscriptionKey, limit.getResetPeriod(), limitMaximumAmount);
          }
        });
  }

  @Override
  public ValidationRule getRule() {
    return ValidationRule.ATM_WITHDRAWAL_LIMITS;
  }

  @Override
  public List<CardTransactionType> getApplicableTransactionTypes() {
    return List.of(AUTH);
  }

}

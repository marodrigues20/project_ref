package com.tenxbanking.cardrails.domain.validator;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.CARD_STATUS;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.SUBSCRIPTION_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.exception.ValidationException;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.rule.CardTransactionValidationRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardTransactionValidatorTest {

  @Mock
  private CardTransactionValidationRule rule1;
  @Mock
  private CardTransactionValidationRule rule2;
  @Mock
  private ValidationConfiguration configuration;
  private CardTransactionValidator underTest;

  @Mock
  private CardTransaction cardTransaction;
  @Mock
  private Card card;
  @Mock
  private Subscription subscription;
  @Mock
  private CardSettings cardSettings;

  @BeforeEach
  void setup() {
    underTest = new CardTransactionValidator(List.of(rule1, rule2), configuration);
  }

  @Test
  void validate_filtersByCardTransactionType() {

    when(cardTransaction.getType()).thenReturn(AUTH);
    when(rule1.getApplicableTransactionTypes()).thenReturn(List.of(AUTH, REVERSAL));
    when(rule2.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(configuration.isActive(any())).thenReturn(true);

    underTest.validate(cardTransaction, card, subscription, cardSettings);

    verify(rule1).validate(cardTransaction, card, subscription, cardSettings);
    verifyZeroInteractions(rule2);
  }

  @Test
  void validate_filtersForActiveRules() {

    when(cardTransaction.getType()).thenReturn(REVERSAL);
    when(rule1.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(rule1.getRule()).thenReturn(SUBSCRIPTION_STATUS);
    when(rule2.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(rule2.getRule()).thenReturn(CARD_STATUS);
    when(configuration.isActive(SUBSCRIPTION_STATUS)).thenReturn(true);
    when(configuration.isActive(CARD_STATUS)).thenReturn(false);

    underTest.validate(cardTransaction, card, subscription, cardSettings);

    verify(rule1).validate(cardTransaction, card, subscription, cardSettings);
    verifyZeroInteractions(rule2);
  }

  @Test
  void validate_throwsExceptionWhenAnyRuleFailsValidation() {

    when(cardTransaction.getType()).thenReturn(REVERSAL);
    when(rule1.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(rule2.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(configuration.isActive(any())).thenReturn(true);
    when(rule1.validate(any(), any(), any(), any())).thenReturn(Optional.of(ValidationFailure.of("Failure")));

    ValidationException validationException = assertThrows(
        ValidationException.class,
        () -> underTest.validate(cardTransaction, card, subscription, cardSettings)
    );

    assertThat(validationException.getFailures()).containsExactly(ValidationFailure.of("Failure"));

    verify(rule1).validate(cardTransaction, card, subscription, cardSettings);
    verify(rule2).validate(cardTransaction, card, subscription, cardSettings);
  }

  @Test
  void validate_throwsExceptionWhenAllRulesFailValidation() {

    when(cardTransaction.getType()).thenReturn(REVERSAL);
    when(rule1.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(rule2.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(configuration.isActive(any())).thenReturn(true);
    when(rule1.validate(any(), any(), any(), any())).thenReturn(Optional.of(ValidationFailure.of("Failure")));
    when(rule2.validate(any(), any(), any(), any())).thenReturn(Optional.of(ValidationFailure.of("Failure 2")));

    ValidationException validationException = assertThrows(
        ValidationException.class,
        () -> underTest.validate(cardTransaction, card, subscription, cardSettings)
    );

    assertThat(validationException.getFailures()).containsExactly(ValidationFailure.of("Failure"), ValidationFailure.of("Failure 2"));

    verify(rule1).validate(cardTransaction, card, subscription, cardSettings);
    verify(rule2).validate(cardTransaction, card, subscription, cardSettings);
  }

  @Test
  void validate_doesNotThrowExceptionWhenNoRulesFailValidation() {

    when(cardTransaction.getType()).thenReturn(REVERSAL);
    when(rule1.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(rule2.getApplicableTransactionTypes()).thenReturn(List.of(REVERSAL));
    when(configuration.isActive(any())).thenReturn(true);

    underTest.validate(cardTransaction, card, subscription, cardSettings);

    verify(rule1).validate(cardTransaction, card, subscription, cardSettings);
    verify(rule2).validate(cardTransaction, card, subscription, cardSettings);
  }

}
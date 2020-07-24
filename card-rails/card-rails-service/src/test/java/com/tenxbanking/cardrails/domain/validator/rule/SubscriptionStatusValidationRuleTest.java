package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.CLEARING;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.ACTIVE;
import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.CLOSED;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.SUBSCRIPTION_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.TestConstant;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionStatusValidationRuleTest {

  @InjectMocks
  private SubscriptionStatusValidationRule underTest;

  @Test
  void validate_valid() {

    Subscription subscription = mock(Subscription.class);
    when(subscription.getStatus()).thenReturn(ACTIVE);

    Optional<ValidationFailure> returned = underTest.validate(mock(CardTransaction.class), mock(Card.class), subscription, mock(CardSettings.class));

    assertThat(returned).isEmpty();
  }

  @Test
  void validate_invalid() {

    Subscription subscription = mock(Subscription.class);
    Card card = mock(Card.class);
    when(subscription.getStatus()).thenReturn(CLOSED);
    when(card.getPanHash()).thenReturn(TestConstant.PAN_HASH);

    Optional<ValidationFailure> returned = underTest.validate(mock(CardTransaction.class), card, subscription, mock(CardSettings.class));

    assertThat(returned).contains(ValidationFailure.of("Subscription status for card aPanHash is not active, currentStatus=CLOSED"));
  }

  @Test
  void getRule() {
    assertThat(underTest.getRule()).isEqualTo(SUBSCRIPTION_STATUS);
  }

  @Test
  void getApplicableTransactionTypes() {
    assertThat(underTest.getApplicableTransactionTypes()).containsExactly(AUTH, ADVICE, REVERSAL, CLEARING);
  }

}


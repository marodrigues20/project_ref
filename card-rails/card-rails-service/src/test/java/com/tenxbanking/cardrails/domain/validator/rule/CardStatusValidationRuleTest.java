package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.CLEARING;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static com.tenxbanking.cardrails.domain.model.card.CardStatus.ACTIVE;
import static com.tenxbanking.cardrails.domain.model.card.CardStatus.FROZEN;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.CARD_STATUS;
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
class CardStatusValidationRuleTest {

  @InjectMocks
  private CardStatusValidationRule underTest;

  @Test
  void validate_valid() {

    Card card = mock(Card.class);
    when(card.getCardStatus()).thenReturn(ACTIVE);

    Optional<ValidationFailure> returned = underTest.validate(mock(CardTransaction.class), card, mock(Subscription.class), mock(CardSettings.class));

    assertThat(returned).isEmpty();
  }

  @Test
  void validate_invalid() {

    Card card = mock(Card.class);
    when(card.getCardStatus()).thenReturn(FROZEN);
    when(card.getPanHash()).thenReturn(TestConstant.PAN_HASH);

    Optional<ValidationFailure> returned = underTest.validate(mock(CardTransaction.class), card, mock(Subscription.class), mock(CardSettings.class));

    assertThat(returned).contains(ValidationFailure.of("Card aPanHash status not in active status, currentStatus=FROZEN"));
  }

  @Test
  void getRule() {
    assertThat(underTest.getRule()).isEqualTo(CARD_STATUS);
  }

  @Test
  void getApplicableTransactionTypes() {
    assertThat(underTest.getApplicableTransactionTypes()).containsExactly(AUTH, ADVICE, REVERSAL, CLEARING);
  }

}

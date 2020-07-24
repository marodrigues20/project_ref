package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.CARD_EFFECTIVE_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.TestConstant;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.service.TimeService;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardEffectiveDateValidationRuleTest {

  @Mock
  private TimeService timeService;
  @InjectMocks
  private CardEffectiveDateValidationRule underTest;

  @Test
  void validate_valid() {

    Card card = mock(Card.class);
    when(card.getCardEffectiveDate()).thenReturn(Instant.now().minusSeconds(10));
    when(timeService.now()).thenReturn(Instant.now());

    Optional<ValidationFailure> returned = underTest.validate(mock(CardTransaction.class), card, mock(Subscription.class), mock(CardSettings.class));

    assertThat(returned).isEmpty();
  }

  @Test
  void validate_invalid() {

    Card card = mock(Card.class);
    Instant future = Instant.now().plusSeconds(10);
    when(card.getCardEffectiveDate()).thenReturn(future);
    when(card.getPanHash()).thenReturn(TestConstant.PAN_HASH);
    when(timeService.now()).thenReturn(Instant.now());
    when(timeService.fromInstant(future)).thenReturn("DateString");

    Optional<ValidationFailure> returned = underTest.validate(mock(CardTransaction.class), card, mock(Subscription.class), mock(CardSettings.class));

    assertThat(returned).contains(ValidationFailure.of("Card aPanHash has effective date in the future, effectiveDate=DateString"));
  }

  @Test
  void getRule() {
    assertThat(underTest.getRule()).isEqualTo(CARD_EFFECTIVE_DATE);
  }

  @Test
  void getApplicableTransactionTypes() {
    assertThat(underTest.getApplicableTransactionTypes()).containsExactly(AUTH);
  }
}

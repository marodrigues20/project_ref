package com.tenxbanking.cardrails.validation;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.card.CardStatus.FROZEN;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.config.AppConfig;
import com.tenxbanking.cardrails.domain.exception.ValidationException;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.CardTransactionValidator;
import com.tenxbanking.cardrails.domain.validator.rule.CardStatusValidationRule;
import com.tenxbanking.cardrails.domain.validator.rule.SubscriptionStatusValidationRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(
    classes = {
        CardTransactionValidator.class,
        CardStatusValidationRule.class,
        SubscriptionStatusValidationRule.class,
        AppConfig.class
    },
    properties = {
        "validation.defaultIsActive=true",
        "validation.rules.CARD_STATUS=false"}
)
@RunWith(SpringRunner.class)
public class ValidationRulesConfigurationTest {

  @Autowired
  private CardTransactionValidator cardTransactionValidator;

  @Test
  public void usesDefaultIsActiveFlagWhenNonRuleConfigProvided() {

    final Card card = mock(Card.class);
    final Subscription subscription = mock(Subscription.class);
    final CardTransaction cardTransaction = mock(CardTransaction.class);
    final CardSettings cardSettings = mock(CardSettings.class);
    when(card.getCardStatus()).thenReturn(CardStatus.ACTIVE);
    when(subscription.getStatus()).thenReturn(SubscriptionStatus.CLOSED);
    when(cardTransaction.getType()).thenReturn(AUTH);
    when(card.getPanHash()).thenReturn("panHash");

    assertThrows(ValidationException.class,
        () -> cardTransactionValidator.validate(cardTransaction, card, subscription, cardSettings),
        "[Subscription status for cardpanHash is not active, currentStatus=CLOSED]");

  }

  @Test
  public void ruleSpecificConfigurationOverridesDefault() {

    final Card card = mock(Card.class);
    final Subscription subscription = mock(Subscription.class);
    final CardTransaction cardTransaction = mock(CardTransaction.class);
    final CardSettings cardSettings = mock(CardSettings.class);
    when(card.getCardStatus()).thenReturn(FROZEN);
    when(subscription.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
    when(cardTransaction.getType()).thenReturn(AUTH);
    when(card.getPanHash()).thenReturn("panHash");

    cardTransactionValidator.validate(cardTransaction, card, subscription, cardSettings);

  }

}

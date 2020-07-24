package com.tenxbanking.cardrails.domain.validator.rule;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;
import static com.tenxbanking.cardrails.domain.model.PaymentMethodType.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.card.Channel.ATM;
import static com.tenxbanking.cardrails.domain.model.card.Channel.INTERNATIONAL;
import static com.tenxbanking.cardrails.domain.validator.rule.ValidationRule.CARD_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.card.ChannelSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import com.tenxbanking.cardrails.domain.validator.ValidationFailure;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardChannelValidationRuleTest {

  @InjectMocks
  private CardChannelValidationRule underTest;

  @Mock
  private CardSettings cardSettings;
  @Mock
  private ChannelSettings channelSettings;
  @Mock
  private CardTransaction cardTransaction;

  @Test
  void validate_valid() {
    when(cardSettings.getChannelSettings()).thenReturn(channelSettings);
    when(cardTransaction.getPaymentMethodType()).thenReturn(INTERNATIONAL_CASH_WITHDRAWAL);
    when(channelSettings.isActive(ATM)).thenReturn(true);
    when(channelSettings.isActive(INTERNATIONAL)).thenReturn(true);

    Optional<ValidationFailure> returned = underTest.validate(cardTransaction, mock(Card.class), mock(Subscription.class), cardSettings);

    assertThat(returned).isEmpty();
  }

  @Test
  void validate_invalid() {
    when(cardSettings.getChannelSettings()).thenReturn(channelSettings);
    when(cardTransaction.getPaymentMethodType()).thenReturn(INTERNATIONAL_CASH_WITHDRAWAL);
    when(channelSettings.isActive(ATM)).thenReturn(true);
    when(channelSettings.isActive(INTERNATIONAL)).thenReturn(false);

    Optional<ValidationFailure> returned = underTest.validate(cardTransaction, mock(Card.class), mock(Subscription.class), cardSettings);

    assertThat(returned).contains(ValidationFailure.of("paymentMethodType INTERNATIONAL_CASH_WITHDRAWAL requires channels INTERNATIONAL to be active"));
  }

  @Test
  void getRule() {
    assertThat(underTest.getRule()).isEqualTo(CARD_CHANNEL);
  }

  @Test
  void getApplicableTransactionTypes() {
    assertThat(underTest.getApplicableTransactionTypes()).isEqualTo(List.of(AUTH));
  }

}
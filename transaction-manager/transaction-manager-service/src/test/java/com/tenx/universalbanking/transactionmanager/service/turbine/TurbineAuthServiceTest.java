package com.tenx.universalbanking.transactionmanager.service.turbine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.exception.CardNotFoundException;
import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.model.CardAuth;
import com.tenx.universalbanking.transactionmanager.model.SubscriptionStatus;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.SchemeMessageResponse;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthAdviceMessageService;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthMessageService;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TurbineAuthServiceTest {

  @InjectMocks
  private TurbineAuthService authService;
  @Mock
  private DebitCardProvider cardProvider;
  @Mock
  private CardAuthAdviceMessageService cardAuthAdviceMessageService;
  @Mock
  private CardAuthTransactionMessageBuilder messageBuilder;
  @Mock
  private CardAuthMessageService cardAuthMessageService;
  @Mock
  private SchemeMessageResponseBuilder schemeMessageResponseBuilder;

  private static final String CARD_ID = "123";

  @Test
  public void shouldThrowCardNotFoundExceptionOnAuth() {
    CardAuth cardAuth = Mockito.mock(CardAuth.class);

    when(cardAuth.getCardId()).thenReturn(CARD_ID);
    when(cardProvider.getCard(CARD_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.authorise(cardAuth))
        .isInstanceOf(CardNotFoundException.class)
        .hasMessage("Debit card with Id 123 is not found and returning error");
  }

  @Test
  public void shouldReturnSchemeMessageResponseWhenProcessedForCardAuth() {
    CardAuth cardAuth = Mockito.mock(CardAuth.class);
    TransactionMessage transactionMessage = new TransactionMessage();
    Card card = createCard();
    CardAuthResponse response = new CardAuthResponse();
    SchemeMessageResponse schemeMessageResponse = Mockito.mock(SchemeMessageResponse.class);

    when(cardAuth.getCardId()).thenReturn(CARD_ID);
    when(cardProvider.getCard(CARD_ID)).thenReturn(Optional.of(card));
    when(messageBuilder.create(cardAuth, card, false)).thenReturn(transactionMessage);
    when(cardAuthMessageService.processCardAuth(transactionMessage)).thenReturn(response);
    when(schemeMessageResponseBuilder.create(response, card)).thenReturn(schemeMessageResponse);

    SchemeMessageResponse messageResponse = authService.authorise(cardAuth);

    assertThat(messageResponse).isEqualTo(schemeMessageResponse);
  }

  @Test
  public void shouldReturnSchemeMessageResponseWhenProcessedForCardAdvice() {
    CardAuth cardAuth = Mockito.mock(CardAuth.class);
    TransactionMessage transactionMessage = new TransactionMessage();
    Card card = createCard();
    CardAuthResponse response = new CardAuthResponse();
    SchemeMessageResponse schemeMessageResponse = Mockito.mock(SchemeMessageResponse.class);

    when(cardAuth.getCardId()).thenReturn(CARD_ID);
    when(cardProvider.getCard(CARD_ID)).thenReturn(Optional.of(card));
    when(messageBuilder.create(cardAuth, card, true)).thenReturn(transactionMessage);
    when(cardAuthAdviceMessageService.processCardAuth(transactionMessage)).thenReturn(response);
    when(schemeMessageResponseBuilder.create(response, card)).thenReturn(schemeMessageResponse);

    SchemeMessageResponse messageResponse = authService.advice(cardAuth);

    assertThat(messageResponse).isEqualTo(schemeMessageResponse);
  }

  @Test
  public void shouldCallCardAuthAdviceMessagingServiceOnReversal() {
    CardAuth cardAuth = Mockito.mock(CardAuth.class);
    TransactionMessage transactionMessage = new TransactionMessage();
    Card card = createCard();
    CardAuthResponse response = new CardAuthResponse();
    SchemeMessageResponse schemeMessageResponse = Mockito.mock(SchemeMessageResponse.class);

    when(cardAuth.getCardId()).thenReturn(CARD_ID);
    when(cardProvider.getCard(CARD_ID)).thenReturn(Optional.of(card));
    when(messageBuilder.create(cardAuth, card, true)).thenReturn(transactionMessage);
    when(cardAuthAdviceMessageService.processCardAuth(transactionMessage)).thenReturn(response);
    when(schemeMessageResponseBuilder.create(response, card)).thenReturn(schemeMessageResponse);

    SchemeMessageResponse messageResponse = authService.reversal(cardAuth);

    assertThat(messageResponse).isEqualTo(schemeMessageResponse);
  }

  private Card createCard() {
    return Card.builder()
        .id(CARD_ID)
        .partyKey(UUID.randomUUID())
        .tenantKey("10000")
        .subscriptionKey(UUID.randomUUID())
        .subscriptionStatus(SubscriptionStatus.ACTIVE)
        .productKey(UUID.randomUUID())
        .build();
  }

}


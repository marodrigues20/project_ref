package com.tenx.universalbanking.transactionmanager.service.turbine;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.rest.client.DebitCardManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.dcm.GetCardResponse;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class DebitCardProviderTest {

  private static final String CARD_ID = "pan";

  @Mock
  private DebitCardManagerClient debitCardManagerClient;

  @Mock
  private DebitCardMapper debitCardMapper;

  @InjectMocks
  private DebitCardProvider unit;

  @Test
  public void shouldReturnDomainModelWhenCallToDebitCardManagerAndMappingSucceeds() {

    final Card card = mock(Card.class);
    final GetCardResponse response = mock(GetCardResponse.class);
    when(debitCardManagerClient.getCardById(CARD_ID))
        .thenReturn(ResponseEntity.ok(response));
    when(debitCardMapper.toCard(response)).thenReturn(card);

    Optional<Card> actual = unit.getCard(CARD_ID);

    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(card);
  }

  @Test
  public void shouldReturnEmptyOptionalWhenCallToDebitCardManagerFails() {

    doThrow(RuntimeException.class).when(debitCardManagerClient).getCardById(CARD_ID);

    Optional<Card> actual = unit.getCard(CARD_ID);

    assertThat(actual.isPresent()).isFalse();
  }

  @Test
  public void shouldReturnEmptyOptionalWhenMappingToDomainModelFails() {

    final GetCardResponse response = mock(GetCardResponse.class);
    when(debitCardManagerClient.getCardById(CARD_ID))
        .thenReturn(ResponseEntity.ok(response));
    doThrow(RuntimeException.class).when(debitCardMapper).toCard(response);

    Optional<Card> actual = unit.getCard(CARD_ID);

    assertThat(actual.isPresent()).isFalse();
  }
}


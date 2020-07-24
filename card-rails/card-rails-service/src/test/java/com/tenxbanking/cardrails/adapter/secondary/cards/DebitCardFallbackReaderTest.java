package com.tenxbanking.cardrails.adapter.secondary.cards;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.domain.model.card.Card;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class DebitCardFallbackReaderTest {

  private static final String PAN_HASH = "panHash";
  private static final GetCardRequest GET_CARD_REQUEST = GetCardRequest.builder().panHash(PAN_HASH)
      .build();

  @Mock
  private DebitCardManagerClient debitCardManagerClient;

  @Mock
  private DebitCardMapper debitCardMapper;

  @InjectMocks
  private DebitCardFallbackReader unit;

  @Test
  void shouldReturnDomainModelWhenCallToDebitCardManagerAndMappingSucceeds() {

    final Card card = mock(Card.class);
    final GetCardResponse response = mock(GetCardResponse.class);
    when(debitCardManagerClient.getCardByHash(GET_CARD_REQUEST))
        .thenReturn(ResponseEntity.ok(response));
    when(debitCardMapper.toCard(response)).thenReturn(card);

    Optional<Card> actual = unit.getCard(PAN_HASH);

    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(card);
  }

  @Test
  void shouldReturnEmptyOptionalWhenCallToDebitCardManagerFails() {

    doThrow(RuntimeException.class).when(debitCardManagerClient).getCardByHash(GET_CARD_REQUEST);

    Optional<Card> actual = unit.getCard(PAN_HASH);

    assertThat(actual.isPresent()).isFalse();
  }

  @Test
  void shouldReturnEmptyOptionalWhenMappingToDomainModelFails() {

    final GetCardResponse response = mock(GetCardResponse.class);
    when(debitCardManagerClient.getCardByHash(GET_CARD_REQUEST))
        .thenReturn(ResponseEntity.ok(response));
    doThrow(RuntimeException.class).when(debitCardMapper).toCard(response);

    Optional<Card> actual = unit.getCard(PAN_HASH);

    assertThat(actual.isPresent()).isFalse();
  }
}
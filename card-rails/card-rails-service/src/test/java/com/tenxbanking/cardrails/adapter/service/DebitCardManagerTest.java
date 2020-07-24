package com.tenxbanking.cardrails.adapter.service;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.cards.DebitCardFallbackReader;
import com.tenxbanking.cardrails.adapter.secondary.cards.DebitCardManager;
import com.tenxbanking.cardrails.adapter.secondary.redis.DebitCardRedisRepository;
import com.tenxbanking.cardrails.domain.model.card.Card;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebitCardManagerTest {

  private static final String PAN_HASH = "panHash";
  private static final Card CARD = mock(Card.class);

  @Mock
  private DebitCardRedisRepository debitCardRedisRepository;

  @Mock
  private DebitCardFallbackReader debitCardFallbackReader;

  @InjectMocks
  private DebitCardManager unit;

  @Test
  void shouldReturnCardFromRedis() {

    when(debitCardRedisRepository.findById(PAN_HASH)).thenReturn(of(CARD));

    Optional<Card> cardOptional = unit.findByCardIdHash(PAN_HASH);

    assertThat(cardOptional.get()).isEqualTo(CARD);
    verifyZeroInteractions(debitCardFallbackReader);
  }

  @Test
  void shouldReturnCardFromDebitCardManagerAndSaveItInRedis() {

    when(debitCardRedisRepository.findById(PAN_HASH)).thenReturn(empty());
    when(debitCardFallbackReader.getCard(PAN_HASH)).thenReturn(of(CARD));

    Optional<Card> cardOptional = unit.findByCardIdHash(PAN_HASH);

    assertThat(cardOptional.isPresent()).isTrue();
    assertThat(cardOptional.get()).isEqualTo(CARD);
    verify(debitCardRedisRepository, timeout(250)).save(CARD);
  }

  @Test
  void shouldReturnCardFromDebitCardManagerWhenCallToRedisFails() {

    doThrow(RuntimeException.class).when(debitCardRedisRepository).findById(PAN_HASH);
    when(debitCardFallbackReader.getCard(PAN_HASH)).thenReturn(of(CARD));

    Optional<Card> actual = unit.findByCardIdHash(PAN_HASH);

    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(CARD);

    verify(debitCardRedisRepository, timeout(250)).save(CARD);
  }

  @Test
  void shouldReturnEmptyOptionalWhenCardIsNotInRedisNeitherOnDebitCardManager() {

    Optional<Card> actual = unit.findByCardIdHash(PAN_HASH);

    assertThat(actual.isPresent()).isFalse();
  }

  @Test
  void shouldDeleteById() {
    unit.evictDebitCardByCardIdHash(PAN_HASH);
    verify(debitCardRedisRepository).deleteById(PAN_HASH);
  }
}
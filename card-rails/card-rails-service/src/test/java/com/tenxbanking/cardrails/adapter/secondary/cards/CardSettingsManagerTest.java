package com.tenxbanking.cardrails.adapter.secondary.cards;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_SETTINGS;
import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.tenxbanking.cardrails.adapter.secondary.redis.CardSettingsRedisRepository;
import com.tenxbanking.cardrails.domain.TestConstant;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardSettingsManagerTest {

  @Mock
  private CardSettingsRedisRepository cardSettingsRedisRepository;
  @Mock
  private CardSettingsFallbackReader cardSettingsFallbackReader;
  @InjectMocks
  private CardSettingsManager underTest;

  @Test
  void findsFromRedisIfPresent() {

    when(cardSettingsRedisRepository.findById(PAN_HASH)).thenReturn(of(CARD_SETTINGS));

    Optional<CardSettings> cardSettings = underTest.findByCardIdOrPanHash(CARD_ID, PAN_HASH);

    assertThat(cardSettings).contains(CARD_SETTINGS);
    verify(cardSettingsRedisRepository).findById(PAN_HASH);
    verifyZeroInteractions(cardSettingsFallbackReader);
  }

  @Test
  void findsFromFallbackReaderIffNotInRedisCache() {

    when(cardSettingsRedisRepository.findById(PAN_HASH)).thenReturn(Optional.empty());
    when(cardSettingsFallbackReader.getSettings(CARD_ID)).thenReturn(Optional.of(CARD_SETTINGS));

    Optional<CardSettings> cardSettings = underTest.findByCardIdOrPanHash(CARD_ID, PAN_HASH);

    verify(cardSettingsRedisRepository).findById(PAN_HASH);
    verify(cardSettingsFallbackReader).getSettings(CARD_ID);
    assertThat(cardSettings).contains(CARD_SETTINGS);
  }

  @Test
  void savesFallbackReaderResponseInRedisCache() {

    when(cardSettingsRedisRepository.findById(PAN_HASH)).thenReturn(Optional.empty());
    when(cardSettingsFallbackReader.getSettings(CARD_ID)).thenReturn(Optional.of(CARD_SETTINGS));

    underTest.findByCardIdOrPanHash(CARD_ID, PAN_HASH);

    verify(cardSettingsRedisRepository, timeout(100)).save(CARD_SETTINGS);
  }

  @Test
  void returnsEmptyOptionalIfNotFoundInRedisOrFallbackReader() {

    when(cardSettingsRedisRepository.findById(PAN_HASH)).thenReturn(Optional.empty());
    when(cardSettingsFallbackReader.getSettings(CARD_ID)).thenReturn(Optional.empty());

    Optional<CardSettings> cardSettings = underTest.findByCardIdOrPanHash(CARD_ID, PAN_HASH);

    assertThat(cardSettings).isEmpty();
    verify(cardSettingsRedisRepository, never()).save(any());

  }

  @Test
  void evictDebitCardByCardIdHash() {

    underTest.evictDebitCardByCardIdHash(PAN_HASH);

    verify(cardSettingsRedisRepository).deleteById(PAN_HASH);

  }

}
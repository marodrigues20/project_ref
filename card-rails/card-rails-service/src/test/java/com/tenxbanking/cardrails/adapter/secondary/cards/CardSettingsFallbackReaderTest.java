package com.tenxbanking.cardrails.adapter.secondary.cards;

import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class CardSettingsFallbackReaderTest {

  @Mock
  private DebitCardManagerClient debitCardManagerClient;
  @Mock
  private CardSettingsMapper cardSettingsMapper;
  @InjectMocks
  private CardSettingsFallbackReader underTest;

  @Mock
  private DebitCardSettingsResponse settingsResponse;
  @Mock
  private CardSettings cardSettings;

  @Test
  void getsCardSettings() {
    when(debitCardManagerClient.getCardSettings(PAN_HASH)).thenReturn(ResponseEntity.ok(settingsResponse));
    underTest.getSettings(PAN_HASH);
    verify(debitCardManagerClient).getCardSettings(PAN_HASH);
  }

  @Test
  void mapsResponseToDomain() {
    when(debitCardManagerClient.getCardSettings(PAN_HASH)).thenReturn(ResponseEntity.ok(settingsResponse));
    underTest.getSettings(PAN_HASH);
    verify(cardSettingsMapper).map(settingsResponse);
  }

  @Test
  void returnsTransformedResponse() {
    when(debitCardManagerClient.getCardSettings(PAN_HASH)).thenReturn(ResponseEntity.ok(settingsResponse));
    when(cardSettingsMapper.map(settingsResponse)).thenReturn(cardSettings);
    Optional<CardSettings> returned = underTest.getSettings(PAN_HASH);
    assertThat(returned).contains(cardSettings);
  }

  @Test
  void returnsEmptyOptionalWhenSettingsNotFound() {
    when(debitCardManagerClient.getCardSettings(PAN_HASH)).thenThrow(new HttpClientErrorException(NOT_FOUND));
    Optional<CardSettings> returned = underTest.getSettings(PAN_HASH);
    assertThat(returned).isEmpty();
  }

}
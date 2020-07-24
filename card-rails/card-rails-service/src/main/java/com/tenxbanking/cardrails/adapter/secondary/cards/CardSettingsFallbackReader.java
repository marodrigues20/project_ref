package com.tenxbanking.cardrails.adapter.secondary.cards;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class CardSettingsFallbackReader {

  private final DebitCardManagerClient debitCardManagerClient;
  private final CardSettingsMapper cardSettingsMapper;

  public Optional<CardSettings> getSettings(@NonNull final String panHash) {

    try {
      final ResponseEntity<DebitCardSettingsResponse> response = debitCardManagerClient.getCardSettings(panHash);
      return Optional.of(cardSettingsMapper.map(response.getBody()));
    } catch (Exception e) {
      log.error("Could not get card {} from debit card manager", panHash, e);
      return empty();
    }
  }

}
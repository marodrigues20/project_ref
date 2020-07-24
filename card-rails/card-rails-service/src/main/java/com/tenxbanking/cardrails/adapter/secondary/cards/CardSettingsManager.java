package com.tenxbanking.cardrails.adapter.secondary.cards;

import static java.util.concurrent.CompletableFuture.runAsync;

import com.tenxbanking.cardrails.adapter.secondary.redis.CardSettingsRedisRepository;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.port.CardSettingsService;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CardSettingsManager implements CardSettingsService {

  private final CardSettingsRedisRepository cardSettingsRedisRepository;
  private final CardSettingsFallbackReader cardSettingsFallbackReader;

  @Autowired
  public CardSettingsManager(
      CardSettingsRedisRepository cardSettingsRedisRepository,
      CardSettingsFallbackReader cardSettingsFallbackReader) {
    this.cardSettingsRedisRepository = cardSettingsRedisRepository;
    this.cardSettingsFallbackReader = cardSettingsFallbackReader;
  }

  public Optional<CardSettings> findByCardIdOrPanHash(@NonNull final String cardId, @NonNull final String cardIdHash) {
    return cardSettingsRedisRepository.findById(cardIdHash)
        .or(() -> fetchAndSaveCardFromDebitCardManager(cardId));
  }

  @Override
  public void evictDebitCardByCardIdHash(@NonNull String cardIdHash) {
    cardSettingsRedisRepository.deleteById(cardIdHash);
  }

  private Optional<CardSettings> fetchAndSaveCardFromDebitCardManager(@NonNull final String cardId) {
    final Optional<CardSettings> cardOptional = cardSettingsFallbackReader.getSettings(cardId);
    cardOptional.ifPresent(cardSettings -> runAsync(() -> cardSettingsRedisRepository.save(cardSettings)));
    return cardOptional;
  }

}
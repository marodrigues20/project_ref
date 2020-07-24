package com.tenxbanking.cardrails.adapter.secondary.cards;

import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.runAsync;

import com.tenxbanking.cardrails.adapter.secondary.redis.DebitCardRedisRepository;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.port.DebitCardService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class DebitCardManager implements DebitCardService {

  private final DebitCardRedisRepository debitCardRedisRepository;
  private final DebitCardFallbackReader debitCardFallbackReader;

  public Optional<Card> findByCardIdHash(@NonNull final String panHash) {

    return getCardFromRedis(panHash)
        .or(() -> fetchAndSaveCardFromDebitCardManager(panHash));
  }

  @Override
  public void evictDebitCardByCardIdHash(@NonNull String panHash) {
    try {
      debitCardRedisRepository.deleteById(panHash);
      log.info("Deleted card {} from redis (cache eviction)", panHash);
    } catch (Exception e) {
      log.error("Exception deleting card {} from redis (cache eviction)", panHash, e);
      throw e;
    }
  }

  private Optional<Card> fetchAndSaveCardFromDebitCardManager(@NonNull final String panHash) {

    final Optional<Card> cardOptional = debitCardFallbackReader.getCard(panHash);
    cardOptional.ifPresent(this::saveAsyncIntoRedis);
    return cardOptional;
  }

  private void save(@NonNull final Card card) {
    try {
      debitCardRedisRepository.save(card);
      log.info("Saved card {} to redis", card.getPanHash());
    } catch (Exception e) {
      log.error("Exception saving card {} to redis", card.getPanHash(), e);
    }
  }

  private Optional<Card> getCardFromRedis(@NonNull final String panHash) {
    try {
      return debitCardRedisRepository.findById(panHash);
    } catch (Exception e) {
      log.error("Exception getting card {} from redis", panHash, e);
      return empty();
    }
  }

  private void saveAsyncIntoRedis(@NonNull final Card card) {
    runAsync(() -> save(card));
  }
}
package com.tenxbanking.cardrails.adapter.secondary.cards;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.domain.model.card.Card;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class DebitCardFallbackReader {

  private final DebitCardManagerClient debitCardManagerClient;
  private final DebitCardMapper debitCardMapper;

  public Optional<Card> getCard(@NonNull final String panHash) {

    GetCardRequest request = GetCardRequest.builder().panHash(panHash).build();

    try {
      final ResponseEntity<GetCardResponse> response = debitCardManagerClient.getCardByHash(request);
      return toCard(response.getBody());
    } catch (Exception e) {
      log.error("Could not get card {} from debit card manager", panHash, e);
      return empty();
    }
  }

  private Optional<Card> toCard(@NonNull final GetCardResponse response) {
    return of(debitCardMapper.toCard(response));
  }
}
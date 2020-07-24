package com.tenx.universalbanking.transactionmanager.service.turbine;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.rest.client.DebitCardManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.dcm.GetCardResponse;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
class DebitCardProvider {

  private final DebitCardManagerClient debitCardManagerClient;
  private final DebitCardMapper debitCardMapper;

  public Optional<Card> getCard(@NonNull final String cardId) {
    try {
      ResponseEntity<GetCardResponse> response = debitCardManagerClient.getCardById(cardId);
      return toCard(response.getBody());
    } catch (Exception e) {
      log.error("Could not get card {} from debit card manager", cardId, e);
      return empty();
    }
  }

  private Optional<Card> toCard(@NonNull final GetCardResponse response) {
    return of(debitCardMapper.toCard(response));
  }
}
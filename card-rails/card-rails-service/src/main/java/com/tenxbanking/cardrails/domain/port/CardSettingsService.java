package com.tenxbanking.cardrails.domain.port;

import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import java.util.Optional;
import lombok.NonNull;

public interface CardSettingsService {

  Optional<CardSettings> findByCardIdOrPanHash(@NonNull final String cardId, @NonNull final String cardIdHash);

  void evictDebitCardByCardIdHash(@NonNull final String cardIdHash);

}

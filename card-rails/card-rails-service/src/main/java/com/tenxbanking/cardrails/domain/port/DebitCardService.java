package com.tenxbanking.cardrails.domain.port;

import com.tenxbanking.cardrails.domain.model.card.Card;
import java.util.Optional;
import lombok.NonNull;

public interface DebitCardService {

  Optional<Card> findByCardIdHash(@NonNull final String panHash);

  void evictDebitCardByCardIdHash(@NonNull final String panHash);

}

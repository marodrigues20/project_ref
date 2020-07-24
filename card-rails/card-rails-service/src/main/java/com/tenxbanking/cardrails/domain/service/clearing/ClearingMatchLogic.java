package com.tenxbanking.cardrails.domain.service.clearing;

import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.port.finder.AuthFinder;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class ClearingMatchLogic {

  private final AuthFinder authFinder;

  public Optional<AuthTransaction> matchingLogic(@NonNull final Cain003 cain003) {

    return authFinder
        .findByCardIdAndTransactionId(
            cain003.getCardId(), cain003.getTransactionLifeCycleID())
        .or(() -> authFinder
            .findByCardIdAndCreatedDateAndCardAmountAndCardAuth(
                cain003.getCardId(),
                cain003.getCreatedDate(),
                cain003.getTransactionAmount().getAmount(),
                cain003.getAuthCode()))
        .or(() -> authFinder
            .findByCardIdAndTransactionAmountAndTransactionAuthCode(
                cain003.getCardId(),
                cain003.getTransactionAmount().getAmount(), cain003.getAuthCode()))
        .or(() -> authFinder
            .findByCardIdAndTransactionDateAndTransactionAuthCode(
                cain003.getCardId(),
                cain003.getCreatedDate(),
                cain003.getAuthCode()))
        .or(() -> authFinder
            .findByCardIdAndTransactionDate(
                cain003.getCardId(), cain003.getCreatedDate()));


  }

}

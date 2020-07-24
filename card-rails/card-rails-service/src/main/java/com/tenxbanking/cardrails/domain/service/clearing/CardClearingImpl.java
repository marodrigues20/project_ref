package com.tenxbanking.cardrails.domain.service.clearing;

import com.tenxbanking.cardrails.adapter.secondary.cards.DebitCardManager;
import com.tenxbanking.cardrails.domain.exception.CardClearingDebitCardManagementException;
import com.tenxbanking.cardrails.domain.exception.CardClearingSubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import com.tenxbanking.cardrails.domain.port.CardClearingService;
import com.tenxbanking.cardrails.domain.port.FeesCheckerService;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import com.tenxbanking.cardrails.domain.port.store.CardClearingTransactionStore;
import com.tenxbanking.cardrails.domain.service.PanHashingService;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardClearingImpl implements CardClearingService {

  private static final String DEBT_CARD_MGN_ERROR = "Failed to retrieve Card. ";

  private final PanHashingService panHashingService;
  private final DebitCardManager debitCardManagerService;
  private final FeesCheckerService feesCheckerService;
  private final CardClearingTransactionStore cardClearingStoreService;
  private final SubscriptionService subscriptionService;
  private final ClearingMatchLogic clearingMatchLogic;
  private final TimeService timeService;

  private final Supplier<UUID> uuidSupplier;

  public void process(@NonNull final Cain003 cain003) {

    CardClearing cardClearing;
    Optional<AuthTransaction> authTransaction = clearingMatchLogic.matchingLogic(cain003);

    final Card card = getCard(cain003.getCardId());
    final UUID subscriptionKey = card.getSubscriptionKey();
    final UUID productKey = getProductKey(subscriptionKey);

    cardClearing = authTransaction
        .map(transaction -> processWithMatch(cain003, transaction, subscriptionKey, card,
            productKey))
        .orElseGet(() -> processWithNotMatch(cain003, card, subscriptionKey, productKey));

    cardClearingStoreService.save(cardClearing);
  }

  private CardClearing processWithMatch(Cain003 cain003, AuthTransaction authTransaction,
      UUID subscriptionKey, Card card, UUID productKey) {

    Cain001 cain001 = authTransaction.getCain001();
    Cain003 cain003WithIds = cain003
        .addTransactionIds(cain001.getTransactionId(), cain001.getCorrelationId());
    Cain003 cain003ToSave;

    //TODO: make this getBilling amount after John Rebase.
    if (authTransaction.getTransactionAmount().equals(cain003WithIds.getBillingAmount())) {
      Optional<Fee> cain001Fee = cain001.getFee();
      cain003ToSave = addFeeIfPresent(cain003WithIds, cain001Fee);
    } else {
      Optional<Fee> fee = feesCheckerService.check(cain003WithIds, subscriptionKey);
      cain003ToSave = addFeeIfPresent(cain003WithIds, fee);
    }

    return new CardClearing(
        cain003ToSave.getCardId(),
        card.getSubscriptionKey(),
        card.getPartyKey(),
        productKey,
        card.getTenantKey(),
        cain003ToSave);
  }

  private CardClearing processWithNotMatch(Cain003 cain003, Card card, UUID subscriptionKey,
      UUID productKey) {

    Cain003 cain003WithIds = cain003.addTransactionIds(uuidSupplier.get(), uuidSupplier.get());

    Optional<Fee> fee = feesCheckerService.check(cain003WithIds, subscriptionKey);
    Cain003 cain003ToSave = addFeeIfPresent(cain003WithIds, fee);

    return new CardClearing(
        cain003ToSave.getCardId(),
        card.getSubscriptionKey(),
        card.getPartyKey(),
        productKey,
        card.getTenantKey(),
        cain003ToSave);
  }

  private Card getCard(final String cardId) {
    return debitCardManagerService.findByCardIdHash(panHashingService.hashCardId(cardId))
        .orElseThrow(() -> new CardClearingDebitCardManagementException(DEBT_CARD_MGN_ERROR));
  }

  private @NonNull UUID getProductKey(final UUID subscriptionKey) {
    return subscriptionService.findById(subscriptionKey)
        .orElseThrow(CardClearingSubscriptionNotFoundException::new)
        .getSubscriptionSettings(timeService.now())
        .getProductKey();
  }

  private Cain003 addFeeIfPresent(Cain003 cain003, Optional<Fee> fee) {
    return fee.isPresent() ? cain003.addFee(fee.get()) : cain003;
  }
}

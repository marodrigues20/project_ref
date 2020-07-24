package com.tenxbanking.cardrails.domain.service.handler;

import static java.util.Optional.empty;

import com.tenxbanking.cardrails.domain.exception.CardNotFoundException;
import com.tenxbanking.cardrails.domain.exception.CardSettingsNotFoundException;
import com.tenxbanking.cardrails.domain.exception.SubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Fee;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.port.CardSettingsService;
import com.tenxbanking.cardrails.domain.port.DebitCardService;
import com.tenxbanking.cardrails.domain.port.FeesCheckerService;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import com.tenxbanking.cardrails.domain.port.sender.AuthReserveTransactionSender;
import com.tenxbanking.cardrails.domain.port.store.CardTransactionStore;
import com.tenxbanking.cardrails.domain.service.PanHashingService;
import com.tenxbanking.cardrails.domain.service.TimeService;
import com.tenxbanking.cardrails.domain.validator.CardTransactionValidator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class CardAuthHandler implements CardHandler {

  private final TimeService timeService;
  private final SubscriptionService subscriptionService;
  private final DebitCardService debitCardService;
  private final FeesCheckerService feesChecker;
  private final CardSettingsService cardSettingsService;
  private final AuthReserveTransactionSender transactionSender;
  private final PanHashingService panHashingService;
  private final Supplier<UUID> uuidSupplier;
  private final CardTransactionStore<AuthTransaction> cardAuthStore;
  private final CardTransactionValidator cardTransactionValidator;

  @Override
  public CardTransactionType handlesCardTransactionType() {
    return CardTransactionType.AUTH;
  }

  @Override
  @Transactional
  public Cain002 auth(@NonNull final Cain001 cain001) {

    final Instant now = timeService.now();

    final Card card = getCard(cain001.getCardId());
    final UUID subscriptionKey = card.getSubscriptionKey();
    final Subscription subscription = getSubscription(subscriptionKey);
    final SubscriptionSettings settings = subscription.getSubscriptionSettings(now);
    final CardSettings cardSettings = getCardSettings(cain001.getCardId(), card.getPanHash());

    final Cain001 cainWithIds = cain001.addTransactionIds(uuidSupplier.get(), uuidSupplier.get());

    final CardAuth cardAuth = new CardAuth(
        cain001.getCardId(),
        card.getSubscriptionKey(),
        card.getPartyKey(),
        settings.getProductKey(),
        card.getTenantKey(),
        cainWithIds);

    cardTransactionValidator.validate(cardAuth, card, subscription, cardSettings);

    Optional<Fee> fee = settings.isHasFees() ? feesChecker.check(cainWithIds, subscriptionKey) : empty();

    CardAuth cardAuthToReserve = fee.isPresent() ? cardAuth.withFee(fee.get()) : cardAuth;

    Cain002 cain002 = transactionSender.reserve(cardAuthToReserve);

    cardAuthToReserve = cardAuthToReserve.toBuilder().cain002(cain002).build();

    cardAuthStore.save(cardAuthToReserve);

    //Convert to official Cain001 and publish on an avro topic

    return cain002;
  }

  private CardSettings getCardSettings(@NonNull final String cardId, @NonNull final String panHash) {
    return cardSettingsService
        .findByCardIdOrPanHash(cardId, panHash)
        .orElseThrow(CardSettingsNotFoundException::new);
  }

  private Card getCard(@NonNull final String cardId) {
    return debitCardService
        .findByCardIdHash(panHashingService.hashCardId(cardId))
        .orElseThrow(CardNotFoundException::new);
  }

  private Subscription getSubscription(@NonNull final UUID subscriptionKey) {

    final Optional<Subscription> subscriptionOptional = subscriptionService
        .findById(subscriptionKey);

    return subscriptionOptional.orElseThrow(SubscriptionNotFoundException::new);
  }
}

package com.tenxbanking.cardrails.domain.service.handler;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static java.lang.String.format;

import com.tenxbanking.cardrails.adapter.primary.rest.exception.UnsolicitedReversalException;
import com.tenxbanking.cardrails.domain.exception.CardNotFoundException;
import com.tenxbanking.cardrails.domain.exception.CardSettingsNotFoundException;
import com.tenxbanking.cardrails.domain.exception.SubscriptionNotFoundException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.card.Card;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuthReversal;
import com.tenxbanking.cardrails.domain.port.CardSettingsService;
import com.tenxbanking.cardrails.domain.port.DebitCardService;
import com.tenxbanking.cardrails.domain.port.SubscriptionService;
import com.tenxbanking.cardrails.domain.port.finder.AuthFinder;
import com.tenxbanking.cardrails.domain.port.sender.AuthReversalTransactionSender;
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

@Slf4j
@Service
@AllArgsConstructor
public class CardAuthReversalHandler implements CardHandler {

  private final TimeService timeService;
  private final SubscriptionService subscriptionService;
  private final DebitCardService debitCardService;
  private final CardSettingsService cardSettingsService;
  private final AuthReversalTransactionSender transactionSender;
  private final PanHashingService panHashingService;
  private final CardTransactionStore<AuthTransaction> authTransactionStore;
  private final AuthFinder authFinder;
  private final Supplier<UUID> uuidSupplier;
  private final CardTransactionValidator cardTransactionValidator;

  @Override
  public CardTransactionType handlesCardTransactionType() {
    return REVERSAL;
  }

  @Override
  public Cain002 auth(@NonNull final Cain001 cain001) {

    final Instant now = timeService.now();
    final Card card = getCard(cain001.getCardId());
    final UUID subscriptionKey = card.getSubscriptionKey();
    final Subscription subscription = getSubscription(subscriptionKey);
    final SubscriptionSettings settings = subscription.getSubscriptionSettings(now);
    final CardSettings cardSettings = getCardSettings(cain001.getCardId(), card.getPanHash());

    AuthTransaction cardAuth = authFinder.findMatchingAuthByRetrievalReferenceNumber(cain001.getRetrievalReferenceNumber())
        .or(() -> authFinder.findMatchingAuthByBanknetReferenceNumber(cain001.getBanknetReferenceNumber()))
        .or(() -> authFinder.findMatchingAuthByAuthCode(cain001.getAuthCode()))
        .orElseThrow(() ->
            new UnsolicitedReversalException(
                format("Cannot matching auth for reversal with retrievalReferenceNumber=%s, banknetReferenceNumber=%s, authCode=%s",
                    cain001.getRetrievalReferenceNumber(),
                    cain001.getBanknetReferenceNumber(),
                    cain001.getAuthCode())));
    // TODO: if the auth has a fee, how should we reverse that?

    final Cain001 cainWithIds = cain001.addTransactionIds(
        cardAuth.getCain001().getTransactionId(),
        cardAuth.getCain001().getCorrelationId());

    CardAuthReversal cardAuthReversal = new CardAuthReversal(
        cain001.getCardId(),
        card.getSubscriptionKey(),
        card.getPartyKey(),
        settings.getProductKey(),
        card.getTenantKey(),
        cainWithIds);

    cardTransactionValidator.validate(cardAuthReversal, card, subscription, cardSettings);

    Cain002 cain002 = transactionSender.reverse(cardAuthReversal);

    cardAuthReversal = cardAuthReversal.toBuilder().cain002(cain002).build();

    authTransactionStore.save(cardAuthReversal);

    return cain002;
  }

  private CardSettings getCardSettings(@NonNull final String cardId, @NonNull final String panHash) {
    return cardSettingsService
        .findByCardIdOrPanHash(cardId, panHash)
        .orElseThrow(CardSettingsNotFoundException::new);
  }

  private Card getCard(@NonNull final String cardId) {

    final Optional<Card> cardOptional = debitCardService
        .findByCardIdHash(panHashingService.hashCardId(cardId));

    return cardOptional.orElseThrow(CardNotFoundException::new);
  }

  private Subscription getSubscription(@NonNull final UUID subscriptionKey) {

    final Optional<Subscription> subscriptionOptional = subscriptionService
        .findById(subscriptionKey);

    return subscriptionOptional.orElseThrow(SubscriptionNotFoundException::new);
  }

}

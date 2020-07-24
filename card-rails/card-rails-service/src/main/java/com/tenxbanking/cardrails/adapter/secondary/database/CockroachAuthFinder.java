package com.tenxbanking.cardrails.adapter.secondary.database;

import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.AUTH;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.transformer.AuthTransactionEntityToDomainTransformer;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.port.finder.AuthFinder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CockroachAuthFinder implements AuthFinder {

  private static final List<CardTransactionType> MATCHING_AUTH_TYPES = List.of(AUTH, ADVICE);


  private final CardTransactionCockroachRepository cardTransactionCockroachRepository;
  private final AuthTransactionEntityToDomainTransformer authTransactionEntityToDomainTransformer;

  @Override
  public Optional<AuthTransaction> findMatchingAuthByRetrievalReferenceNumber(
      String retrievalReferenceNumber) {
    return findMatchingAuth(() ->
        cardTransactionCockroachRepository
            .findByCardTransactionTypeInAndRetrievalReferenceNumberAndIsSuccess(
                MATCHING_AUTH_TYPES,
                retrievalReferenceNumber,
                true
            ));
  }

  @Override
  public Optional<AuthTransaction> findMatchingAuthByBanknetReferenceNumber(
      String banknetReferenceNumber) {
    return findMatchingAuth(() ->
        cardTransactionCockroachRepository
            .findByCardTransactionTypeInAndBanknetReferenceNumberAndIsSuccess(
                MATCHING_AUTH_TYPES,
                banknetReferenceNumber,
                true
            ));
  }

  @Override
  public Optional<AuthTransaction> findMatchingAuthByAuthCode(String authCode) {
    return findMatchingAuth(() ->
        cardTransactionCockroachRepository.findByCardTransactionTypeInAndAuthCodeAndIsSuccess(
            MATCHING_AUTH_TYPES,
            authCode,
            true
        ));
  }



  @Override
  public Optional<AuthTransaction> findByCardIdAndTransactionId(
      String cardId, String transactionLifeCycleId) {

    return findMatchingAuth(() ->
        cardTransactionCockroachRepository
            .findByCardTransactionTypeAndIdAndTransactionIdAndIsSuccess(
                MATCHING_AUTH_TYPES,
                cardId,
                transactionLifeCycleId,
                true
            ));

  }

  @Override
  public Optional<AuthTransaction> findByCardIdAndCreatedDateAndCardAmountAndCardAuth(
      String cardId, Instant createdDate,
      BigDecimal transactionAmount, String transactionAuthCode) {

    return findMatchingAuth(() ->
        cardTransactionCockroachRepository
            .findByCardTransactionTypeAndCardIdAndCreatedDateAndTransactionAmountAndAuthCodeAndIsSuccess(
                MATCHING_AUTH_TYPES,
                cardId,
                createdDate,
                transactionAmount,
                transactionAuthCode,
                true
            ));


  }

  @Override
  public Optional<AuthTransaction> findByCardIdAndTransactionAmountAndTransactionAuthCode(
      String cardId, BigDecimal transactionAmount,
      String transactionAuthCode) {

    return findMatchingAuth(() ->
        cardTransactionCockroachRepository
            .findByCardTransactionTypeAndCardIdAndTransactionAmountAndAuthCodeAndIsSuccess(
                MATCHING_AUTH_TYPES,
                cardId,
                transactionAmount,
                transactionAuthCode,
                true
            ));

  }

  @Override
  public Optional<AuthTransaction> findByCardIdAndTransactionDateAndTransactionAuthCode(
      String cardId, Instant transactionDate,
      String transactionAuthCode) {

    return findMatchingAuth(() ->
        cardTransactionCockroachRepository
            .findByCardTransactionTypeAndCardIdAndCreatedDateAndAuthCodeAndIsSuccess(
                MATCHING_AUTH_TYPES,
                cardId,
                transactionDate,
                transactionAuthCode,
                true
            ));

  }

  @Override
  public Optional<AuthTransaction> findByCardIdAndTransactionDate(
      String cardId, Instant transactionDate) {

    return findMatchingAuth(() ->
        cardTransactionCockroachRepository
            .findByCardTransactionTypeAndCardIdAndCreatedDateAndIsSuccess(
                MATCHING_AUTH_TYPES,
                cardId,
                transactionDate,
                true
            ));


  }

  private Optional<AuthTransaction> findMatchingAuth(
      Supplier<List<CardTransactionEntity>> supplier) {

    List<CardTransactionEntity> entities = supplier.get();

    if (entities.size() > 1) {
      throw new IllegalStateException(
          String.format("More than one matching auth found, entities=%s", entities));
    }

    return entities
        .stream()
        .findFirst()
        .map(authTransactionEntityToDomainTransformer::transform);
  }
}

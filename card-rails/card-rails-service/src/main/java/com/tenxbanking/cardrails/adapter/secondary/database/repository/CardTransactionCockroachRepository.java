package com.tenxbanking.cardrails.adapter.secondary.database.repository;

import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardTransactionCockroachRepository extends
    JpaRepository<CardTransactionEntity, UUID> {

  List<CardTransactionEntity> findByCardTransactionTypeInAndRetrievalReferenceNumberAndIsSuccess(
      List<CardTransactionType> cardTransactionTypes,
      String retrievalReferenceNumber,
      boolean isSuccess);

  List<CardTransactionEntity> findByCardTransactionTypeInAndBanknetReferenceNumberAndIsSuccess(
      List<CardTransactionType> cardTransactionTypes,
      String banknetReferenceNumber,
      boolean isSuccess);

  List<CardTransactionEntity> findByCardTransactionTypeInAndAuthCodeAndIsSuccess(
      List<CardTransactionType> cardTransactionTypes,
      String authCode,
      boolean isSuccess);

  List<CardTransactionEntity> findByCardTransactionTypeAndIdAndTransactionIdAndIsSuccess(
      List<CardTransactionType> matchingAuthTypes, String cardId, String transactionLifeCycleId,
      boolean isSuccess);

  List<CardTransactionEntity> findByCardTransactionTypeAndCardIdAndCreatedDateAndTransactionAmountAndAuthCodeAndIsSuccess(
      List<CardTransactionType> matchingAuthTypes, String cardId, Instant createdDate,
      BigDecimal transactionAmount, String transactionAuthCode, boolean isSuccess);

  List<CardTransactionEntity> findByCardTransactionTypeAndCardIdAndTransactionAmountAndAuthCodeAndIsSuccess(
      List<CardTransactionType> matchingAuthTypes,
      String cardId, BigDecimal transactionAmount, String transactionAuthCode, boolean isSuccess);

  List<CardTransactionEntity> findByCardTransactionTypeAndCardIdAndCreatedDateAndAuthCodeAndIsSuccess(
      List<CardTransactionType> matchingAuthTypes, String cardId,
      Instant transactionDate, String transactionAuthCode, boolean isSuccess);

  List<CardTransactionEntity> findByCardTransactionTypeAndCardIdAndCreatedDateAndIsSuccess(
      List<CardTransactionType> matchingAuthTypes, String cardId, Instant transactionDate,
      boolean isSuccess);
}

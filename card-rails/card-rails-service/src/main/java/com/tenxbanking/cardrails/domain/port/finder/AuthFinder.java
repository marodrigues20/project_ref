package com.tenxbanking.cardrails.domain.port.finder;

import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface AuthFinder {

  Optional<AuthTransaction> findMatchingAuthByRetrievalReferenceNumber(String retrievalReferenceNumber);

  Optional<AuthTransaction> findMatchingAuthByBanknetReferenceNumber(String banknetReferenceNumber);

  Optional<AuthTransaction> findMatchingAuthByAuthCode(String authCode);

  Optional<AuthTransaction> findByCardIdAndTransactionId(
      String cardId, String transactionLifeCycleID);


  Optional<AuthTransaction> findByCardIdAndCreatedDateAndCardAmountAndCardAuth(
      String cardId,
      Instant createdDate,
      BigDecimal transactionAmount,
      String authCode);

  Optional<AuthTransaction> findByCardIdAndTransactionAmountAndTransactionAuthCode(
      String cardId,
      BigDecimal transactionAmount,
      String authCode);


  Optional<AuthTransaction> findByCardIdAndTransactionDateAndTransactionAuthCode(
      String cardId,
      Instant createdDate,
      String authCode);

  Optional<AuthTransaction> findByCardIdAndTransactionDate(
      String cardId,
      Instant createdDate);


}

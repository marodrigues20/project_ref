package com.tenx.universalbanking.transactionmanager.service.helpers;

import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorisationFinder {

  @Autowired
  private PaymentAuthorisations authRepository;

  private static final String AUTH = "AUTH";
  private static final String REVERSAL = "REVERSAL";

  private Authorisations getAuthorisation(String tsysAccountId,
      Date date,
      BigDecimal transactionAmount,
      String authorisationCode,
      String bankNetReferenceNumber,
      boolean isReversal) {
    String transactionType = isReversal ? REVERSAL : AUTH;
    LocalDate transactionDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    Optional<Authorisations> authorisation = authRepository
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(tsysAccountId, bankNetReferenceNumber, transactionType);
    if (authorisation.isPresent()) {
      return authorisation.get();
    }

    authorisation = authRepository
        .findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
            tsysAccountId,
            transactionAmount, transactionDate, authorisationCode);
    if (authorisation.isPresent()) {
      return authorisation.get();
    }

    authorisation = authRepository
        .findByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode(tsysAccountId,
            transactionAmount, authorisationCode);

    if (authorisation.isPresent()) {
      return authorisation.get();
    }

    authorisation = authRepository
        .findByTsysAccountIdAndId_TransactionDateAndId_AuthorisationCode(tsysAccountId,
            transactionDate, authorisationCode);

    if (authorisation.isPresent()) {
      return authorisation.get();
    }

    authorisation = authRepository
        .findByTsysAccountIdAndId_TransactionDateAndTransactionType(tsysAccountId, transactionDate, transactionType);
    return authorisation.orElse(null);
  }

  public Authorisations getAuthorisation(String tsysAccountId,
      Date date,
      BigDecimal transactionAmount,
      String authorisationCode,
      String bankNetReferenceNumber) {
    return getAuthorisation(tsysAccountId, date, transactionAmount, authorisationCode, bankNetReferenceNumber, false);
  }

  public Authorisations getAuthorisationEntryForReversal(String tsysAccountId,
      String bankNetReferenceNumber) {
    Optional<Authorisations> authorisation = authRepository
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(tsysAccountId, bankNetReferenceNumber, AUTH);
    return authorisation.orElse(null);
  }

  public Optional<Authorisations> getAuthorisation(String tsysAccountId,
      LocalDate transactionDate,
      BigDecimal transactionAmount,
      String bankNetReferenceNumber,
      boolean isReversal) {
    String transactionType = isReversal ? REVERSAL : AUTH;
    return authRepository
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionAmountAndId_TransactionDateAndTransactionType(
            tsysAccountId, bankNetReferenceNumber, transactionAmount, transactionDate, transactionType);
  }

  public List<Authorisations> getUnmatchedNonReversedAuthorisations(LocalDate date,
      boolean matched, boolean expired) {
    return authRepository
        .findAuthorisationsBeforeDateAndNotExpiredAndNotMatched(date, matched, expired);
  }
}

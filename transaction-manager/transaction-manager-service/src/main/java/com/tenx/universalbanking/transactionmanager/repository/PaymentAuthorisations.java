package com.tenx.universalbanking.transactionmanager.repository;

import com.tenx.universalbanking.transactionmanager.entity.AuthorisationId;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAuthorisations extends JpaRepository<Authorisations, AuthorisationId> {

  List<Authorisations> findByTsysAccountIdAndId_BankNetReferenceNumber(String tsysAccountId,
      String bankNetReferenceNumber);

  Optional<Authorisations> findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(String tsysAccountId, String bankNetReferenceNumber,
      String transactionType);

  Optional<Authorisations> findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
      String tsysAccountId,
      BigDecimal transactionAmount,
      LocalDate transactionDate,
      String authorisationCode);

  Optional<Authorisations> findByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode(
      String tsysAccountId,
      BigDecimal transactionAmount,
      String authorisationCode);

  Optional<Authorisations> findByTsysAccountIdAndId_TransactionDateAndId_AuthorisationCode(
      String tsysAccountId,
      LocalDate transactionDate,
      String authorisationCode);

  Optional<Authorisations> findByTsysAccountIdAndId_TransactionDateAndTransactionType(
      String tsysAccountId,
      LocalDate transactionDate,
      String transactionType);

  Optional<Authorisations> findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionAmountAndId_TransactionDateAndTransactionType(
      String tsysAccountId,
      String bankNetReferenceNumber, BigDecimal transactionAmount, LocalDate transactionDate,
      String transactionType);

  @Query(value = "select * from authorisations a where a.transaction_date <= :transactionDate and a.matched = :matched and a.expired = :expired", nativeQuery = true)
  List<Authorisations> findAuthorisationsBeforeDateAndNotExpiredAndNotMatched(
      @Param("transactionDate") LocalDate transactionDate,
      @Param("matched") boolean matched,
      @Param("expired") boolean expired);
}

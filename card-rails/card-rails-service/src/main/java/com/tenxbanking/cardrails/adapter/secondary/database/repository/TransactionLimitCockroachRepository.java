package com.tenxbanking.cardrails.adapter.secondary.database.repository;

import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionLimit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLimitCockroachRepository extends JpaRepository<TransactionLimit, UUID> {

  @Query("select sum(tl.amount) "
      + "from TransactionLimit tl "
      + "where tl.subscriptionKey = :subscriptionKey "
      + "and tl.isAtmWithdrawal is true "
      + "and tl.transactionDatetime >= :startInstant "
      + "and tl.transactionDatetime <= :endInstant")
  Optional<BigDecimal> getCurrentAtmWithdrawal(
      @Param("subscriptionKey") @NonNull final UUID subscriptionKey,
      @Param("startInstant") @NonNull final Instant startInstant,
      @Param("endInstant") @NonNull final Instant endInstant);
}

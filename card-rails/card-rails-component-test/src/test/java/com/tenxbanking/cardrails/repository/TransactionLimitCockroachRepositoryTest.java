package com.tenxbanking.cardrails.repository;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;

import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionLimit;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionLimitCockroachRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionLimitCockroachRepositoryTest extends BaseComponentTest {

  private static final UUID SUBSCRIPTION_KEY = UUID
      .fromString("391df9e4-ad44-43cc-8c7b-05122b3b02c8");
  private static final UUID ANOTHER_SUBSCRIPTION_KEY = UUID
      .fromString("c6b43092-150d-41f2-a054-ea397103a3fd");


  private static final Instant NOW = LocalDateTime.of(2019, 8, 11, 19, 40).toInstant(UTC);
  private static final Instant START_OF_DAY = LocalDateTime.of(2019, 8, 11, 0, 0).toInstant(UTC);
  private static final Instant END_OF_DAY = LocalDateTime.of(2019, 8, 11, 23, 59, 59, 999)
      .toInstant(UTC);
  private static final Instant TOMORROW = NOW.plus(Duration.ofDays(1));
  private static final Instant YESTERDAY = NOW.minus(Duration.ofDays(1));


  @Autowired
  private TransactionLimitCockroachRepository transactionLimitCockroachRepository;

  @Before
  public void before() {
    transactionLimitCockroachRepository.deleteAll();
  }

  @Test
  public void shouldGetCurrentAtmWithdrawalForASubscriptionWithoutTransactions() {
    //when
    Optional<BigDecimal> actual = transactionLimitCockroachRepository
        .getCurrentAtmWithdrawal(SUBSCRIPTION_KEY, START_OF_DAY, END_OF_DAY);

    //then
    assertThat(actual.isEmpty()).isTrue();
  }

  @Test
  public void shouldGetCurrentAtmWithdrawalForASubscription() {

    // a transaction that counts
    TransactionLimit transaction1 = getTransactionLimit(SUBSCRIPTION_KEY,
        new BigDecimal("10.01"), true, NOW);

    // a transaction that does not count (not atm withdrawal)
    TransactionLimit transaction2 = getTransactionLimit(SUBSCRIPTION_KEY,
        new BigDecimal("20.02"), false, NOW);

    // a transaction that does not count (different subscription key)
    TransactionLimit transaction3 = getTransactionLimit(ANOTHER_SUBSCRIPTION_KEY,
        new BigDecimal("30.03"), true, NOW);

    // a transaction that does not count (made yesterday)
    TransactionLimit transaction4 = getTransactionLimit(SUBSCRIPTION_KEY,
        new BigDecimal("40.04"), true, YESTERDAY);

    // a transaction that does not count (made tomorrow)
    TransactionLimit transaction5 = getTransactionLimit(SUBSCRIPTION_KEY,
        new BigDecimal("50.05"), true, TOMORROW);

    // a transaction that counts
    TransactionLimit transaction6 = getTransactionLimit(SUBSCRIPTION_KEY,
        new BigDecimal("60.06"), true, NOW.plus(Duration.ofMinutes(1)));

    //and
    transactionLimitCockroachRepository.saveAll(
        asList(transaction1, transaction2, transaction3, transaction4, transaction5, transaction6));

    //when
    Optional<BigDecimal> actual = transactionLimitCockroachRepository
        .getCurrentAtmWithdrawal(SUBSCRIPTION_KEY, START_OF_DAY, END_OF_DAY);

    //then
    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(new BigDecimal("70.07"));
  }

  private TransactionLimit getTransactionLimit(UUID subscriptionKey,
      BigDecimal amount,
      boolean isAtmWithdrawal,
      Instant transactionDateTime) {

    return TransactionLimit.builder()
        .id(UUID.randomUUID())
        .subscriptionKey(subscriptionKey)
        .amount(amount)
        .isAtmWithdrawal(isAtmWithdrawal)
        .transactionDatetime(transactionDateTime)
        .build();
  }
}

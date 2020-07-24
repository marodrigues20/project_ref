package com.tenxbanking.cardrails.adapter.secondary.database.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table
@Getter
@EqualsAndHashCode(exclude = {"id"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLimit {

  @Id
  @GeneratedValue(generator = "uuid_generator")
  @GenericGenerator(name = "uuid_generator", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, updatable = false)
  private UUID subscriptionKey;

  @Column(nullable = false, updatable = false)
  private BigDecimal amount;

  @Column(nullable = false, updatable = false)
  private boolean isAtmWithdrawal;

  @Column(nullable = false, updatable = false)
  private Instant transactionDatetime;
}

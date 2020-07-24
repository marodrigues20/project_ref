package com.tenxbanking.cardrails.adapter.secondary.database.model;

import java.math.BigDecimal;
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
@Table(name = "fee")
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeEntity {

  @Id
  @GeneratedValue(generator = "uuid_generator")
  @GenericGenerator(name = "uuid_generator", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column
  private BigDecimal amount;

  @Column
  private String description;

  @Column
  private String feeCurrencyCode;

  @Column
  private String status;

  @Column
  private BigDecimal taxAmount;

  @Column
  private String parentTransactionId;

  @Column
  private String taxTransactionId;

  @Column
  private String statementDescription;

  @Column
  private String transactionCode;

  @Column
  private String transactionCorrelationId;

  @Column
  private String transactionDate;

  @Column
  private String transactionId;

  @Column
  private String valueDateTime;

}

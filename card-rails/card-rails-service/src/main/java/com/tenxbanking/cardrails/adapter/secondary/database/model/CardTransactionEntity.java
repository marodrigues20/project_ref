package com.tenxbanking.cardrails.adapter.secondary.database.model;

import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "card_transaction")
@Getter
@EqualsAndHashCode(exclude = {"id"})
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CardTransactionEntity {

  @Id
  @GeneratedValue(generator = "uuid_generator")
  @GenericGenerator(name = "uuid_generator", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, updatable = false, length = 64)
  private String transactionId;

  @Column(nullable = false, updatable = false, length = 64)
  private String correlationId;

  @Column(nullable = false, updatable = false)
  private UUID subscriptionKey;

  @Column(nullable = false, updatable = false)
  private UUID partyKey;

  @Column(nullable = false, updatable = false)
  private UUID productKey;

  @Column(nullable = false, updatable = false)
  private String tenantKey;

  @Column(nullable = false, updatable = false)
  private Instant createdDate;

  @OneToMany(targetEntity = TransactionMessageEntity.class,
      mappedBy = "cardTransaction",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER)
  private List<TransactionMessageEntity> transactionMessages;

  @Column(updatable = false, length = 64)
  private String authCode;

  @Column(updatable = false, length = 64)
  @Enumerated(EnumType.STRING)
  private AuthResponseCode authResponseCode;

  @Column(updatable = false, length = 32)
  private String cardId;

  @Column(updatable = false, length = 32)
  private String banknetReferenceNumber;

  @Column(updatable = false, length = 32)
  @Enumerated(EnumType.STRING)
  private CardTransactionType cardTransactionType;

  @Column(updatable = false, length = 32)
  private String retrievalReferenceNumber;

  //@Column(nullable = false)
  @Column
  private BigDecimal transactionAmount;

  //Column(nullable = false)
  @Column
  private String transactionCurrency;

  @Column(nullable = false)
  private BigDecimal billingAmount;

  @Column(nullable = false)
  private String billingCurrency;

  @Column
  private BigDecimal settlementAmount;

  @Column
  private String settlementCurrency;

  @Column
  private BigDecimal reversalTransactionAmount;

  @Column
  private String reversalTransactionCurrency;

  @Column
  private BigDecimal reversalSettlementAmount;

  @Column
  private String reversalSettlementCurrency;

  @Column
  private BigDecimal reversalBillingAmount;

  @Column
  private String reversalBillingCurrency;

  @Column
  private String merchantCategoryCode;

  @Column
  private String accountQualifier;

  @Column(nullable = false)
  private String processingCode;

  @Column(nullable = false)
  private BigDecimal conversionRate;

  @Column
  private String cardExpiryDate;

  @Column
  private String pointOfServiceEntryMode;

  @Column
  private String pointOfServiceConditionCode;

  @Column
  private String networkId;

  @Column
  private String cardAcceptorCountryCode;

  @Column
  private BigDecimal updatedBalance;

  @OneToOne
  @JoinColumn(name = "fee_id")
  private FeeEntity fee;

  @Column
  private boolean isSuccess;

  @Column(length = 64)
  @Enumerated(EnumType.STRING)
  private PaymentMethodType paymentMethodType;

}

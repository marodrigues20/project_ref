package com.tenxbanking.cardrails.adapter.secondary.database.model;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "transaction_message")
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionMessageEntity {

  @Id
  @GeneratedValue(generator = "uuid_generator")
  @GenericGenerator(name = "uuid_generator", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_transaction_id", nullable = false)
  private CardTransactionEntity cardTransaction;

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb", nullable = false, updatable = false)
  private TransactionMessage transactionMessage;


}

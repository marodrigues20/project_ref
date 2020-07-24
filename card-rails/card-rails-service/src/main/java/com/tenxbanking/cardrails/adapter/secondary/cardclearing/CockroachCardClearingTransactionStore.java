package com.tenxbanking.cardrails.adapter.secondary.cardclearing;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.cardclearing.util.Cain003MessageBuilder;
import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionMessageEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionMessageCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.transformer.CardClearingTransactionDomainToEntityTransformer;
import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import com.tenxbanking.cardrails.domain.port.store.CardClearingTransactionStore;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class CockroachCardClearingTransactionStore implements CardClearingTransactionStore {

  private final CardTransactionCockroachRepository cardTransactionCockroachRepository;
  private final CardClearingTransactionDomainToEntityTransformer domainToEntityTransformer;
  private final TransactionMessageCockroachRepository transactionMessageCockroachRepository;
  private final Cain003MessageBuilder cain003MessageBuilder;


  @Retryable
  @Transactional
  @Override
  public void save(@NonNull final CardClearing cardClearing) {

    final TransactionMessage transactionMessages = cain003MessageBuilder.create(cardClearing);
    CardTransactionEntity entity = domainToEntityTransformer.transform(cardClearing);
    CardTransactionEntity savedEntity = cardTransactionCockroachRepository.save(entity);
    TransactionMessageEntity transactionMessageEntity = transactionMessageEntity(savedEntity,
        transactionMessages);
    transactionMessageCockroachRepository.save(transactionMessageEntity);

  }

  private TransactionMessageEntity transactionMessageEntity(CardTransactionEntity entity,
      TransactionMessage transactionMessage) {
    return TransactionMessageEntity
        .builder()
        .cardTransaction(entity)
        .transactionMessage(transactionMessage)
        .build();
  }
}

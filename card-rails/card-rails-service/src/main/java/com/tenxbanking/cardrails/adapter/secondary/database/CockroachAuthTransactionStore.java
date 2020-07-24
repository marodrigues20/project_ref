package com.tenxbanking.cardrails.adapter.secondary.database;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionMessageEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionMessageCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.transformer.AuthTransactionDomainToEntityTransformer;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.CardAuthTransactionMessageCreator;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.ReservationConfirmationTransactionMessageCreator;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.port.store.AuthTransactionStore;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class CockroachAuthTransactionStore implements AuthTransactionStore {

  private final CardTransactionCockroachRepository cardTransactionCockroachRepository;
  private final TransactionMessageCockroachRepository transactionMessageCockroachRepository;
  private final AuthTransactionDomainToEntityTransformer domainToEntityTransformer;
  private final CardAuthTransactionMessageCreator messageMapper;
  private final ReservationConfirmationTransactionMessageCreator reservationConfirmationTransactionMessageCreator;

  @Retryable
  @Transactional
  @Override
  public void save(@NonNull final AuthTransaction cardTransaction) {
    List<TransactionMessage> transactionMessages = getTransactionMessages(cardTransaction);
    CardTransactionEntity entity = domainToEntityTransformer.transform(cardTransaction);
    CardTransactionEntity savedEntity = cardTransactionCockroachRepository.save(entity);
    transactionMessageCockroachRepository
        .saveAll(transactionMessages
            .stream()
            .map(message -> transactionMessageEntity(savedEntity, message))
            .collect(Collectors.toList()));
  }

  private TransactionMessageEntity transactionMessageEntity(CardTransactionEntity entity, TransactionMessage transactionMessage) {
    return TransactionMessageEntity
        .builder()
        .cardTransaction(entity)
        .transactionMessage(transactionMessage)
        .build();
  }

  private List<TransactionMessage> getTransactionMessages(AuthTransaction cardAuth) {
    final TransactionMessage transactionMessage = messageMapper.create(cardAuth);
    final TransactionMessage confirmationMessage = reservationConfirmationTransactionMessageCreator.create(transactionMessage, cardAuth.getCain002());
    return List.of(transactionMessage, confirmationMessage);
  }

}

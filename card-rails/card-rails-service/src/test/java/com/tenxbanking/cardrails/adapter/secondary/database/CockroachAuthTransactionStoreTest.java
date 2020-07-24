package com.tenxbanking.cardrails.adapter.secondary.database;

import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionMessageEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.TransactionMessageCockroachRepository;
import com.tenxbanking.cardrails.adapter.secondary.database.transformer.AuthTransactionDomainToEntityTransformer;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.CardAuthTransactionMessageCreator;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.ReservationConfirmationTransactionMessageCreator;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class CockroachAuthTransactionStoreTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();

  @Mock
  private CardTransactionCockroachRepository cardTransactionCockroachRepository;
  @Mock
  private TransactionMessageCockroachRepository transactionMessageCockroachRepository;
  @Mock
  private AuthTransactionDomainToEntityTransformer entityTransformer;
  @Mock
  private CardAuthTransactionMessageCreator messageMapper;
  @Mock
  private ReservationConfirmationTransactionMessageCreator reservationConfirmationTransactionMessageCreator;
  @InjectMocks
  private CockroachAuthTransactionStore underTest;

  @Mock
  private Cain001 cain001;
  @Mock
  private Cain002 cain002;

  @Mock
  private TransactionMessage transactionMessage;
  @Mock
  private TransactionMessage confirmationMessage;

  @Mock
  private CardTransactionEntity cardTransactionEntity;
  @Mock
  private CardTransactionEntity savedCardTransactionEntity;

  private CardAuth cardAuth;

  @BeforeEach
  void setup() {
    cardAuth = cardAuth();
  }

  @Test
  void save_transformsToEntities() {

    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(reservationConfirmationTransactionMessageCreator.create(transactionMessage, cain002)).thenReturn(confirmationMessage);

    underTest.save(cardAuth);

    verify(messageMapper).create(cardAuth);
    verify(reservationConfirmationTransactionMessageCreator).create(transactionMessage, cain002);
    verify(entityTransformer).transform(cardAuth);
  }

  @Test
  void save_savesEntities() {

    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(reservationConfirmationTransactionMessageCreator.create(transactionMessage, cain002)).thenReturn(confirmationMessage);
    when(entityTransformer.transform(cardAuth)).thenReturn(cardTransactionEntity);
    when(cardTransactionCockroachRepository.save(cardTransactionEntity)).thenReturn(savedCardTransactionEntity);

    underTest.save(cardAuth);

    verify(cardTransactionCockroachRepository).save(cardTransactionEntity);
    verify(transactionMessageCockroachRepository).saveAll(List.of(
        transactionMessageEntity(savedCardTransactionEntity, transactionMessage),
        transactionMessageEntity(savedCardTransactionEntity, confirmationMessage)));
  }

  private CardAuth cardAuth() {
    return CardAuth
        .builder()
        .cardId(CARD_ID)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .cain001(cain001)
        .cain002(cain002)
        .partyKey(PARTY_KEY)
        .tenantKey(TENANT_KEY)
        .productKey(PRODUCT_KEY)
        .build();
  }

  private TransactionMessageEntity transactionMessageEntity(CardTransactionEntity entity, TransactionMessage transactionMessage) {
    return TransactionMessageEntity
        .builder()
        .cardTransaction(entity)
        .transactionMessage(transactionMessage)
        .build();
  }

}
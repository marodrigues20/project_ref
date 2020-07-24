package com.tenxbanking.cardrails.adapter.secondary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.ledger.LedgerManagerClient;
import com.tenxbanking.cardrails.adapter.secondary.ledger.RestAuthTransactionSender;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.ReserveResponse;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.CardAuthTransactionMessageCreator;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.ReservationConfirmationTransactionMessageCreator;
import com.tenxbanking.cardrails.domain.exception.BalanceReservationException;
import com.tenxbanking.cardrails.domain.exception.ReservationConfirmationException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RestAuthTransactionSenderTest {

  @InjectMocks
  private RestAuthTransactionSender messageHandler;
  @Mock
  private LedgerManagerClient ledgerClient;
  @Mock
  private CardAuthTransactionMessageCreator messageMapper;
  @Mock
  private ReservationConfirmationTransactionMessageCreator confirmationMessageCreator;

  @Mock
  private Cain001 cain001;
  @Mock
  private CardAuth cardAuth;

  @Test
  void shouldCallReserveOnLedgerManager() {

    when(cardAuth.getCain001()).thenReturn(cain001);
    TransactionMessage transactionMessage = new TransactionMessage();

    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(ledgerClient.reserve(transactionMessage)).thenReturn(ResponseEntity.ok().build());

    messageHandler.reserve(cardAuth);

    verify(ledgerClient).reserve(transactionMessage);
  }

  @Test
  void shouldReturnResultFromResponse() {
    final TransactionMessage transactionMessage = new TransactionMessage();
    ResponseEntity.ok(new ReserveResponse(BigDecimal.ONE, true));


    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.getCurrency()).thenReturn(Currency.getInstance("GBP"));
    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(ledgerClient.reserve(transactionMessage)).thenReturn(ResponseEntity.ok(new ReserveResponse(BigDecimal.ONE, true)));

    Cain002 cain002 = messageHandler.reserve(cardAuth);

    assertThat(cain002.isSuccess()).isTrue();
    assertThat(cain002.getUpdatedBalance().getAmount()).isEqualTo(BigDecimal.ONE);
  }

  @Test
  void shouldReturnResultFromWhenResponseBodyIsNull() {
    TransactionMessage transactionMessage = new TransactionMessage();

    when(cardAuth.getCain001()).thenReturn(cain001);
    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(ledgerClient.reserve(transactionMessage)).thenReturn(ResponseEntity.ok().build());

    Cain002 cain002 = messageHandler.reserve(cardAuth);

    assertThat(cain002.isSuccess()).isFalse();
    assertThat(cain002.getUpdatedBalance()).isNull();
    verify(ledgerClient, never()).confirmReservation(any());
  }

  @Test
  void shouldNotConfirmReservationWhenReserveResultFailed() {
    TransactionMessage transactionMessage = new TransactionMessage();

    when(cardAuth.getCain001()).thenReturn(cain001);
    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(ledgerClient.reserve(transactionMessage)).thenReturn(ResponseEntity.ok(new ReserveResponse(BigDecimal.ONE, false)));

    Cain002 cain002 = messageHandler.reserve(cardAuth);

    assertThat(cain002.isSuccess()).isFalse();
    assertThat(cain002.getUpdatedBalance()).isNull();
    verify(ledgerClient, never()).confirmReservation(any());
  }

  @Test
  void shouldCallLedgerToConfirmReservationWhenSuccess() {
    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionMessage cain002TransactionMessage = new TransactionMessage();

    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(cain001.getCurrency()).thenReturn(Currency.getInstance("GBP"));
    when(confirmationMessageCreator.create(eq(transactionMessage), any(Cain002.class)))
        .thenReturn(cain002TransactionMessage);
    when(ledgerClient.reserve(transactionMessage))
        .thenReturn(ResponseEntity.ok(new ReserveResponse(BigDecimal.ONE, true)));
    when(cardAuth.getCain001()).thenReturn(cain001);

    Cain002 cain002 = messageHandler.reserve(cardAuth);

    assertThat(cain002.isSuccess()).isTrue();
    assertThat(cain002.getUpdatedBalance().getAmount()).isOne();
    verify(ledgerClient).confirmReservation(cain002TransactionMessage);
  }

  @Test
  void shouldThrowBalanceReservationExceptionWhenErrorClient() {
    TransactionMessage transactionMessage = new TransactionMessage();

    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(ledgerClient.reserve(transactionMessage)).thenThrow(new RuntimeException());

    assertThatThrownBy(() ->
        messageHandler.reserve(cardAuth)
    ).isInstanceOf(BalanceReservationException.class);
  }

  @Test
  void shouldThrowReservationConfirmationExceptionWhenErrorClient() {
    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionMessage cain002TransactionMessage = new TransactionMessage();

    when(cardAuth.getCain001()).thenReturn(cain001);
    when(cain001.getCurrency()).thenReturn(Currency.getInstance("GBP"));
    when(messageMapper.create(cardAuth)).thenReturn(transactionMessage);
    when(confirmationMessageCreator.create(eq(transactionMessage), any(Cain002.class)))
        .thenReturn(cain002TransactionMessage);
    when(ledgerClient.reserve(transactionMessage))
        .thenReturn(ResponseEntity.ok(new ReserveResponse(BigDecimal.ONE, true)));
    when(ledgerClient.confirmReservation(cain002TransactionMessage)).thenThrow(new RuntimeException());

    assertThatThrownBy(() ->
        messageHandler.reserve(cardAuth)
    ).isInstanceOf(ReservationConfirmationException.class);
  }

}
package com.tenxbanking.cardrails.adapter.secondary.ledger;

import static java.util.Objects.isNull;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.ReserveResponse;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.CardAuthTransactionMessageCreator;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.ReservationConfirmationTransactionMessageCreator;
import com.tenxbanking.cardrails.domain.exception.BalanceReservationException;
import com.tenxbanking.cardrails.domain.exception.ReservationConfirmationException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.port.sender.AuthReserveTransactionSender;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class RestAuthTransactionSender implements AuthReserveTransactionSender {

  private final LedgerManagerClient ledgerClient;
  private final CardAuthTransactionMessageCreator messageMapper;
  private final ReservationConfirmationTransactionMessageCreator reservationConfirmationTransactionMessageCreator;

  @Override
  public Cain002 reserve(@NonNull AuthTransaction authTransaction) {

    TransactionMessage message = messageMapper.create(authTransaction);
    ResponseEntity<ReserveResponse> reserve;

    log.debug("Posting to ledger manager reserve endpoint with transactionMessage={} ", message);
    try {
      reserve = ledgerClient.reserve(message);
    } catch (Exception ex) {
      log.error("Failed to reserve balance on ledger", ex);
      throw new BalanceReservationException("Failed to reserve balance for card transaction");
    }

    Cain002 cain002 = createResult(reserve.getBody(), authTransaction.getCain001());

    if (cain002.isSuccess()) {
      TransactionMessage reservationConfirmation = reservationConfirmationTransactionMessageCreator
          .create(message, cain002);

      try {
        ledgerClient.confirmReservation(reservationConfirmation);
      } catch (Exception ex) {
        log.error("Failed to reserve balance on ledger", ex);
        throw new ReservationConfirmationException(
            "Failed to confirm the balance reservation for card transaction");
      }

    }

    return cain002;
  }

  private Cain002 createResult(ReserveResponse reserveResponse, Cain001 cain001) {
    return isFailure(reserveResponse) ? Cain002.unsuccessful(cain001) : Cain002.successful(cain001, reserveResponse.getAvailableBalance());
  }

  private boolean isFailure(ReserveResponse reserveResponse) {
    return isNull(reserveResponse) || !reserveResponse.isResult();
  }

}

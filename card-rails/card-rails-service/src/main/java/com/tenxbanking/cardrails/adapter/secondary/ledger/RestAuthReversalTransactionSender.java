package com.tenxbanking.cardrails.adapter.secondary.ledger;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.BalanceResponse;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.CardAuthTransactionMessageCreator;
import com.tenxbanking.cardrails.domain.exception.BalanceReservationException;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuthReversal;
import com.tenxbanking.cardrails.domain.port.sender.AuthReversalTransactionSender;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestAuthReversalTransactionSender implements AuthReversalTransactionSender {

  private final LedgerManagerClient ledgerClient;
  private final CardAuthTransactionMessageCreator messageMapper;

  @Autowired
  public RestAuthReversalTransactionSender(
      LedgerManagerClient ledgerClient,
      CardAuthTransactionMessageCreator messageMapper) {
    this.ledgerClient = ledgerClient;
    this.messageMapper = messageMapper;
  }

  @Override
  public Cain002 reverse(@NonNull CardAuthReversal cardAuthReversal) {
    try {
      TransactionMessage message = messageMapper.create(cardAuthReversal);
      log.debug("Posting to ledger manager reverse endpoint with transactionMessage={} ", message);
      ResponseEntity<BalanceResponse> reversal = ledgerClient.reversal(message);
      return createResult(reversal.getBody(), cardAuthReversal.getCain001());
    } catch (Exception ex) {
      log.error("Failed posting reversal on ledger", ex);
      throw new BalanceReservationException(String.format("Failed to post cardAuthReversal=%s", cardAuthReversal));
    }
  }

  private Cain002 createResult(BalanceResponse balanceResponse, Cain001 cain001) {
    return buildCain002(balanceResponse, cain001);
  }

  private Cain002 buildCain002(BalanceResponse balanceResponse, Cain001 cain001) {
    return Cain002.successful(cain001, balanceResponse.getBalance().getAmount().getValue());
  }
}

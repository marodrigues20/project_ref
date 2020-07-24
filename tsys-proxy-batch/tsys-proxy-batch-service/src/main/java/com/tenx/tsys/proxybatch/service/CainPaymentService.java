package com.tenx.tsys.proxybatch.service;

import com.tenx.tsys.proxybatch.client.debitcardmanager.model.DebitCardResponse;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessage;
import java.text.ParseException;
import org.springframework.stereotype.Service;

@FunctionalInterface
@Service
public interface CainPaymentService {

  public TransactionMessage buildMessage(String message, DebitCardResponse debitCardResponse)
      throws ParseException;
}

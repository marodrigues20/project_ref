package com.tenx.tsys.proxybatch.service.cain005;

import com.tenx.tsys.proxybatch.client.debitcardmanager.model.DebitCardResponse;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessage;
import com.tenx.tsys.proxybatch.service.CainPaymentService;
import com.tenx.tsys.proxybatch.service.cain005.helper.Cain005MessageBuilder;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "cain005PaymentService")
public class Cain005PaymentService implements CainPaymentService {

  @Autowired
  private Cain005MessageBuilder cain005MessageBuilder;

  @Override
  public TransactionMessage buildMessage(String message, DebitCardResponse debitCardResponse)
      throws ParseException {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage = cain005MessageBuilder.setTransactionMessageHeader(transactionMessage);
    transactionMessage = cain005MessageBuilder
        .setTransactionMessage(transactionMessage, message, debitCardResponse);
    return transactionMessage;
  }
}



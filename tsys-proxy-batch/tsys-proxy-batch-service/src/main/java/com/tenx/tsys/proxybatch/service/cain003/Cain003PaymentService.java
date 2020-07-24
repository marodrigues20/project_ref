package com.tenx.tsys.proxybatch.service.cain003;

import com.tenx.tsys.proxybatch.client.debitcardmanager.model.DebitCardResponse;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessage;
import com.tenx.tsys.proxybatch.service.CainPaymentService;
import com.tenx.tsys.proxybatch.service.cain003.helper.Cain003MessageBuilder;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "cain003PaymentService")
public class Cain003PaymentService implements CainPaymentService {

  @Autowired
  private Cain003MessageBuilder cain003MessageBuilder;

  @Override
  public TransactionMessage buildMessage(String message, DebitCardResponse debitCardResponse)
      throws ParseException {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage = cain003MessageBuilder.setTransactionMessageHeader(transactionMessage);
    transactionMessage = cain003MessageBuilder
        .setTransactionMessage(transactionMessage, message, debitCardResponse);
    return transactionMessage;
  }
}

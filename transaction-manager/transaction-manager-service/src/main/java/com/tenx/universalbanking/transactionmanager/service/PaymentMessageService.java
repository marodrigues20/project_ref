package com.tenx.universalbanking.transactionmanager.service;

import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;

public interface PaymentMessageService {

  PaymentMessageTypeEnum getType();

  Authorisations getAuthorisations(TransactionMessage message);

  void setTransactionType(PaymentMessage paymentMessage);
}

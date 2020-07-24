package com.tenx.universalbanking.transactionmanager.service;

import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import javax.servlet.http.HttpServletRequest;

public interface TransactionMessageService {

  TransactionMessageTypeEnum getType();

  PaymentProcessResponse process(TransactionMessage message, HttpServletRequest request);
}

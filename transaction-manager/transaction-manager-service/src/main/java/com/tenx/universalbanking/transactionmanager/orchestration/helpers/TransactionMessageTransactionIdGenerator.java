package com.tenx.universalbanking.transactionmanager.orchestration.helpers;

import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionMessageTransactionIdGenerator {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private GeneratorUtil generatorUtil;

  public void addTransactionId(PaymentMessage paymentMessage, String transactionId) {
    logger.debug("Adding transactionId: " + transactionId);
    paymentMessage.getAdditionalInfo()
        .put(PaymentMessageAdditionalInfoEnum.TRANSACTION_ID.name(), transactionId);
  }

  public void addTransactionId(PaymentMessage paymentMessage) {
    String transactionId = generatorUtil.generateRandomKey();
    logger.debug("Generated transactionId: " + transactionId);
    addTransactionId(paymentMessage, transactionId);
  }

}

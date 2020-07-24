package com.tenx.universalbanking.transactionmanager.orchestration;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import org.slf4j.Logger;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageSender;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;

abstract class TransactionMessageHandlerBase {

  private static final Logger logger = getLogger(TransactionMessageHandlerBase.class);
  private final GeneratorUtil generatorUtil;
  private final TransactionMessageSender transactionMessageSender;

  TransactionMessageHandlerBase(GeneratorUtil lionGeneratorUtil,
      TransactionMessageSender transactionMessageSender) {
    this.generatorUtil = lionGeneratorUtil;
    this.transactionMessageSender = transactionMessageSender;
  }

  abstract TransactionMessageTypeEnum handlesMessageOfType();

  public void handleMessage(TransactionMessage transactionMessage) {
    addTransactionId(transactionMessage);
    addCorrelationId(transactionMessage);

    //this method is added as a hook in cases where the handler needs to enrich the transaction message further.
    //override this method in the handler that extends from this class.
    enrich(transactionMessage);

    transactionMessageSender.send(transactionMessage);
  }

  private void enrich(TransactionMessage transactionMessage) {
    logger.debug("Processing transaction with correlation ID {}",
        transactionMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()));
  }

  TransactionMessage addCorrelationTransactionIDs(TransactionMessage transactionMessage){
    addCorrelationId(transactionMessage);
    addTransactionId(transactionMessage);
    return transactionMessage;
  }

  private void addCorrelationId(TransactionMessage transactionMessage) {
    transactionMessage.getAdditionalInfo().computeIfAbsent(
        TRANSACTION_CORRELATION_ID.name(), key -> generatorUtil.generateRandomKey());
  }

  private void addTransactionId(TransactionMessage transactionMessage) {
    transactionMessage.getMessages().forEach(paymentMessage ->
        paymentMessage.getAdditionalInfo().computeIfAbsent(
            TRANSACTION_ID.name(), key -> generatorUtil.generateRandomKey()));
  }
}

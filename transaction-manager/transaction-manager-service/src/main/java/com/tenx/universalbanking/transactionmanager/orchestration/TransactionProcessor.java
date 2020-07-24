package com.tenx.universalbanking.transactionmanager.orchestration;

import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import com.tenx.universalbanking.transactionmanager.exception.NoValidTransactionHandlerException;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;

@Component
public class TransactionProcessor {

  private final Logger logger = getLogger(TransactionProcessor.class);
  private final Map<TransactionMessageTypeEnum, TransactionMessageHandler> transactionMessageTypeEnumTransactionMessageHandlerMap;

  TransactionProcessor(List<TransactionMessageHandler> transactionMessageHandlers) {
    this.transactionMessageTypeEnumTransactionMessageHandlerMap = transactionMessageHandlers.stream()
        .collect(toMap(TransactionMessageHandler::handlesMessageOfType, handler -> handler));
  }

  public void handle(TransactionMessage transactionMessage) {

    TransactionMessageTypeEnum transactionMessageTypeEnum = TransactionMessageTypeEnum
        .valueOf(transactionMessage.getHeader().getType());

    logger.debug("Started processing the transaction: {}", transactionMessageTypeEnum);

    TransactionMessageHandler transactionMessageHandler = transactionMessageTypeEnumTransactionMessageHandlerMap
        .get(transactionMessageTypeEnum);

    if (transactionMessageHandler == null) {
      throw new NoValidTransactionHandlerException(
          String.format("No valid handler found for payment message type %s", transactionMessageTypeEnum));
    }

    transactionMessageHandler.handleMessage(transactionMessage);
  }
}

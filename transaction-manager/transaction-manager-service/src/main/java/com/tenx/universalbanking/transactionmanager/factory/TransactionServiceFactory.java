package com.tenx.universalbanking.transactionmanager.factory;

import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageTypeException;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Component
public class TransactionServiceFactory {

  private final MessageServiceProcessorHelper processorHelper;

  private final Map<TransactionMessageTypeEnum, TransactionMessageService> transactionMessageServiceMap;

  @Autowired
  public TransactionServiceFactory(
      MessageServiceProcessorHelper messageServiceProcessorHelper,
      List<TransactionMessageService> transactionMessageServices) {
    this.processorHelper = messageServiceProcessorHelper;
    transactionMessageServiceMap =
        transactionMessageServices.stream().collect(toMap(TransactionMessageService::getType,
            transactionMessageService -> transactionMessageService));
  }

  public TransactionMessageService getTransactionMessageService(TransactionMessage message) {

    TransactionMessageTypeEnum messageType = TransactionMessageTypeEnum
        .valueOf(message.getHeader().getType());

    processorHelper.addPaymentMethodType(message);

    return ofNullable(transactionMessageServiceMap.get(messageType)).orElseThrow(() -> invalidTransactionMessageTypeException(messageType));
  }

  private InvalidTransactionMessageTypeException invalidTransactionMessageTypeException(
      TransactionMessageTypeEnum type) {
    return new InvalidTransactionMessageTypeException(
        "Transaction Type " + type + " is not supported");
  }
}

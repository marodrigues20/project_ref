package com.tenx.universalbanking.transactionmanager.factory;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageTypeException;
import com.tenx.universalbanking.transactionmanager.service.CardAuthService;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardAuthServiceFactory {

  private final Map<TransactionMessageTypeEnum, CardAuthService> cardAuthServiceMap;

  @Autowired
  public CardAuthServiceFactory(List<CardAuthService> cardAuthServiceServices) {
    cardAuthServiceMap =
        cardAuthServiceServices.stream().collect(toMap(CardAuthService::getType,
            cardAuthService -> cardAuthService));
  }

  public CardAuthService getCardAuthService(TransactionMessage message) {

    TransactionMessageTypeEnum messageType = TransactionMessageTypeEnum
        .valueOf(message.getHeader().getType());

    return ofNullable(cardAuthServiceMap.get(messageType)).orElseThrow(() -> invalidTransactionMessageTypeException(messageType));
  }

  private InvalidTransactionMessageTypeException invalidTransactionMessageTypeException(
      TransactionMessageTypeEnum type) {
    return new InvalidTransactionMessageTypeException(
        "Transaction Type " + type + " is not supported");
  }

}

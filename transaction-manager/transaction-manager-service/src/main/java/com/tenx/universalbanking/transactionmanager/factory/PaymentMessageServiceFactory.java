package com.tenx.universalbanking.transactionmanager.factory;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageTypeException;
import com.tenx.universalbanking.transactionmanager.service.PaymentMessageService;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentMessageServiceFactory {

  private final Map<PaymentMessageTypeEnum, PaymentMessageService> paymentMessageServiceMap;

  @Autowired
  public PaymentMessageServiceFactory(List<PaymentMessageService> paymentMessageServices) {
    paymentMessageServiceMap =
        paymentMessageServices.stream().collect(toMap(PaymentMessageService::getType,
            paymentMessageService -> paymentMessageService));
  }

  public PaymentMessageService getPaymentMessageService(PaymentMessage message) {

    PaymentMessageTypeEnum messageType = PaymentMessageTypeEnum
        .valueOf(message.getType());

    return ofNullable(paymentMessageServiceMap.get(messageType))
        .orElseThrow(() -> invalidTransactionMessageTypeException(messageType));
  }

  private InvalidTransactionMessageTypeException invalidTransactionMessageTypeException(
      PaymentMessageTypeEnum type) {
    return new InvalidTransactionMessageTypeException(
        "Payment Type " + type + " is not supported");
  }
}

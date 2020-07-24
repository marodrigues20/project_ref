package com.tenx.universalbanking.transactionmanager.service.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mapstruct.Mapper;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.PaymentMessage;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessage;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessageHeader;

@Mapper(componentModel = "spring")
public interface PPTransactionMessageMapper {
  
  TransactionMessageHeader toTransactionMessageHeaderMap(
      com.tenx.universalbanking.transactionmessage.TransactionMessageHeader header);

  List<PaymentMessage> toPaymentMessageList(
      List<com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage> paymentMessageList);

  PaymentMessage toClientPaymentMessage(
      com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage paymentMessage);

  TransactionMessage toPPTransactionMessage(
      com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage);

  com.tenx.universalbanking.transactionmessage.TransactionMessage toTMTransactionMessage(
      TransactionMessage transactionMessage);

  @SuppressWarnings("unchecked")
  default Map<String,Object> toAdditionalInfoMap(Object additionalInfoObject) {
    if (!Objects.isNull(additionalInfoObject) && additionalInfoObject instanceof Map) {
      return (Map<String, Object>) additionalInfoObject;
    }
    return Collections.EMPTY_MAP;
    }
  

  com.tenx.universalbanking.transactionmessage.TransactionMessage toTransactionMessage(
      TransactionMessage transactionMessage);
}

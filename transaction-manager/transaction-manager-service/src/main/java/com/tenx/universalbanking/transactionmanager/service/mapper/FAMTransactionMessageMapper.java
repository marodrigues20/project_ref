package com.tenx.universalbanking.transactionmanager.service.mapper;

import com.tenx.universalbanking.fundaccountmanager.client.model.PaymentMessage;
import com.tenx.universalbanking.fundaccountmanager.client.model.TransactionMessage;
import com.tenx.universalbanking.fundaccountmanager.client.model.TransactionMessageHeader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FAMTransactionMessageMapper {

  TransactionMessageHeader toTransactionMessageHeaderMap(
      com.tenx.universalbanking.transactionmessage.TransactionMessageHeader transactionMessageHeader);

  List<PaymentMessage> toPaymentMessageList(
      List<com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage> paymentMessage);

  PaymentMessage toClientPaymentMessage(
      com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage paymentMessage);

  TransactionMessage toFAMTransactionMessage(
      com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage);

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

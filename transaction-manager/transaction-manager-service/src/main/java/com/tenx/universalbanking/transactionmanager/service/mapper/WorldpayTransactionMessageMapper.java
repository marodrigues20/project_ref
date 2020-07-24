package com.tenx.universalbanking.transactionmanager.service.mapper;

import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.PaymentMessage;
import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.TransactionMessageHeader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorldpayTransactionMessageMapper {

  TransactionMessageHeader fromWorldpayTransactionMessageHeader(
      com.tenx.universalbanking.transactionmessage.TransactionMessageHeader header);

  PaymentMessage fromWorldpayPaymentMessage(
      com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage paymentMessage);

  com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.TransactionMessage toWorldPayAdapterClientTransactionMessage(
      com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage);

  default com.tenx.universalbanking.transactionmessage.TransactionMessage clientTmToTmUtl(
      com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.TransactionMessage transactionMessage) {

    com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessageutil = new com.tenx.universalbanking.transactionmessage.TransactionMessage();
    if (transactionMessage.getAdditionalInfo() instanceof Map) {
      transactionMessageutil.setAdditionalInfo((Map) transactionMessage.getAdditionalInfo());
    }

    List<com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage> paymentMessageList = new ArrayList<>();
    paymentMessageList.add(toPaymenMessage(transactionMessage.getMessages().get(0)));
    transactionMessageutil.setMessages(paymentMessageList);

    transactionMessageutil.setHeader(toTransactionMessageheader(transactionMessage.getHeader()));

    return transactionMessageutil;
  }

  default com.tenx.universalbanking.transactionmessage.TransactionMessageHeader toTransactionMessageheader(
      com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.TransactionMessageHeader transactionMessageHeader) {
    com.tenx.universalbanking.transactionmessage.TransactionMessageHeader headerUtil = new com.tenx.universalbanking.transactionmessage.TransactionMessageHeader();

    headerUtil.setType(transactionMessageHeader.getType());
    headerUtil.setUrl(transactionMessageHeader.getUrl());
    return headerUtil;

  }

  default com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage toPaymenMessage(
      com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.PaymentMessage paymentMessage) {
    com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage toPaymentMessage = new com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage();
    if (paymentMessage.getAdditionalInfo() instanceof Map) {
      toPaymentMessage.setAdditionalInfo((Map) paymentMessage.getAdditionalInfo());
    }
    if (paymentMessage.getMessage() instanceof Map) {
      toPaymentMessage.setMessage((Map) paymentMessage.getMessage());
    }
    toPaymentMessage.setType(paymentMessage.getType());
    return toPaymentMessage;
  }
}

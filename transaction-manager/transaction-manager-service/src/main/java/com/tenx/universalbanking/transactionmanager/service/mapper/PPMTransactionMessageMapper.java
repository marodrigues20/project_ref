package com.tenx.universalbanking.transactionmanager.service.mapper;

import com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.PaymentMessage;
import com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.TransactionMessage;
import com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.TransactionMessageHeader;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface PPMTransactionMessageMapper {

  TransactionMessageHeader toClientTransactionMessageHeader(
          com.tenx.universalbanking.transactionmessage.TransactionMessageHeader header);

  PaymentMessage toClientPaymentMessage(
          com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage paymentMessage);

  List<PaymentMessage> toClientPaymentMessageList(
          List<com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage> paymentMessageList);

  TransactionMessage toClientTransactionMessage(
          com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage);


}

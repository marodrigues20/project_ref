package com.tenx.universalbanking.transactionmanager.service.mapper;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentMessage;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessageHeader;
import java.util.List;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface PDFTransactionMessageMapper {

  TransactionMessageHeader toClientTransactionMessageHeader(
      com.tenx.universalbanking.transactionmessage.TransactionMessageHeader header);

  PaymentMessage toClientPaymentMessage(
      com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage paymentMessage);

  List<PaymentMessage> toClientPaymentMessageList(
      List<com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage> paymentMessageList);

  TransactionMessage toClientTransactionMessage(
      com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage);


}

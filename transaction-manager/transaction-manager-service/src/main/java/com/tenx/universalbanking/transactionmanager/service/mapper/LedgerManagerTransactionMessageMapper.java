package com.tenx.universalbanking.transactionmanager.service.mapper;


import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.PaymentMessage;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessageHeader;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LedgerManagerTransactionMessageMapper {

  TransactionMessageHeader toTransactionMessageHeaderMap(
      com.tenx.universalbanking.transactionmessage.TransactionMessageHeader transactionMessageHeader);

  List<PaymentMessage> toPaymentMessageList(
      List<com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage> paymentMessage);

  PaymentMessage toClientPaymentMessage(
      com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage paymentMessage);

  TransactionMessage toLMTransactionMessage(
      com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage);

}

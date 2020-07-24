package com.tenx.universalbanking.transactionmanager.utils;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import org.springframework.stereotype.Component;

@Component
public class LoggerUtils {

  private LoggerUtils(){

  }

  public static String appendTransactionMessageRequest(TransactionMessage transactionMessage) {
    if (transactionMessage == null) {
      return null;
    }
    StringBuilder transactionMsg = new StringBuilder();
    if (transactionMessage.getHeader() != null) {
      transactionMsg.append("MESSAGE_TYPE : " + transactionMessage.getHeader().getType());
    }
    if (transactionMessage.getAdditionalInfo() != null) {
      if (null != transactionMessage.getAdditionalInfo().get("TRANSACTION_CORRELATION_ID")) {
        transactionMsg
            .append(" ,TRANSACTION_CORRELATION_ID : " + transactionMessage.getAdditionalInfo()
                .get("TRANSACTION_CORRELATION_ID"));
      }
      if (null != transactionMessage.getAdditionalInfo().get("TRACE_ID")) {
        transactionMsg
            .append(" ,TRACE_ID : " + transactionMessage.getAdditionalInfo().get("TRACE_ID"));
      }
    }
    if (transactionMessage.getMessages() != null) {
      transactionMsg.append(" Messages : ");
      transactionMessage.getMessages().forEach((PaymentMessage paymentMessage) -> {
        transactionMsg.append(" { TYPE : " + paymentMessage.getType());
        transactionMsg
            .append(" ,PRODUCT_KEY : " + paymentMessage.getAdditionalInfo().get("PRODUCT_KEY"));
        transactionMsg
            .append(" ,PARTY_KEY : " + paymentMessage.getAdditionalInfo().get("PARTY_KEY"));
        transactionMsg.append(
            " ,SUBSCRIPTION_KEY : " + paymentMessage.getAdditionalInfo().get("SUBSCRIPTION_KEY"));
        if (null != paymentMessage.getAdditionalInfo().get("TRANSACTION_ID")) {
          transactionMsg.append(
              " ,TRANSACTION_ID : " + paymentMessage.getAdditionalInfo().get("TRANSACTION_ID"));
        }
        if (null != paymentMessage.getMessage().get("TOTAL_AMOUNT")) {
          transactionMsg
              .append(" ,TOTAL_AMOUNT : " + paymentMessage.getMessage().get("TOTAL_AMOUNT"));
        }
        if (null != paymentMessage.getMessage().get("INSTRUCTED_AMOUNT")) {
          transactionMsg.append(
              " ,INSTRUCTED_AMOUNT : " + paymentMessage.getMessage().get("INSTRUCTED_AMOUNT"));
        }
        if (null != paymentMessage.getMessage().get("CARD_TOKEN")) {
          transactionMsg.append(" ,CARD_TOKEN : " + paymentMessage.getMessage().get("CARD_TOKEN"));
        }
        if (null != paymentMessage.getMessage().get("TOKEN")) {
          transactionMsg.append(" ,TOKEN : " + paymentMessage.getMessage().get("TOKEN"));
        }
        transactionMsg.append(" } ");
      });
    }
    return transactionMsg.toString();
  }
}
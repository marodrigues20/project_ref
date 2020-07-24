package com.tenx.universalbanking.transactionmanager.utils;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Cain001Enum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class XMLMessageUtils {

  private static String escapeXMLSpecialCharacters(String value) {
    return StringEscapeUtils.escapeXml(value);
  }

  public static TransactionMessage escapeXMLSpecialCharactersForCain001PaymentMessages(TransactionMessage transactionMessage, Cain001Enum cain001Enum) {
    transactionMessage.getMessages().forEach(item -> {
      if (item.getType().equals(PaymentMessageTypeEnum.CAIN001.name())) {
        item.getMessage().put(cain001Enum.name(), escapeXMLSpecialCharacters(item.getMessage().get(cain001Enum.name()).toString()));
      }});

    return transactionMessage;
  }

}

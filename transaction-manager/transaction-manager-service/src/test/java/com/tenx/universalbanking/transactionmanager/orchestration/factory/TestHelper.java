package com.tenx.universalbanking.transactionmanager.orchestration.factory;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;

class TestHelper {

  private static final String JSON_SERIALIZER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  static String dateToString(Date date) throws ParseException {
    return new SimpleDateFormat(JSON_SERIALIZER_DATETIME_FORMAT).format(date);
  }

  static Date createDateFor(int day, int hour, int minute, int second,
      int nanoSecond) {
    return Date.from(
        LocalDateTime.of(2018, 5, day, hour, minute, second, nanoSecond).atZone(ZoneId.of("UTC")).toInstant());
  }

  static PaymentMessage getPaymentMessage(TransactionMessage transactionMessage) {
    return transactionMessage.getMessages().get(0);
  }

  static Customization createDatetimeCustomization(String path) {
    return new Customization(path,
        new RegularExpressionValueMatcher<>("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}[+]\\d{4}"));
  }

}

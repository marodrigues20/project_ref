package com.tenx.tsys.proxybatch.service;

import static com.tenx.tsys.proxybatch.utils.CurrencyUtil.getCurrencyCode;

import com.tenx.universalbanking.transactionmessage.enums.Cain003Enum;
import com.tenx.universalbanking.transactionmessage.enums.Cain005Enum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CainIsoMapBuilder {

  @Value("${date.format.julian}")
  private String julianDateFormat;

  @Value("${date.format.calendar}")
  private String calendarDateFormat;

  @Value("${time.zone.utc}")
  private String timeZoneUTC;

  @Value("${transaction.posted.time.position.in.map}")
  private String transactionPostedTimePositionInMap;
  private Logger logger = LoggerFactory.getLogger(getClass());

  public Map<String, Object> buildMessageMap(String message, List<String> dataPositionList)
      throws ParseException {
    Map<String, Object> dataMap = new HashMap<String, Object>();
    String transactionPostedEntry = dataPositionList
        .get(Integer.parseInt(transactionPostedTimePositionInMap));

    for (String dataPosition : dataPositionList) {
      try {
        String enumValAsKey = dataPosition.split(",")[0];
        String dataType = dataPosition.split(",")[3];
        int startIndex = Integer.parseInt(dataPosition.split(",")[1]) - 1;
        int endIndex = Integer.parseInt(dataPosition.split(",")[2]);
        if (!(startIndex > message.length() && endIndex > message.length())) {
          Object value = message.substring(startIndex, endIndex).trim();
          if (enumValAsKey.equals(Cain003Enum.TRANSACTION_DATE.name())
              || enumValAsKey.equals(Cain005Enum.TRANSACTION_DATE.name()) ||
              enumValAsKey.equals(PaymentMessageAdditionalInfoEnum.SETTLEMENT_DATE.name())) {
            value = setDateIntoMessageMap((String) value);
          } else if (enumValAsKey.equals(Cain003Enum.TRANSACTION_POSTED_DATE.name())) {
            value = setDateAndTimetoMessageMap((String) value, message, transactionPostedEntry);
            dataMap.put(Cain003Enum.TRANSACTION_POSTED_DATE_TIME.name(), value);
          }

          if (dataType.equals(DataType.STRING.name())) {
            if (enumValAsKey.equals(Cain003Enum.BILLING_CURRENCY_CODE.name())) {
              dataMap.put(enumValAsKey, getCurrencyCode(Integer.valueOf((String) value)));
            } else {
              dataMap.put(enumValAsKey, String.valueOf(value));
            }
          } else if (dataType.equals(DataType.DATE.name()) && !(
              enumValAsKey.equals(Cain003Enum.TRANSACTION_POSTED_TIME.name()) ||
                  enumValAsKey.equals(Cain003Enum.TRANSACTION_POSTED_TIME.name()))) {
            dataMap.put(enumValAsKey, value);
          } else if (dataType.equals(DataType.DECIMAL.name())) {
            if (enumValAsKey.equals(Cain003Enum.TRANSACTION_AMOUNT.name())) {
              StringBuilder sb = new StringBuilder((String) value);
              sb.insert(((String) value).length() - 2, '.');
              value = sb.toString();
            }
            dataMap.put(enumValAsKey, new BigDecimal((String) value));
          } else if (dataType.equals(DataType.BOOLEAN.name())) {
            switch (enumValAsKey) {
              case "CARDHOLER_PRESENT":
              case "CARDHOLDER_PRESENT":
                dataMap.put(enumValAsKey, "0".equals(value));
                break;
              case "RECURRING_PAYMENT_INDICATOR":
                dataMap.put(enumValAsKey, "R".equals(value));
                break;
              case "CASH_BACK_INDICATOR":
                dataMap.put(enumValAsKey, "Y".equals(value));
                break;
            }
          }
        }

      } catch (Exception e) {
        logger.error("Exception while parsing {}", dataPosition);
      }
    }
    return dataMap;
  }

  private String setDateAndTimetoMessageMap(String value, String message,
      String transactionPostedEntry) {
    String valueTobeSet = null;
    DateFormat fmt2 = new SimpleDateFormat(calendarDateFormat);
    int startIndex = Integer.parseInt(transactionPostedEntry.split(",")[1]) - 1;
    Date date = generateFormattedDate(value);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);

    calendar.set(Calendar.HOUR, Integer.parseInt(message.substring(startIndex, startIndex + 2)));
    calendar
        .set(Calendar.MINUTE, Integer.parseInt(message.substring(startIndex + 2, startIndex + 4)));
    calendar
        .set(Calendar.SECOND, Integer.parseInt(message.substring(startIndex + 4, startIndex + 6)));
    calendar.setTimeZone(TimeZone.getTimeZone(timeZoneUTC));
    valueTobeSet = fmt2.format(calendar.getTime());
    return valueTobeSet;
  }

  private Object setDateIntoMessageMap(String value) {
    DateFormat fmt2 = new SimpleDateFormat(calendarDateFormat);
    return fmt2.format(generateFormattedDate(value));
  }

  private Date generateFormattedDate(String value) {
    DateFormat fmt1 = new SimpleDateFormat(julianDateFormat);
    Date date = null;
    try {
      date = fmt1.parse(value);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return date;
  }

  public enum DataType {
    STRING, DECIMAL, INTEGER, DATE, BOOLEAN;
  }


}

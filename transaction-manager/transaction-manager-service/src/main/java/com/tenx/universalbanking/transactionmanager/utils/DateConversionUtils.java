package com.tenx.universalbanking.transactionmanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DateConversionUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DateConversionUtils.class);

  private static final java.lang.String JSON_SERIALIZER_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  public Date parse(String dateString) throws ParseException {
    return new SimpleDateFormat(JSON_SERIALIZER_DATETIME_FORMAT).parse(dateString);
  }

  public String format(Date accruedDate) {
    return new SimpleDateFormat(JSON_SERIALIZER_DATETIME_FORMAT).format(accruedDate);
  }

  public LocalDate getLastBusinessDate(LocalDate date) {
    Calendar cal = Calendar.getInstance();
    LocalDate prevDate = null;

    try {
      while (!date.equals(prevDate)) {
        prevDate = date;
        cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date.toString()));
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
          date = date.minusDays(1);
        } else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
          date = date.minusDays(2);
        }

        cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date.toString()));

        if (cal.get(Calendar.MONTH) == Calendar.JANUARY &&
            cal.get(Calendar.DAY_OF_MONTH) == 1) {
          date = date.minusDays(1);
        } else if (cal.get(Calendar.MONTH) == Calendar.MAY
            && cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
            && cal.get(Calendar.DAY_OF_MONTH) > (31 - 7)) {
          date = date.minusDays(3);
        } else if (cal.get(Calendar.MONTH) == Calendar.AUGUST
            && cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
            && cal.get(Calendar.DAY_OF_MONTH) > (31 - 7)) {
          date = date.minusDays(3);
        } else if (cal.get(Calendar.MONTH) == Calendar.DECEMBER &&
            cal.get(Calendar.DAY_OF_MONTH) == 25) {
          date = date.minusDays(1);
        } else if (cal.get(Calendar.MONTH) == Calendar.DECEMBER &&
            cal.get(Calendar.DAY_OF_MONTH) == 26) {
          date = date.minusDays(2);
        }

      }
    } catch (ParseException ex) {
      LOGGER.error(ex.getMessage());
    }
    return date;
  }
}

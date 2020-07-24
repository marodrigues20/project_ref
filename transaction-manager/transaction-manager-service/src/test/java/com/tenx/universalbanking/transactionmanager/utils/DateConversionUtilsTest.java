package com.tenx.universalbanking.transactionmanager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class DateConversionUtilsTest {

  private final DateConversionUtils dateConversionUtils = new DateConversionUtils();

  @Test
  public void shouldReturnDateFromString() throws ParseException {
    String dateString = "2012-12-13T12:12:12.000+0000";
    Long dateEpoch = 1355400732000L;
    Date actualDate = dateConversionUtils.parse(dateString);
    assertNotNull(actualDate);
    assertEquals(dateEpoch, actualDate.toInstant().toEpochMilli());
  }

  @Test
  public void shouldThrowParseException() throws ParseException {
    String dateString = "2012-11-13";
    assertThrows(ParseException.class, () -> {
      dateConversionUtils.parse(dateString);
    });
  }

  @Test
  public void shouldReturnStringFromDate() throws ParseException {
    Date date = new Date(1355400732000L);
    String actualDate = dateConversionUtils.format(date);
    assertNotNull(actualDate);
    assertEquals("2012-12-13T12:12:12.000+0000", actualDate);
  }

  @Test
  public void getPrevBusinessDateForSaturday() {
    LocalDate date = LocalDate.of(2018, Month.SEPTEMBER, 29);
    LocalDate responseDate = dateConversionUtils.getLastBusinessDate(date);
    assertEquals(28, responseDate.getDayOfMonth());
    assertEquals(Month.SEPTEMBER, responseDate.getMonth());
    assertEquals(2018, responseDate.getYear());
  }

  @Test
  public void getPrevBusinessDateForSunday() {
    LocalDate date = LocalDate.of(2018, Month.SEPTEMBER, 30);
    LocalDate responseDate = dateConversionUtils.getLastBusinessDate(date);
    assertEquals(28, responseDate.getDayOfMonth());
    assertEquals(Month.SEPTEMBER, responseDate.getMonth());
    assertEquals(2018, responseDate.getYear());
  }

  @Test
  public void getPrevBusinessDateForJan1st() {
    LocalDate date = LocalDate.of(2018, Month.JANUARY, 1);
    LocalDate responseDate = dateConversionUtils.getLastBusinessDate(date);
    assertEquals(29, responseDate.getDayOfMonth());
    assertEquals(Month.DECEMBER, responseDate.getMonth());
    assertEquals(2017, responseDate.getYear());
  }

  @Test
  public void getPrevBusinessDateForSpringBankHoliday() {
    LocalDate date = LocalDate.of(2018, Month.MAY, 28);
    LocalDate responseDate = dateConversionUtils.getLastBusinessDate(date);
    assertEquals(25, responseDate.getDayOfMonth());
    assertEquals(Month.MAY, responseDate.getMonth());
    assertEquals(2018, responseDate.getYear());
  }

  @Test
  public void getPrevBusinessDateForSummerBankHoliday() {
    LocalDate date = LocalDate.of(2018, Month.AUGUST, 27);
    LocalDate responseDate = dateConversionUtils.getLastBusinessDate(date);
    assertEquals(24, responseDate.getDayOfMonth());
    assertEquals(Month.AUGUST, responseDate.getMonth());
    assertEquals(2018, responseDate.getYear());
  }

  @Test
  public void getPrevBusinessDateForDec25() {
    LocalDate date = LocalDate.of(2018, Month.DECEMBER, 25);
    LocalDate responseDate = dateConversionUtils.getLastBusinessDate(date);
    assertEquals(24, responseDate.getDayOfMonth());
    assertEquals(Month.DECEMBER, responseDate.getMonth());
    assertEquals(2018, responseDate.getYear());
  }

  @Test
  public void getPrevBusinessDateForDec26() {
    LocalDate date = LocalDate.of(2018, Month.DECEMBER, 26);
    LocalDate responseDate = dateConversionUtils.getLastBusinessDate(date);
    assertEquals(24, responseDate.getDayOfMonth());
    assertEquals(Month.DECEMBER, responseDate.getMonth());
    assertEquals(2018, responseDate.getYear());
  }

  @Test
  public void getPrevBusinessDateForDec26Weekend() {
    LocalDate date = LocalDate.of(2017, Month.DECEMBER, 26);
    LocalDate responseDate = dateConversionUtils.getLastBusinessDate(date);
    assertEquals(22, responseDate.getDayOfMonth());
    assertEquals(Month.DECEMBER, responseDate.getMonth());
    assertEquals(2017, responseDate.getYear());
  }
}
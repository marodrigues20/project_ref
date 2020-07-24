package com.tenx.tsys.proxybatch.service;


import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import com.tenx.tsys.proxybatch.service.CainIsoMapBuilder.DataType;
import com.tenx.universalbanking.transactionmessage.enums.Cain003Enum;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CainIsoMapBuilderTest {

  @InjectMocks
  private CainIsoMapBuilder unit;

  private String validRawData;
  private String invalidRawData;
  private List<String> dataPositionList = new ArrayList<String>();

  @Before
  public void setupInputData() {

    setDataPositionList();
    Collections.unmodifiableList(dataPositionList);
    ReflectionTestUtils.setField(unit, "julianDateFormat", "yyyyDDD");
    ReflectionTestUtils.setField(unit, "calendarDateFormat", "yyyy-MM-dd'T'HH:mm:ss.SSS+0000");
    ReflectionTestUtils.setField(unit, "timeZoneUTC", "UTC");
    ReflectionTestUtils.setField(unit, "transactionPostedTimePositionInMap", "1");
  }

  @Test
  public void buildMessageMap_ShouldReturnMap_WhenValidData() throws ParseException {
    validRawData =
        "123456789012000000000000000000201806512345675356546020058856   000000000000000000000000000000000000000000000000000000001234567890123456789012345123456789012312312312345678901234560000001234560012012345678901234500D0000000000000123456780012345678900000000000000000000000000000000000000000000000000000000000000000000000000000DE2600000000000000000000000000000000000008260000000000000000000000000000000000201803600000000000000000000000000201804200000000000320-234.6789012345123456789012345000000000001234567890123400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010001234567890123456789000000000000000000000000000000000000000000000000000000000000001";
    HashMap<String, Object> messageMap = (HashMap<String, Object>) unit
        .buildMessageMap(validRawData, dataPositionList);
    assertEquals(messageMap.get(Cain003Enum.TOKENISED_PAN.name()), "5356546020058856");
    assertEquals(messageMap.get(Cain003Enum.BILLING_CURRENCY_CODE.name()),
        "GBP");
    assertEquals(messageMap.get(Cain003Enum.CARD_ACCEPTOR_NAME.name()),
        "1234567890123456789012345");
    assertEquals(messageMap.get(Cain003Enum.CARD_ACCEPTOR_CITY.name()), "1234567890123");
    assertEquals(messageMap.get(Cain003Enum.CARD_ACCEPTOR_STATE.name()), "123");
    assertEquals(messageMap.get(Cain003Enum.CARD_ACCEPTOR_COUNTRY_CODE.name()), "123");
    assertEquals(messageMap.get(Cain003Enum.MERCHANT_NUMBER.name()), "1234567890123456");
    assertEquals(messageMap.get(Cain003Enum.CARD_DATA_ENTRY_MODE.name()), "12");
    assertEquals(messageMap.get(Cain003Enum.CARDHOLER_PRESENT.name()), true);
    assertEquals(messageMap.get(Cain003Enum.CARDHOLDER_PRESENT.name()), true);
    assertEquals(messageMap.get(Cain003Enum.CARD_ACCEPTOR_ID.name()), "123456789012345");
    assertEquals(messageMap.get(Cain003Enum.CARD_ACCEPTOR_TERMINAL_ID.name()), "12345678");
    assertEquals(messageMap.get(Cain003Enum.BANKNET_REFERENCE_NUMBER.name()), "123456789");
    assertEquals(messageMap.get(Cain003Enum.MERCHANT_CATEGORY_CODE.name()), "DE26");
    assertEquals(messageMap.get(Cain003Enum.TRANSACTION_DATE.name()),
        "2018-02-11T00:00:00.000+0000");
    assertEquals(messageMap.get(Cain003Enum.BILLING_AMOUNT.name()),
        new BigDecimal("-234.6789012345"));
    assertEquals(messageMap.get(Cain003Enum.CASH_BACK_AMOUNT.name()),
        new BigDecimal("123456789012345"));
    assertEquals(messageMap.get(Cain003Enum.TRANSACTION_AMOUNT.name()),
        new BigDecimal("000000000003.20"));
    assertEquals(messageMap.get(Cain003Enum.RECURRING_PAYMENT_INDICATOR.name()), false);
    assertEquals(messageMap.get(Cain003Enum.EXCHANGE_RATE.name()),
        new BigDecimal("1234567890123456789"));
    assertEquals(messageMap.get(Cain003Enum.CASH_BACK_INDICATOR.name()), false);
    assertEquals("2018-03-06T12:34:56.000+0000",
        messageMap.get(Cain003Enum.TRANSACTION_POSTED_DATE_TIME.name()));
  }

  @Test
  public void buildMessageMap_ShouldReturnAUD_WhenBillingCurrencyCodeIs036() throws ParseException {
    validRawData =
        "123456789012000000000000000000201806512345675356546020058856   000000000000000000000000000000000000000000000000000000001234567890123456789012345123456789012312312312345678901234560000001234560012012345678901234500D0000000000000123456780012345678900000000000000000000000000000000000000000000000000000000000000000000000000000DE2600000000000000000000000000000000000000360000000000000000000000000000000000201803600000000000000000000000000201804200000000000320-234.6789012345123456789012345000000000001234567890123400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010001234567890123456789000000000000000000000000000000000000000000000000000000000000001";
    HashMap<String, Object> messageMap = (HashMap<String, Object>) unit
        .buildMessageMap(validRawData, dataPositionList);
    assertEquals(messageMap.get(Cain003Enum.BILLING_CURRENCY_CODE.name()),
        "AUD");
  }

  @Test
  public void buildMessageMap_ShouldReturnGBP_WhenBillingCurrencyCodeIs826() throws ParseException {
    validRawData =
        "123456789012000000000000000000201806512345675356546020058856   000000000000000000000000000000000000000000000000000000001234567890123456789012345123456789012312312312345678901234560000001234560012012345678901234500D0000000000000123456780012345678900000000000000000000000000000000000000000000000000000000000000000000000000000DE2600000000000000000000000000000000000008260000000000000000000000000000000000201803600000000000000000000000000201804200000000000320-234.6789012345123456789012345000000000001234567890123400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010001234567890123456789000000000000000000000000000000000000000000000000000000000000001";
    HashMap<String, Object> messageMap = (HashMap<String, Object>) unit
        .buildMessageMap(validRawData, dataPositionList);
    assertEquals(messageMap.get(Cain003Enum.BILLING_CURRENCY_CODE.name()),
        "GBP");
  }


  @Test
  public void buildMessageMap_ShouldReturnMap_WhenInvalidData() throws ParseException {
    invalidRawData = "TEST_DATA";
    HashMap<String, Object> messageMap = (HashMap<String, Object>) unit
        .buildMessageMap(invalidRawData, dataPositionList);
    assertThat(messageMap.get(Cain003Enum.TOKENISED_PAN.name()), not("1234567890123456789"));
  }


  private void setDataPositionList() {
    dataPositionList
        .add(Cain003Enum.TRANSACTION_POSTED_DATE.name() + ",031,037," + DataType.DATE.name());
    dataPositionList
        .add(Cain003Enum.TRANSACTION_POSTED_TIME.name() + ",038,044," + DataType.DATE.name());
    dataPositionList.add(Cain003Enum.TOKENISED_PAN.name() + ",045,063," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.CARD_ACCEPTOR_NAME.name() + ",120,144," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.CARD_ACCEPTOR_CITY.name() + ",145,157," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.CARD_ACCEPTOR_STATE.name() + ",158,160," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.CARD_ACCEPTOR_COUNTRY_CODE.name() + ",161,163," + DataType.STRING.name());
    dataPositionList.add(Cain003Enum.MERCHANT_NUMBER.name() + ",164,179," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.CARD_DATA_ENTRY_MODE.name() + ",194,195," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.CARDHOLER_PRESENT.name() + ",196,196," + DataType.BOOLEAN.name());
    dataPositionList
        .add(Cain003Enum.CARDHOLDER_PRESENT.name() + ",196,196," + DataType.BOOLEAN.name());
    dataPositionList
        .add(Cain003Enum.CARD_ACCEPTOR_ID.name() + ",197,211," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.CARD_ACCEPTOR_TERMINAL_ID.name() + ",228,235," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.BANKNET_REFERENCE_NUMBER.name() + ",238,246," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.MERCHANT_CATEGORY_CODE.name() + ",324,327," + DataType.STRING.name());
    dataPositionList
        .add(Cain003Enum.BILLING_CURRENCY_CODE.name() + ",365,367," + DataType.STRING.name());
    dataPositionList.add(Cain003Enum.TRANSACTION_DATE.name() + ",435,441," + DataType.DATE.name());
    dataPositionList.add(Cain003Enum.BILLING_AMOUNT.name() + ",456,470," + DataType.DECIMAL.name());
    dataPositionList
        .add(Cain003Enum.CASH_BACK_AMOUNT.name() + ",471,485," + DataType.DECIMAL.name());
    dataPositionList
        .add(Cain003Enum.TRANSACTION_AMOUNT.name() + ",442,455," + DataType.DECIMAL.name());
    dataPositionList.add(
        Cain003Enum.RECURRING_PAYMENT_INDICATOR.name() + ",658,658," + DataType.BOOLEAN.name());
    dataPositionList.add(Cain003Enum.EXCHANGE_RATE.name() + ",662,680," + DataType.DECIMAL.name());
    dataPositionList
        .add(Cain003Enum.CASH_BACK_INDICATOR.name() + ",743,743," + DataType.BOOLEAN.name());

  }

}

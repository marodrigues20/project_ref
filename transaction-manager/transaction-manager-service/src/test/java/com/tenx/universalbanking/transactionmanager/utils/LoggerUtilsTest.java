package com.tenx.universalbanking.transactionmanager.utils;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class LoggerUtilsTest {

  private static final String REQUEST_MESSAGE = "message/LoggerUtilsTestMessage.json";
  private final FileReaderUtil fileReader = new FileReaderUtil();

  @Test
  public void appendTransactionMessageRequestNullTest() {
    String actual = LoggerUtils.appendTransactionMessageRequest(null);
    assertNull(actual);
  }

  @Test
  public void appendTransactionMessageRequestTest() throws IOException {
    TransactionMessage message = stringToJson(
        fileReader.getFileContent(REQUEST_MESSAGE),
        TransactionMessage.class);
    String actual = LoggerUtils.appendTransactionMessageRequest(message);
    assertNotNull(actual);
    assertEquals(
        "MESSAGE_TYPE : CARD_AUTH_VIA_ADVICE ,TRANSACTION_CORRELATION_ID : 5306671493 ,TRACE_ID : 10000 Messages :  { TYPE : CAIN002 ,PRODUCT_KEY : 703 ,PARTY_KEY : 102 ,SUBSCRIPTION_KEY : abc ,TRANSACTION_ID : 5306671493 ,TOTAL_AMOUNT : 123 ,INSTRUCTED_AMOUNT : 13 ,CARD_TOKEN : 1234-----56788 ,TOKEN : 12 } ",
        actual);
  }

  @Test
  public void appendTransactionMessageRequestHeaderNullTest() throws IOException {
    TransactionMessage message = stringToJson(
        fileReader.getFileContent(REQUEST_MESSAGE),
        TransactionMessage.class);
    message.setHeader(null);
    String actual = LoggerUtils.appendTransactionMessageRequest(message);
    assertNotNull(actual);
    assertEquals(
        " ,TRANSACTION_CORRELATION_ID : 5306671493 ,TRACE_ID : 10000 Messages :  { TYPE : CAIN002 ,PRODUCT_KEY : 703 ,PARTY_KEY : 102 ,SUBSCRIPTION_KEY : abc ,TRANSACTION_ID : 5306671493 ,TOTAL_AMOUNT : 123 ,INSTRUCTED_AMOUNT : 13 ,CARD_TOKEN : 1234-----56788 ,TOKEN : 12 } ",
        actual);
  }

  @Test
  public void appendTransactionMessageRequestAddInfoNullTest() throws IOException {
    TransactionMessage message = stringToJson(
        fileReader.getFileContent(REQUEST_MESSAGE),
        TransactionMessage.class);
    message.setAdditionalInfo(null);
    String actual = LoggerUtils.appendTransactionMessageRequest(message);
    assertNotNull(actual);
    assertEquals(
        "MESSAGE_TYPE : CARD_AUTH_VIA_ADVICE Messages :  { TYPE : CAIN002 ,PRODUCT_KEY : 703 ,PARTY_KEY : 102 ,SUBSCRIPTION_KEY : abc ,TRANSACTION_ID : 5306671493 ,TOTAL_AMOUNT : 123 ,INSTRUCTED_AMOUNT : 13 ,CARD_TOKEN : 1234-----56788 ,TOKEN : 12 } ",
        actual);
  }

  @Test
  public void appendTransactionMessageRequestTransactionCorrelationNullTest() throws IOException {
    TransactionMessage message = stringToJson(
        fileReader.getFileContent(REQUEST_MESSAGE),
        TransactionMessage.class);
    message.getAdditionalInfo().put("TRANSACTION_CORRELATION_ID", null);
    String actual = LoggerUtils.appendTransactionMessageRequest(message);
    assertNotNull(actual);
    assertEquals(
        "MESSAGE_TYPE : CARD_AUTH_VIA_ADVICE ,TRACE_ID : 10000 Messages :  { TYPE : CAIN002 ,PRODUCT_KEY : 703 ,PARTY_KEY : 102 ,SUBSCRIPTION_KEY : abc ,TRANSACTION_ID : 5306671493 ,TOTAL_AMOUNT : 123 ,INSTRUCTED_AMOUNT : 13 ,CARD_TOKEN : 1234-----56788 ,TOKEN : 12 } ",
        actual);
  }

  @Test
  public void appendTransactionMessageRequestTraceIdNullTest() throws IOException {
    TransactionMessage message = stringToJson(
        fileReader.getFileContent(REQUEST_MESSAGE),
        TransactionMessage.class);
    message.getAdditionalInfo().put("TRACE_ID", null);
    String actual = LoggerUtils.appendTransactionMessageRequest(message);
    assertNotNull(actual);
    assertEquals(
        "MESSAGE_TYPE : CARD_AUTH_VIA_ADVICE ,TRANSACTION_CORRELATION_ID : 5306671493 Messages :  { TYPE : CAIN002 ,PRODUCT_KEY : 703 ,PARTY_KEY : 102 ,SUBSCRIPTION_KEY : abc ,TRANSACTION_ID : 5306671493 ,TOTAL_AMOUNT : 123 ,INSTRUCTED_AMOUNT : 13 ,CARD_TOKEN : 1234-----56788 ,TOKEN : 12 } ",
        actual);
  }

  @Test
  public void appendTransactionMessageRequestTotalAmountNullTest() throws IOException {
    TransactionMessage message = stringToJson(
        fileReader.getFileContent(REQUEST_MESSAGE),
        TransactionMessage.class);
    message.getMessages().get(0).getMessage().put("TOTAL_AMOUNT", null);
    String actual = LoggerUtils.appendTransactionMessageRequest(message);
    assertNotNull(actual);
    assertEquals(
        "MESSAGE_TYPE : CARD_AUTH_VIA_ADVICE ,TRANSACTION_CORRELATION_ID : 5306671493 ,TRACE_ID : 10000 Messages :  { TYPE : CAIN002 ,PRODUCT_KEY : 703 ,PARTY_KEY : 102 ,SUBSCRIPTION_KEY : abc ,TRANSACTION_ID : 5306671493 ,INSTRUCTED_AMOUNT : 13 ,CARD_TOKEN : 1234-----56788 ,TOKEN : 12 } ",
        actual);
  }

  @Test
  public void appendTransactionMessageRequestMessagesNullTest() throws IOException {
    TransactionMessage message = stringToJson(
        fileReader.getFileContent(REQUEST_MESSAGE),
        TransactionMessage.class);
    message.setMessages(null);
    String actual = LoggerUtils.appendTransactionMessageRequest(message);
    assertNotNull(actual);
    assertEquals(
        "MESSAGE_TYPE : CARD_AUTH_VIA_ADVICE ,TRANSACTION_CORRELATION_ID : 5306671493 ,TRACE_ID : 10000",
        actual);
  }

}

package com.tenx.universalbanking.transactionmanager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class JsonUtilsTest {

  @Test
  public void jsonToStringNullTest() {
    String actual = JsonUtils.jsonToString(null);
    assertEquals("Null Object", actual);
  }

  @Test
  public void jsonToStringSuccessTest() {
    String actual = JsonUtils.jsonToString(new TransactionMessage());
    assertNotNull(actual);
    assertEquals("{\"additionalInfo\":null,\"header\":null,\"messages\":null}", actual);
  }

  @Test
  public void stringToJsonNullTest() {
    TransactionMessage actual = JsonUtils.stringToJson(null, TransactionMessage.class);
    assertNull(actual);
  }

  @Test
  public void stringToJsonSuccessTest() {
    String json = "{\"header\":{\"type\":\"FPS_OUT\",\"url\":null},\"messages\":null,\"additionalInfo\":{\"REQUEST_ID\":\"0152199712\"}}";
    TransactionMessage actual = JsonUtils.stringToJson(json, TransactionMessage.class);
    assertNotNull(actual);
    assertEquals("FPS_OUT", actual.getHeader().getType());
  }

  @Test
  public void stringToJsonParseExceptionTest() {
    TransactionMessage actual = JsonUtils.stringToJson("", TransactionMessage.class);
    assertNull(actual);
  }

  @Test
  public void toJsonSuccessTest() {
    String actual = JsonUtils.toJson(new TransactionMessage());
    assertNotNull(actual);
    assertEquals("{\"additionalInfo\":null,\"header\":null,\"messages\":null}", actual);
  }

}

package com.tenx.universalbanking.transactionmanager.utils;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.AMOUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class XMLMessageUtilsTest {

  private static final String REQUEST_MESSAGE = "message/cain001TransactionMesage.json";
  private final FileReaderUtil fileReader = new FileReaderUtil();

  @Test
  public void appendTransactionMessageRequestNullTest() throws IOException {
    TransactionMessage message = stringToJson(
        fileReader.getFileContent(REQUEST_MESSAGE),
        TransactionMessage.class);
    TransactionMessage actual = XMLMessageUtils
        .escapeXMLSpecialCharactersForCain001PaymentMessages(message, AMOUNT);
    assertNotNull(actual);
    assertEquals(message, actual);
  }

}

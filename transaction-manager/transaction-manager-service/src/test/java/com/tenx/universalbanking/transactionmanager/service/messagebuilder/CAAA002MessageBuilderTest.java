package com.tenx.universalbanking.transactionmanager.service.messagebuilder;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.TOP_UP_BY_CARD;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@RunWith(MockitoJUnitRunner.class)
public class CAAA002MessageBuilderTest {

  private static final String CARD_FUND_TRANSACTION_MESSAGE_REQUEST = "message/cardFundTransactionMessageRequest.json";
  private static final String CARD_FUND_PDF_FAILURE_REQUEST = "message/payment-decision-failure-response.json";
  private static final String CARD_FUND_TRANSACTION_MESSAGE_RESPONSE = "message/cardFundTransactionMessageResponse.json";
  private static final String CARD_FUND_CAAA002_PAYMENT_MESSAGE = "message/caa002PaymentMessage.json";

  @InjectMocks
  private CAAA002MessageBuilder caa002MessageBuilder;

  @Mock
  private GeneratorUtil generatorUtil;

  private FileReaderUtil fileReader;

  @Before
  public void init() {
    fileReader = new FileReaderUtil();
    when(generatorUtil.generateRandomKey()).thenReturn("55555");
  }

  @Test
  public void caaa002MessageBuilderTest() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    PaymentProcessResponse PaymentProcessResponse = stringToJson(
        fileReader.getFileContent(CARD_FUND_PDF_FAILURE_REQUEST),
        PaymentProcessResponse.class);

    TransactionMessage expectedTxnMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_RESPONSE),
        TransactionMessage.class);

    TransactionMessage txnMessage = caa002MessageBuilder.caaa002MessageBuilder(transactionMessage, PaymentProcessResponse);
    JSONAssert.assertEquals(new JSONObject(expectedTxnMessage),
        new JSONObject(txnMessage), JSONCompareMode.LENIENT);

  }

  @Test
  public void buildHeaderTest(){
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(TOP_UP_BY_CARD.name());

    TransactionMessageHeader actualResult = caa002MessageBuilder.buildHeader();
    assertEquals(header.getType(),actualResult.getType());

  }

  @Test
  public void buildAdditionalInfoTest(){
    Map<String, Object> additionalInfoMap = new HashMap<>();
    additionalInfoMap.put(REQUEST_ID.name(), generatorUtil.generateRandomKey());

    Map<String, Object> actualAdditionalInfoMap = caa002MessageBuilder.buildAdditionalInfo();
    assertEquals(additionalInfoMap.get(REQUEST_ID.name()),actualAdditionalInfoMap.get(REQUEST_ID.name()));
  }

  @Test
  public void buildCaaa002MessageTest() throws IOException{

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    PaymentProcessResponse paymentProcessResponse = stringToJson(
        fileReader.getFileContent(CARD_FUND_PDF_FAILURE_REQUEST),
        PaymentProcessResponse.class);

    PaymentMessage expectedCAA002Payment = stringToJson(
        fileReader.getFileContent(CARD_FUND_CAAA002_PAYMENT_MESSAGE),
        PaymentMessage.class);

    PaymentMessage actualCAAA002Message = caa002MessageBuilder.buildCaaa002Message(transactionMessage,paymentProcessResponse);

    JSONAssert.assertEquals(new JSONObject(expectedCAA002Payment),
        new JSONObject(actualCAAA002Message), JSONCompareMode.LENIENT);
  }

}

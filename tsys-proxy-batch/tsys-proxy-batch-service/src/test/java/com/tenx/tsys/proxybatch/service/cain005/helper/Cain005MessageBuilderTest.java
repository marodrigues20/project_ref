package com.tenx.tsys.proxybatch.service.cain005.helper;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tenx.tsys.proxybatch.client.debitcardmanager.model.DebitCardResponse;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.PaymentMessage;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessage;
import com.tenx.tsys.proxybatch.service.CainIsoMapBuilder;
import com.tenx.tsys.proxybatch.service.PaymentMethodProvider;
import com.tenx.tsys.proxybatch.util.FileUtility;
import com.tenx.tsys.proxybatch.utils.JsonUtils;
import com.tenx.universalbanking.transactionmessage.enums.Cain005Enum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Cain005MessageBuilderTest {

  private static final String CAIN005_RESPONSE = "response/cain005-transaction-message.json";
  private final String subscriptionKey = "TEST_SUBSCRIPTION_KEY";
  private final String productKey = "TEST_PRODUCT_KEY";
  @InjectMocks
  private Cain005MessageBuilder unit;
  @Mock
  private CainIsoMapBuilder cainIsoMapBuilder;
  @Spy
  private PaymentMethodProvider paymentMethodProvider;
  private String validRawData;
  private String invalidRawData;

  @Before
  public void setupInputData() {
    validRawData = "12345678901200000000000000000020180651234567123456789012345678900000000000000000000000000000000000000000000000000000000123456789012345678901234512345678901231231231234567890123456000000123456001211234567890123450010000000000000123456780012345678900000000000000000000000000000000000000000000000000000000000000000000000000000DE2600000000000000000000000000000000000000000000000000000000000000000000000000201803600000000000000000000000000201804200000000000000Yen456789012345123456789012345000000000001234567890123400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010001234567890123456789000000000000000000000000000000000000000000000000000000000000001";
    invalidRawData = "TEST_DATA";
  }

  @Test
  public void setTransactionMessageHeader_ShouldReturnValidMessageHeader() {
    TransactionMessage transactionMessage = new TransactionMessage();
    unit.setTransactionMessageHeader(transactionMessage);
    assertNotNull(transactionMessage);
  }

  @Test
  public void setTransactionMessageHeader_ShouldReturnValidMessageHeader_WhenValidData() {
    TransactionMessage transactionMessage = new TransactionMessage();
    unit.setTransactionMessageHeader(transactionMessage);
    assertNotNull(transactionMessage);
    assertEquals(transactionMessage.getHeader().getType(),
        TransactionMessageTypeEnum.CLEARING.name());
  }

  @Test
  public void setTransactionMessage_ShouldReturnaAtleastOneMessage_WhenValidRawData()
      throws ParseException {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> expectedMessageMap = generateMessageMapData();
    DebitCardResponse debitCardResponse = buildDebitCardResponse();
    when(cainIsoMapBuilder.buildMessageMap(validRawData, unit.getMessageDataPositionList()))
        .thenReturn(expectedMessageMap);
    unit.setTransactionMessage(transactionMessage, validRawData, debitCardResponse);
    assertNotNull(transactionMessage.getMessages().get(0));
  }

  @Test
  public void setTransactionMessage_ShouldReturnCAIN005MessageType_WhenValidData()
      throws ParseException {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> expectedMessageMap = generateMessageMapData();
    DebitCardResponse debitCardResponse = buildDebitCardResponse();
    when(cainIsoMapBuilder.buildMessageMap(validRawData, unit.getMessageDataPositionList()))
        .thenReturn(expectedMessageMap);
    unit.setTransactionMessage(transactionMessage, validRawData, debitCardResponse);
    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertEquals(paymentMessage.getType(), PaymentMessageTypeEnum.CAIN005.name());
  }

  @Test
  public void setTransactionMessage_ShouldReturnCAIN005MessageBody_WhenValidData()
      throws Exception {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> expectedMessageMap = generateMessageMapData();
    DebitCardResponse debitCardResponse = buildDebitCardResponse();
    when(cainIsoMapBuilder.buildMessageMap(validRawData, unit.getMessageDataPositionList()))
        .thenReturn(expectedMessageMap);
    unit.setTransactionMessage(transactionMessage, validRawData, debitCardResponse);
    assertThat(FileUtility.getFileContent(CAIN005_RESPONSE))
        .isEqualTo(JsonUtils.jsonToString(transactionMessage));
  }
//  check sub key &Add Info

  @Test
  public void setTransactionMessage_ShouldReturnCAIN005AdditionalInfo_WhenValidData()
      throws ParseException {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> expectedMessageMap = generateMessageMapData();
    DebitCardResponse debitCardResponse = buildDebitCardResponse();
    when(cainIsoMapBuilder.buildMessageMap(validRawData, unit.getMessageDataPositionList()))
        .thenReturn(expectedMessageMap);
    unit.setTransactionMessage(transactionMessage, validRawData, debitCardResponse);
    PaymentMessage paymentMessage = (PaymentMessage) transactionMessage.getMessages().get(0);
    assertNotNull(paymentMessage.getAdditionalInfo());
  }

  @Test
  public void setTransactionMessage_ShouldReturnCAIN005SubscriptionKey_WhenValidData()
      throws ParseException {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> expectedMessageMap = generateMessageMapData();
    DebitCardResponse debitCardResponse = buildDebitCardResponse();
    when(cainIsoMapBuilder.buildMessageMap(validRawData, unit.getMessageDataPositionList()))
        .thenReturn(expectedMessageMap);
    unit.setTransactionMessage(transactionMessage, validRawData, debitCardResponse);
    PaymentMessage paymentMessage = (PaymentMessage) transactionMessage.getMessages().get(0);
    assertTrue(paymentMessage.getAdditionalInfo().toString().contains(subscriptionKey));
  }

  private Map<String, Object> generateMessageMapData() {
    Map<String, Object> expectedMessageMap = new HashMap<String, Object>();
    expectedMessageMap.put(Cain005Enum.CARD_ACCEPTOR_STATE.name(), "123");
    expectedMessageMap.put(Cain005Enum.CASH_BACK_INDICATOR.name(), "1");
    expectedMessageMap.put(Cain005Enum.CARD_ACCEPTOR_TERMINAL_ID.name(), "12345678");
    expectedMessageMap.put(Cain005Enum.MERCHANT_CATEGORY_CODE.name(), "DE26");
    expectedMessageMap.put(Cain005Enum.CARD_DATA_ENTRY_MODE.name(), "05");
    expectedMessageMap.put(Cain005Enum.COMMON_COUNTRY_CODE.name(), "GBR");

    return expectedMessageMap;
  }

  private DebitCardResponse buildDebitCardResponse() {
    DebitCardResponse debitCardResponse = new DebitCardResponse();
    debitCardResponse.setSubscriptionKey(subscriptionKey);
    debitCardResponse.setProductKey(productKey);
    return debitCardResponse;

  }


}

package com.tenx.tsys.proxybatch.service.cain005;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import com.tenx.tsys.proxybatch.client.debitcardmanager.model.DebitCardResponse;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.PaymentMessage;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessage;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessageHeader;
import com.tenx.tsys.proxybatch.service.cain005.helper.Cain005MessageBuilder;
import java.text.ParseException;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Cain005PaymentServiceTest {

  private final String subscriptionKey = "TEST_SUBSCRIPTION_KEY";
  private final String productKey = "TEST_PRODUCT_KEY";
  private String testData = "12345678901200000000000000000012345671234567123456789012345678900000000000000000000000000000000000000000000000000000000123456789012345678901234512345678901231231231234567890123456000000123456001211234567890123450010000000000000123456780012345678900000000000000000000000000000000000000000000000000000000000000000000000000000DE2600000000000000000000000000000000000000000000000000000000000000000000000000201803600000000000000000000000000201804200000000000000Yen456789012345123456789012345000000000001234567890123400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010001234567890123456789000000000000000000000000000000000000000000000000000000000000001";
  @InjectMocks
  private Cain005PaymentService cain005PaymentService;
  @Mock
  private Cain005MessageBuilder cain005MessageBuilder;

  @Test
  public void buildMessageTest_Return_NotNUllResult_WhenSuccess() throws ParseException {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setHeader(new TransactionMessageHeader());
    DebitCardResponse debitCardResponse = buildDebitCardResponse();
    when(cain005MessageBuilder.setTransactionMessageHeader(transactionMessage))
        .thenReturn(transactionMessage);
    when(cain005MessageBuilder
        .setTransactionMessage(transactionMessage, testData, debitCardResponse))
        .thenReturn(transactionMessage);
    cain005PaymentService.buildMessage(testData, debitCardResponse);
    assertNotNull(transactionMessage);
  }

  @Test
  public void buildMessageTest_Return_Header_AndMessage_WhenSuccess() throws ParseException {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setHeader(new TransactionMessageHeader());
    transactionMessage.setMessages(new ArrayList<PaymentMessage>());
    DebitCardResponse debitCardResponse = buildDebitCardResponse();
    when(cain005MessageBuilder.setTransactionMessageHeader(transactionMessage))
        .thenReturn(transactionMessage);
    when(cain005MessageBuilder
        .setTransactionMessage(transactionMessage, testData, debitCardResponse))
        .thenReturn(transactionMessage);
    cain005PaymentService.buildMessage(testData, debitCardResponse);
    assertNotNull(transactionMessage);
    assertNotNull(transactionMessage.getHeader());
    assertNotNull(transactionMessage.getMessages());
  }

  private DebitCardResponse buildDebitCardResponse() {
    DebitCardResponse debitCardResponse = new DebitCardResponse();
    debitCardResponse.setSubscriptionKey(subscriptionKey);
    debitCardResponse.setProductKey(productKey);
    return debitCardResponse;

  }
}


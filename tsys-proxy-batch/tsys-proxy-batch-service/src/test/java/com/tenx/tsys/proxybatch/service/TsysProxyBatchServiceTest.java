package com.tenx.tsys.proxybatch.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.tenx.tsys.proxybatch.client.debitcardmanager.api.DebitCardControllerApi;
import com.tenx.tsys.proxybatch.client.debitcardmanager.model.DebitCardResponse;
import com.tenx.tsys.proxybatch.client.transactionmanager.api.SettlementControllerApi;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessage;
import com.tenx.tsys.proxybatch.dto.request.SettlementRequestDto;
import com.tenx.tsys.proxybatch.service.cain003.Cain003PaymentService;
import com.tenx.tsys.proxybatch.service.cain005.Cain005PaymentService;
import java.text.ParseException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;


@RunWith(MockitoJUnitRunner.Silent.class)
@ComponentScan("com.tenx")
public class TsysProxyBatchServiceTest {

  private static final String REPAIR_TOPIC = "repair-topic";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @InjectMocks
  private TsysProxyBatchService tsysProxyBatchService;
  @Mock
  private DebitCardControllerApi debitCardControllerApi;
  @Mock
  private SettlementControllerApi settlementControllerApi;
  @Mock
  private CainPaymentService cainPaymentService;
  @Mock
  private Cain005PaymentService cain005PaymentService;
  @Mock
  private Cain003PaymentService cain003PaymentService;
  @Mock
  private MessageSender messageSender;

  private KafkaProducerService kafkaProducerService;

  private DebitCardResponse expected;
  private SettlementRequestDto testData;
  private SettlementRequestDto invalidTestData;
  private SettlementRequestDto testData742;
  private String cardToken;
  private TransactionMessage transactionMessage;

  @Before
  public void setUp() {
    kafkaProducerService = mock(KafkaProducerService.class);
    messageSender = new MessageSender(kafkaProducerService, REPAIR_TOPIC);
    expected = new DebitCardResponse();
    expected.setSubscriptionKey("TEST_SUBSCRIPTION_KEY");
    testData = new SettlementRequestDto();
    testData.setSettlementRequest(
        "TESTCSETTLEMENT_REQUEST_REQUEST_REQUEST_REQQTEST_CARD_TOKENXXXXAST_REQUEST_REQUEST_REQUEST_REQUEST");
    invalidTestData = new SettlementRequestDto();
    invalidTestData.setSettlementRequest(
        "TEST_SETTLEMENT_REQUEST");
    testData742 = new SettlementRequestDto();
    testData742.setSettlementRequest("TESTCSETTLEMENT_REQUEST_REQUEST_REQUEST_REQQTEST_" +
        "CARD_TOKENXXXXAST_REQUEST_REQUEST_REQUEST_REQUEST_TEST_SETTLEMENT_REQUEST_REQUEST_REQUEST_REQQTEST_"
        +
        "CARD_TOKENXXXXAST_REQUEST_REQUEST_REQUEST_REQUEST_SETTLEMENT_REQUCC_TEST_SETTLEMENT_REQUEST"
        +
        "_REQUEST_REQUEST_REQQTEST_CARD_TOKENXXXXAST_REQUEST_REQUEST_REQUEST_REQUEST_TEST_SETTLEMENT"
        +
        "_REQUEST_REQUEST_REQUEST_REQQTEST_CARD_TOKENXXXXAST_REQUEST_REQUEST_REQUEST_REQUEST_SETTLEMENT"
        +
        "_REQUCC_TEST_SETTLEMENT_REQUEST_REQUEST_REQUEST_REQQTEST_CARD_TOKENXXXXAST_REQUEST_" +
        "REQUEST_REQUEST_REQUEST_TEST_SETTLEMENT_REQUEST_REQUEST_REQUEST_REQQTEST_CARD_TOKENXXXXAST_REQUEST"
        +
        "_REQUEST_REQUEST_REQUEST_SETTLEMENT_REQUEST_TEST_SETTLEMENT_REQUEST_REQUEST_REQUEST_REQQTEST_CARD_"
        +
        "TOKENXXXXAST_REQUEST_REQUEST_REQUEST_RE");
    cardToken = "TEST_CARD_TOKENXXXX";
    ReflectionTestUtils.setField(tsysProxyBatchService, "debitIndicator", "D");
    ReflectionTestUtils.setField(tsysProxyBatchService, "creditIndicator", "C");
    ReflectionTestUtils.setField(tsysProxyBatchService, "creditDebitIndicatorPosition", "5");
    ReflectionTestUtils.setField(tsysProxyBatchService, "settlementRequestCardTokenStart", "45");
    ReflectionTestUtils.setField(tsysProxyBatchService, "settlementRequestCardTokenEnd", "63");
  }

  @Test
  public void tsysProxyBatchMethod_ShouldReturnSubscriptionDetails_WhenInputTokenIsValid()
      throws Exception {

    when(debitCardControllerApi.getCardByToken(cardToken)).thenReturn(expected);
    tsysProxyBatchService.findSubscriptionKey(testData);
    verify(debitCardControllerApi).getCardByToken(cardToken);
  }

  @Test
  public void tsysProxyBatchMethod_ShouldVerifyCorrectTokenValue()
      throws Exception {

    when(debitCardControllerApi.getCardByToken(cardToken)).thenReturn(expected);
    tsysProxyBatchService.findSubscriptionKey(testData);
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor
        .forClass(String.class);
    verify(debitCardControllerApi).getCardByToken(argumentCaptor.capture());
    Assert.assertEquals(cardToken, argumentCaptor.getValue());
  }


  @Test
  public void tsysProxyBatchMethod_ShouldThrowExceptionAndWriteMessageOnKafka_WhenSubscriptionKeyNotFound()
      throws Exception {
    when(debitCardControllerApi.getCardByToken(cardToken))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    ProducerRecord<String, String> expectedProducerRecord = new ProducerRecord<>(REPAIR_TOPIC,
        "TEST_CARD_TOKEN");
    tsysProxyBatchService.findSubscriptionKey(testData);
    messageSender.send(REPAIR_TOPIC, "TEST_CARD_TOKEN");
    verify(kafkaProducerService).send(expectedProducerRecord);
  }

  @Test
  public void tsysProxyBatchMethodWhenSubscriptionKeyFound()
      throws Exception {
    ReflectionTestUtils.setField(tsysProxyBatchService, "creditDebitIndicatorPosition", "214");
    when(debitCardControllerApi.getCardByToken(cardToken))
        .thenReturn(expected);
    DebitCardResponse debitCardResponse = debitCardControllerApi
        .getCardByToken(cardToken);
    tsysProxyBatchService
        .makeDebitPaymentProcessWithCainMessage(testData.getSettlementRequest(), debitCardResponse);
    verifyNoMoreInteractions(settlementControllerApi);
  }

  @Test
  public void makeDebitPaymentProcessWithCainMessageWhenLengthGreaterThan214Cain005MessageService()
      throws ParseException {
    tsysProxyBatchService
        .makeDebitPaymentProcessWithCainMessage(testData.getSettlementRequest(), expected);
    when(cainPaymentService.buildMessage(testData.getSettlementRequest(), expected))
        .thenReturn(transactionMessage);
    when(settlementControllerApi.processSettlementUsingPOST(transactionMessage)).thenReturn(null);
    verify(settlementControllerApi).processSettlementUsingPOST(transactionMessage);
  }

  @Test
  public void makeDebitPaymentProcessWithCainMessageWhenLengthGreaterThan214Cain003MessageService()
      throws ParseException {
    tsysProxyBatchService
        .makeDebitPaymentProcessWithCainMessage(testData.getSettlementRequest().replace('C', 'D'),
            expected);
    when(cainPaymentService.buildMessage(testData.getSettlementRequest(), expected))
        .thenReturn(transactionMessage);
    when(settlementControllerApi.processSettlementUsingPOST(transactionMessage)).thenReturn(null);
    verify(settlementControllerApi).processSettlementUsingPOST(transactionMessage);
  }

  @Test
  public void makeDebitPaymentProcessWithCainMessageWhenLengthIs742Cain005MessageService()
      throws ParseException {
    tsysProxyBatchService
        .makeDebitPaymentProcessWithCainMessage(testData742.getSettlementRequest(), expected);
    when(cainPaymentService.buildMessage(testData.getSettlementRequest(), expected))
        .thenReturn(transactionMessage);
    when(settlementControllerApi.processSettlementUsingPOST(transactionMessage)).thenReturn(null);
    verify(settlementControllerApi).processSettlementUsingPOST(transactionMessage);
  }

  @Test
  public void makeDebitPaymentProcessWithCainMessageWhenLengthIs742Cain003MessageService()
      throws ParseException {
    tsysProxyBatchService.makeDebitPaymentProcessWithCainMessage(
        testData742.getSettlementRequest().replace('C', 'D'), expected);
    when(cainPaymentService.buildMessage(testData.getSettlementRequest(), expected))
        .thenReturn(transactionMessage);
    when(settlementControllerApi.processSettlementUsingPOST(transactionMessage)).thenReturn(null);
    verify(settlementControllerApi).processSettlementUsingPOST(transactionMessage);
  }

  @Test
  public void makeDebitPaymentProcessWithCainMessageThrowsException() throws ParseException {
    tsysProxyBatchService
        .makeDebitPaymentProcessWithCainMessage(testData.getSettlementRequest(), expected);
    when(cainPaymentService.buildMessage(testData.getSettlementRequest(), expected))
        .thenThrow(ParseException.class);
    when(settlementControllerApi.processSettlementUsingPOST(transactionMessage)).thenReturn(null);
    verify(settlementControllerApi).processSettlementUsingPOST(transactionMessage);
  }

  @Test
  public void tsysProxyBatchMethodWhenSubscriptionKeyNotFound()
      throws Exception {
    when(debitCardControllerApi.getCardByToken(cardToken))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    tsysProxyBatchService
        .makeDebitPaymentProcessWithCainMessage(testData.getSettlementRequest(), expected);
    verifyNoMoreInteractions(debitCardControllerApi);
  }

  @Test
  public void findSubscriptionKeyInvalidSettlementRequest() throws Exception {
    when(debitCardControllerApi.getCardByToken(cardToken)).thenReturn(expected);
    tsysProxyBatchService.findSubscriptionKey(invalidTestData);
    verifyNoMoreInteractions(debitCardControllerApi);
  }
}
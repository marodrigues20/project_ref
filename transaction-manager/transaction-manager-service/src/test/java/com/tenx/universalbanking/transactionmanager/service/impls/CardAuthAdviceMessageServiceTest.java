package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TOTAL_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.DebitCreditIndicatorEnum.CREDIT;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH_VIA_ADVICE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.APPROVED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.DEBIT_CREDIT_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_POS_CHIP_AND_PIN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.UNKNOWN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.CardAuthResponseBuilder;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.client.PaymentDecisionClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.messagebuilder.CAIN002MessageBuilder;
import com.tenx.universalbanking.transactionmanager.service.validation.MessageValidator;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Cain001Enum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class CardAuthAdviceMessageServiceTest {

  private static final String CAIN002_FORIEGN_SALE_TRANSACTION_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN002ForiegnSaleTransactionTransactionMessage.json";

  private static final String CAIN001_FORIEGN_SALE_TRANSACTION_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN001ForiegnSaleTransactionTransactionMessage.json";

  private static final String CAIN002_ABROADCASH_WITHDRAWAL_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN002AbroadcashWithdrawalTransactionMessage.json";

  private static final String CAIN001_ABROADCASH_WITHDRAWAL_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN001AbroadcashWithdrawalTransactionMessage.json";

  private static final String CAIN002_DEBIT_CARD_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN002DebitCardTransactionMessage.json";

  private static final String CAIN001_DEBIT_CARD_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN001DebitCardTransactionMessage.json";

  private static final String CAIN002_CROSS_BORDER_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN002CrossBorderTransactionMessage.json";

  private static final String CAIN001_CROSS_BORDER_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN001CrossBorderTransactionMessage.json";

  private static final String CAIN001_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN001ReversalAdviceTransactionMessage.json";

  private static final String CAIN002_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON = "message/cardauthadvice/CAIN002ReversalAdviceTransactionMessage.json";

  private static final String TRANSACTIONID = "transactionId";
  private static final String CORRELATIONID = "correlationId";

  @Mock
  private CAIN002MessageBuilder cain002MessageBuilder;

  @Mock
  private PaymentDecisionClient paymentDecisionClient;

  @Mock
  private MessageSender messageSender;

  @Mock
  private MessageServiceProcessorHelper processorHelper;

  @Mock
  private MessageValidator messageValidator;

  @Mock
  private CardAuthResponseBuilder cardAuthResponseBuilder;

  @Mock
  private PaymentAuthorisations paymentAuthorisationsMock;

  @Mock
  private AuthorisationFinder authorisationFinderMock;

  @Mock
  private HttpServletRequest request;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

  @InjectMocks
  private CardAuthAdviceMessageService unit;

  private FileReaderUtil fileReader;

  private TransactionMessage message;
  private PaymentDecisionTransactionResponse paymentDecisionResponse;

  @BeforeEach
  public void setUp() {
    fileReader = new FileReaderUtil();
    message = createTransactionMessage();
    paymentDecisionResponse = new PaymentDecisionTransactionResponse();
    paymentDecisionResponse.setDecisionResponse(SUCCESS.name());
  }

  @Test
  public void shouldHandleCardAuthTransactionMessage() {
    assertEquals(CARD_AUTH_VIA_ADVICE, unit.getType());
  }

  @Test
  public void shouldValidateTransactionMessage() {
    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(message);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, message))
        .thenReturn(paymentProcessResponse);
    PaymentProcessResponse actual = unit.process(message, request);
    assertEquals(paymentProcessResponse, actual);
    verify(cain002MessageBuilder).getCain002Response(message, paymentDecisionResponse);
    verify(paymentDecisionClient).getPaymentDecision(message);
  }

  @Test
  public void shouldGenerateTransactionAndCorrelationIds() {
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(new TransactionMessage());
    unit.process(message, request);
    verify(processorHelper).generateTransactionAndCorrelationIds(message);
  }

  @Test
  public void shouldThrowExceptionWhenNotCardAuth() {
    TransactionMessage transactionMessage = mock(TransactionMessage.class);
    when(messageValidator.validateMessage(transactionMessage, CAIN001))
        .thenThrow(InvalidTransactionMessageException.class);
    assertThrows(InvalidTransactionMessageException.class, () -> {
      unit.processCardAuth(transactionMessage, request);
    });
  }

  @Test
  public void shouldCallPaymentDecision() {
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(new TransactionMessage());
    unit.processCardAuth(message, request);
    verify(paymentDecisionClient).getPaymentDecision(message);
  }

  @Test
  public void shouldCallPaymentDecisionWithoutGivenHTTPRequest() {
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(new TransactionMessage());
    unit.processCardAuth(message);
    verify(paymentDecisionClient).getPaymentDecision(message);
  }

  @Test
  public void shouldProcessPaymentsReversal() {
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(new TransactionMessage());
    unit.processCardAuth(message, request);
    verify(paymentDecisionClient).getPaymentDecision(message);
  }

  @Test
  public void shouldGenerateTransactionId() {
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(new TransactionMessage());

    unit.processCardAuth(message, request);
    verify(processorHelper).generateTransactionAndCorrelationIds(message);
  }

  @Test
  public void shouldBuildCardAuthCain002Response() {
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(new TransactionMessage());
    unit.processCardAuth(message, request);

    verify(cain002MessageBuilder)
        .getCain002Response(message, paymentDecisionResponse);
  }

  @Test
  public void shouldBuildCardAuthCain002ResponsePDFThrowsException() {
    CardAuthResponse cardAuthResponse = new CardAuthResponse();
    when(paymentDecisionClient.getPaymentDecision(message)).thenThrow(new RestClientException(""));
    when(cain002MessageBuilder.getCain002Response(message)).thenReturn(message);
    when(cardAuthResponseBuilder.buildCardAuthResponse(message)).thenReturn(cardAuthResponse);
    doNothing().when(processorHelper).generateTransactionAndCorrelationIds(message);
    doNothing().when(processorHelper).addTransactionStatus(message, APPROVED);

    CardAuthResponse actual = unit.processCardAuth(message, request);

    assertEquals(cardAuthResponse, actual);
    verify(cain002MessageBuilder).getCain002Response(message);
    verify(cardAuthResponseBuilder).buildCardAuthResponse(message);
  }

  @Test
  public void shouldBuildCardAuthCain002ResponseForDuplicateRequest() {

    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_DATE.name(), "2018-05-11T08:19:53.000+0000");
    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_AMOUNT.name(), new BigDecimal("20.00"));

    doReturn(Optional.of(new Authorisations()))
        .when(authorisationFinderMock)
        .getAuthorisation(anyString(), any(), any(), any(), anyBoolean());
    doReturn(new TransactionMessage()).when(cain002MessageBuilder).getCain002Response(message);
    unit.processCardAuth(message, request);

    verify(cain002MessageBuilder)
        .getCain002Response(message);

    verify(authorisationFinderMock)
        .getAuthorisation(anyString(), any(), any(), anyString(), anyBoolean());
  }

  @Test
  public void shouldBuildCardAuthCain002ResponseForFailedRequest() {

    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_DATE.name(), "2018-05-11T08:19:53.000+0000");
    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_AMOUNT.name(), new BigDecimal("20.00"));
    message.getMessages().get(0).getAdditionalInfo().put(SUBSCRIPTION_KEY.name(), "1234");
    message.getAdditionalInfo().put(TRANSACTION_STATUS.name(), FAILED.name());

    doReturn(Optional.of(new Authorisations()))
        .when(authorisationFinderMock)
        .getAuthorisation(anyString(), any(), any(), any(), anyBoolean());
    doReturn(new TransactionMessage()).when(cain002MessageBuilder).getCain002Response(message);
    when(lmClient.getAvailableBalanceFromLedger(any())).thenReturn(new BigDecimal("2"));
    unit.processCardAuth(message, request);

    verify(cain002MessageBuilder)
        .getCain002Response(message);

    verify(authorisationFinderMock)
        .getAuthorisation(anyString(), any(), any(), anyString(), anyBoolean());
  }

  @Test
  public void shouldBuildCardAuthCain002ResponseForFailedRequestAndLMThrowsError() {

    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_DATE.name(), "2018-05-11T08:19:53.000+0000");
    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_AMOUNT.name(), new BigDecimal("20.00"));
    message.getMessages().get(0).getAdditionalInfo().put(SUBSCRIPTION_KEY.name(), "1234");
    message.getAdditionalInfo().put(TRANSACTION_STATUS.name(), FAILED.name());

    doReturn(Optional.of(new Authorisations()))
        .when(authorisationFinderMock)
        .getAuthorisation(anyString(), any(), any(), any(), anyBoolean());
    doReturn(new TransactionMessage()).when(cain002MessageBuilder).getCain002Response(message);
    when(lmClient.getAvailableBalanceFromLedger(any())).thenThrow(new RuntimeException());
    unit.processCardAuth(message, request);

    verify(cain002MessageBuilder).getCain002Response(message);
    verify(authorisationFinderMock)
        .getAuthorisation(anyString(), any(), any(), anyString(), anyBoolean());
    verify(lmClient).getAvailableBalanceFromLedger(any());
  }

  @Test
  public void shouldBuildCardAuthResponse() {
    TransactionMessage cain002 = mock(TransactionMessage.class);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);
    unit.processCardAuth(message, request);
    verify(cardAuthResponseBuilder)
        .buildCardAuthResponse(paymentDecisionResponse, cain002);
  }

  @Test
  public void shouldBuildCardAuthResponseForDuplicateRequest() {
    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_DATE.name(), "2018-05-11T08:19:53.000+0000");
    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_AMOUNT.name(), new BigDecimal("20.00"));

    when(authorisationFinderMock.getAuthorisation(anyString(), any(), any(), any(), anyBoolean()))
        .thenReturn(
            Optional.of(new Authorisations()));

    TransactionMessage cain002 = mock(TransactionMessage.class);
    doReturn(cain002).when(cain002MessageBuilder).getCain002Response(message);
    unit.processCardAuth(message, request);
    verify(cardAuthResponseBuilder)
        .buildCardAuthResponse(cain002);
  }

  @Test
  public void shouldBuildCardAuthAndSaveAuthorisationsResponse() {
    TransactionMessage cain002 = new TransactionMessage();
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN002.name());

    Map<String, Object> messageMap = new HashMap();
    Map<String, Object> paymentAddtlInfo = new HashMap();
    Map<String, Object> tmAddtnlInfo = new HashMap();
    messageMap.put(AUTHORISATION_CODE.name(), "12345");
    messageMap.put(BANKNET_REFERENCE_NUMBER.name(), "1000");
    messageMap.put(MERCHANT_CATEGORY_CODE.name(), "5411");
    messageMap.put(TOTAL_AMOUNT.name(), BigDecimal.valueOf(1000L));
    messageMap.put(CARD_PROCESSOR_ACCOUNT_ID.name(), "1234");
    messageMap.put(Cain001Enum.TRANSACTION_DATE.name(), "2018-08-20T13:14:30.893+0000");
    paymentMessage.setMessage(messageMap);

    paymentAddtlInfo.put(TRANSACTION_ID.name(), TRANSACTIONID);
    paymentAddtlInfo.put(PAYMENT_METHOD_TYPE.name(), DOMESTIC_CASH_WITHDRAWAL.name());
    paymentMessage.setAdditionalInfo(paymentAddtlInfo);
    cain002.setMessages(asList(paymentMessage));

    tmAddtnlInfo.put(TRANSACTION_CORRELATION_ID.name(), CORRELATIONID);
    cain002.setAdditionalInfo(tmAddtnlInfo);

    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);

    unit.processCardAuth(message, request);

    ArgumentCaptor<ArrayList<Authorisations>> authorisations = ArgumentCaptor
        .forClass(ArrayList.class);

    verify(paymentAuthorisationsMock).saveAll(authorisations.capture());
    verifyNoMoreInteractions(paymentAuthorisationsMock);

    assertAuthorisation(expectedTransactionMessage(), authorisations.getValue().get(0), false);

    verify(cardAuthResponseBuilder)
        .buildCardAuthResponse(paymentDecisionResponse, cain002);
  }

  @Test
  public void shouldBuildCardAuthAndSaveAuthorisationsLMFailureResponse() throws IOException {
    TransactionMessage cain002 = new TransactionMessage();
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN002.name());

    Map<String, Object> messageMap = new HashMap();
    Map<String, Object> paymentAddtlInfo = new HashMap();
    Map<String, Object> tmAddtnlInfo = new HashMap();
    messageMap.put(AUTHORISATION_CODE.name(), "12345");
    messageMap.put(BANKNET_REFERENCE_NUMBER.name(), "1000");
    messageMap.put(MERCHANT_CATEGORY_CODE.name(), "5411");
    messageMap.put(TOTAL_AMOUNT.name(), BigDecimal.valueOf(1000L));
    messageMap.put(CARD_PROCESSOR_ACCOUNT_ID.name(), "1234");
    messageMap.put(Cain001Enum.TRANSACTION_DATE.name(), "2018-08-20T13:14:30.893+0000");
    paymentMessage.setMessage(messageMap);

    paymentAddtlInfo.put(TRANSACTION_ID.name(), TRANSACTIONID);
    paymentAddtlInfo.put(PAYMENT_METHOD_TYPE.name(), DOMESTIC_CASH_WITHDRAWAL.name());
    paymentMessage.setAdditionalInfo(paymentAddtlInfo);
    cain002.setMessages(asList(paymentMessage));

    tmAddtnlInfo.put(TRANSACTION_CORRELATION_ID.name(), CORRELATIONID);
    cain002.setAdditionalInfo(tmAddtnlInfo);

    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);

    unit.processCardAuth(message, request);

    ArgumentCaptor<ArrayList<Authorisations>> authorisations = ArgumentCaptor
        .forClass(ArrayList.class);

    verify(paymentAuthorisationsMock).saveAll(authorisations.capture());
    verifyNoMoreInteractions(paymentAuthorisationsMock);

    assertAuthorisation(expectedTransactionMessage(), authorisations.getValue().get(0), false);
  }

  @Test
  public void processAuthReversalAndSaveAuthorisation() throws IOException {
    TransactionMessage reversalMessage = stringToJson(
        fileReader.getFileContent(CAIN001_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    TransactionMessage cain002 = new TransactionMessage();
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN002.name());

    Map<String, Object> messageMap = new HashMap();
    Map<String, Object> paymentAddtlInfo = new HashMap();
    Map<String, Object> tmAddtnlInfo = new HashMap();
    messageMap.put(AUTHORISATION_CODE.name(), "12345");
    messageMap.put(BANKNET_REFERENCE_NUMBER.name(), "1000");
    messageMap.put(MERCHANT_CATEGORY_CODE.name(), Integer.valueOf("5411"));
    messageMap.put(TOTAL_AMOUNT.name(), BigDecimal.valueOf(1000L));
    messageMap.put(CARD_PROCESSOR_ACCOUNT_ID.name(), "1234");
    messageMap.put(Cain001Enum.TRANSACTION_DATE.name(), "2018-08-20T13:14:30.893+0000");
    paymentMessage.setMessage(messageMap);

    paymentAddtlInfo.put(TRANSACTION_ID.name(), TRANSACTIONID);
    paymentAddtlInfo.put(PAYMENT_METHOD_TYPE.name(), DOMESTIC_CASH_WITHDRAWAL.name());
    paymentMessage.setAdditionalInfo(paymentAddtlInfo);
    cain002.setMessages(asList(paymentMessage));

    tmAddtnlInfo.put(TRANSACTION_CORRELATION_ID.name(), CORRELATIONID);
    cain002.setAdditionalInfo(tmAddtnlInfo);
    Authorisations authForReversal = new Authorisations();
    authForReversal.setTransactionId("1234567890");
    authForReversal.setCorrelationId("1234567899");

    when(paymentDecisionClient.getPaymentDecision(reversalMessage))
        .thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(reversalMessage, paymentDecisionResponse))
        .thenReturn(cain002);
    when(authorisationFinderMock.getAuthorisationEntryForReversal(anyString(), anyString()))
        .thenReturn(authForReversal);

    unit.processCardAuth(reversalMessage, request);

    ArgumentCaptor<ArrayList<Authorisations>> authorisations = ArgumentCaptor
        .forClass(ArrayList.class);
    ArgumentCaptor<Authorisations> authforreversal = ArgumentCaptor.forClass(Authorisations.class);

    verify(paymentAuthorisationsMock).save(authforreversal.capture());
    verify(paymentAuthorisationsMock).saveAll(authorisations.capture());
    verifyNoMoreInteractions(paymentAuthorisationsMock);

    assertAuthorisation(expectedTransactionMessage(), authorisations.getValue().get(0), true);

    verify(cardAuthResponseBuilder)
        .buildCardAuthResponse(paymentDecisionResponse, cain002);
  }

  @Test
  public void shouldCheckTransactionCodeForCrossBorderCashWithdrawal() throws IOException {
    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_CROSS_BORDER_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_CROSS_BORDER_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, cain002Message))
        .thenReturn(paymentProcessResponse);

    PaymentProcessResponse actual = unit.process(transactionMessage, request);
    assertEquals(paymentProcessResponse, actual);
    verify(messageSender).sendPaymentMessage(any(), captor.capture());
    assertNotNull(captor.getValue());
    assertEquals("PMNT.CCRD.XBCW",
        captor.getValue().getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name())
    );
  }

  @Test
  public void shouldCheckTransactionCodeForDebitCardWithdrawal() throws IOException {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_DEBIT_CARD_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_DEBIT_CARD_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);

    unit.process(transactionMessage, request);
    verify(messageSender).sendPaymentMessage(any(), captor.capture());
    assertNotNull(captor.getValue());
    assertEquals("PMNT.CCRD.POSD",
        captor.getValue().getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name())
    );
  }

  @Test
  public void shouldCheckTransactionCodeForAbroadCashWithdrawal() throws IOException {
    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_ABROADCASH_WITHDRAWAL_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_ABROADCASH_WITHDRAWAL_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, cain002Message))
        .thenReturn(paymentProcessResponse);
    PaymentProcessResponse actual = unit.process(transactionMessage, request);
    assertEquals(paymentProcessResponse, actual);
    verify(messageSender).sendPaymentMessage(any(), captor.capture());
    assertNotNull(captor.getValue());
    assertEquals("PMNT.FCAT.FAFA",
        captor.getValue().getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name())
    );
  }

  @Test
  public void shouldCheckTransactionCodeForForiegnSaleTransaction() throws IOException {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_FORIEGN_SALE_TRANSACTION_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_FORIEGN_SALE_TRANSACTION_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);

    unit.process(transactionMessage, request);
    verify(messageSender).sendPaymentMessage(any(), captor.capture());
    assertNotNull(captor.getValue());
    assertEquals("UNKNOWN",
        captor.getValue().getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name())
    );
  }

  @Test
  public void shouldCheckTransactionCodeForReversalAdviceTransaction() throws IOException {
    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, cain002Message))
        .thenReturn(paymentProcessResponse);
    PaymentProcessResponse actual = unit.process(transactionMessage, request);
    assertEquals(paymentProcessResponse, actual);
    verify(messageSender).sendPaymentMessage(any(), captor.capture());
    assertNotNull(captor.getValue());
    assertEquals("NOT_AVAILABLE",
        captor.getValue().getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name())
    );
  }

  @Test
  public void generateTransactionCode_ShouldReturnUnknown() {
    TransactionMessage transactionMessage = buildTransactionMessage(
        INTERNATIONAL_POS_CHIP_AND_PIN.name());
    unit.generateTransactionCode(transactionMessage);
    String actual = transactionMessage.getMessages().get(0).getAdditionalInfo()
        .get(TRANSACTION_CODE.name()).toString();

    assertEquals(UNKNOWN.name(), actual);
  }

  @Test
  public void generateTransactionCode_ShouldReturnDomesticCashWithdrawal() {
    TransactionMessage transactionMessage = buildTransactionMessage(
        DOMESTIC_CASH_WITHDRAWAL.name());
    unit.generateTransactionCode(transactionMessage);
    String actual = transactionMessage.getMessages().get(0).getAdditionalInfo()
        .get(TRANSACTION_CODE.name()).toString();

    assertEquals("PMNT.CCRD.CWDL", actual);
  }

  @Test
  public void generateTransactionCode_ShouldReturnInternationalCashWithdrawal() {
    TransactionMessage transactionMessage = buildTransactionMessage(
        INTERNATIONAL_CASH_WITHDRAWAL.name());
    unit.generateTransactionCode(transactionMessage);
    String actual = transactionMessage.getMessages().get(0).getAdditionalInfo()
        .get(TRANSACTION_CODE.name()).toString();

    assertEquals("PMNT.CCRD.XBCW", actual);
  }

  @Test
  public void generateTransactionCode_ShouldReturnNotAvailable() {
    TransactionMessage transactionMessage = buildTransactionMessage(
        INTERNATIONAL_POS_CHIP_AND_PIN.name());
    transactionMessage.getMessages().get(0).getAdditionalInfo()
        .put(DEBIT_CREDIT_INDICATOR.name(), CREDIT.name());
    unit.generateTransactionCode(transactionMessage);
    String actual = transactionMessage.getMessages().get(0).getAdditionalInfo()
        .get(TRANSACTION_CODE.name()).toString();

    assertEquals(UNKNOWN.name(), actual);
  }

  private TransactionMessage buildTransactionMessage(String paymentMethodType) {
    TransactionMessage transactionMessage = new TransactionMessage();
    PaymentMessage paymentMessage = new PaymentMessage();
    Map<String, Object> additionalInfo = new HashMap<>();

    additionalInfo.put(PAYMENT_METHOD_TYPE.name(), paymentMethodType);
    paymentMessage.setAdditionalInfo(additionalInfo);
    transactionMessage.setMessages(singletonList(paymentMessage));

    return transactionMessage;
  }

  private void assertAuthorisation(TransactionMessage expected, Authorisations actual,
      boolean isReversal) {
    assertEquals(expected.getMessages().get(0).getMessage().get(AUTHORISATION_CODE.name()),
        actual.getId().getAuthorisationCode());
    assertEquals(expected.getMessages().get(0).getMessage().get(BANKNET_REFERENCE_NUMBER.name()),
        actual.getId().getBankNetReferenceNumber());
    assertEquals(expected.getMessages().get(0).getMessage().get(MERCHANT_CATEGORY_CODE.name()),
        actual.getMcc());
    assertEquals((expected.getMessages().get(0).getMessage().get(TOTAL_AMOUNT.name())),
        actual.getTransactionAmount());
    assertEquals(expected.getMessages().get(0).getMessage().get(CARD_PROCESSOR_ACCOUNT_ID.name()),
        actual.getTsysAccountId());
    assertEquals(actual.getId().getTransactionDate(), LocalDate.of(2018, 8, 20));
    assertEquals(false, actual.isExpired());
    assertEquals(isReversal, actual.isMatched());
    assertEquals(isReversal ? "REVERSAL" : "AUTH", actual.getTransactionType());
  }

  private TransactionMessage expectedTransactionMessage() {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.getHeader().setType(CARD_AUTH.name());
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN002.name());
    Map<String, Object> message = new HashMap();
    Map<String, Object> paymentAddtnlInfo = new HashMap();
    Map<String, Object> tmAddtnlInfo = new HashMap();
    message.put(AUTHORISATION_CODE.name(), "12345");
    message.put(BANKNET_REFERENCE_NUMBER.name(), "1000");
    message.put(MERCHANT_CATEGORY_CODE.name(), "5411");
    message.put(TOTAL_AMOUNT.name(), BigDecimal.valueOf(1000L));
    message.put(CARD_PROCESSOR_ACCOUNT_ID.name(), "1234");
    message.put(Cain001Enum.TRANSACTION_DATE.name(), "2018-08-20T13:14:30.893+0000");
    paymentMessage.setMessage(message);

    paymentAddtnlInfo.put(TRANSACTION_ID.name(), TRANSACTIONID);
    paymentMessage.setAdditionalInfo(paymentAddtnlInfo);
    transactionMessage.setMessages(asList(paymentMessage));

    tmAddtnlInfo.put(TRANSACTION_CORRELATION_ID.name(), CORRELATIONID);
    transactionMessage.setAdditionalInfo(tmAddtnlInfo);
    return transactionMessage;
  }

  private TransactionMessage createTransactionMessage() {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.getHeader().setType(CARD_AUTH.name());
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN001.name());
    Map<String, Object> message = new HashMap();
    message.put(Cain001Enum.TRANSACTION_DATE.name(), "2018-08-20T13:14:30.893+0000");
    message.put(AUTHORISATION_CODE.name(), "12345");
    message.put(BANKNET_REFERENCE_NUMBER.name(), "1000");
    message.put(MERCHANT_CATEGORY_CODE.name(), Integer.valueOf("5411"));
    message.put(TOTAL_AMOUNT.name(), BigDecimal.valueOf(1000L));
    paymentMessage.setMessage(message);
    Map<String, Object> addtlInfo = new HashMap();
    addtlInfo.put(TRANSACTION_ID.name(), TRANSACTIONID);
    addtlInfo.put(PAYMENT_METHOD_TYPE.name(), DOMESTIC_CASH_WITHDRAWAL.name());
    paymentMessage.setAdditionalInfo(addtlInfo);
    transactionMessage.setMessages(singletonList(paymentMessage));
    addtlInfo.clear();
    addtlInfo.put(TRANSACTION_CORRELATION_ID.name(), CORRELATIONID);
    addtlInfo.put(PAYMENT_METHOD_TYPE.name(), DOMESTIC_CASH_WITHDRAWAL.name());
    transactionMessage.setAdditionalInfo(addtlInfo);
    return transactionMessage;
  }

}
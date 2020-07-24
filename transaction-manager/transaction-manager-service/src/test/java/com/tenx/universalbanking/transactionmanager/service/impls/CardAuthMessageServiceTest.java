package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TOTAL_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.DebitCreditIndicatorEnum.CREDIT;
import static com.tenx.universalbanking.transactionmessage.enums.DebitCreditIndicatorEnum.DEBIT;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.RESERVE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.DEBIT_CREDIT_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.DOMESTIC_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.INTERNATIONAL_POS_CHIP_AND_PIN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMethodTypeEnum.POS_MAG_STRIPE;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.CardAuthResponseBuilder;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.client.PaymentDecisionClient;
import com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.messagebuilder.CAIN002MessageBuilder;
import com.tenx.universalbanking.transactionmanager.service.validation.MessageValidator;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Cain001Enum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import org.mockito.internal.verification.Times;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class CardAuthMessageServiceTest {

  private static final String CAIN002_FORIEGN_SALE_TRANSACTION_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN002ForiegnSaleTransactionTransactionMessage.json";

  private static final String CAIN001_FORIEGN_SALE_TRANSACTION_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN001ForiegnSaleTransactionTransactionMessage.json";

  private static final String CAIN002_ABROADCASH_WITHDRAWAL_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN002AbroadcashWithdrawalTransactionMessage.json";

  private static final String CAIN001_ABROADCASH_WITHDRAWAL_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN001AbroadcashWithdrawalTransactionMessage.json";

  private static final String CAIN002_DEBIT_CARD_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN002DebitCardTransactionMessage.json";

  private static final String CAIN001_DEBIT_CARD_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN001DebitCardTransactionMessage.json";

  private static final String CAIN002_CROSS_BORDER_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN002CrossBorderTransactionMessage.json";

  private static final String CAIN001_CROSS_BORDER_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN001CrossBorderTransactionMessage.json";

  private static final String CAIN001_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN001ReversalAdviceTransactionMessage.json";

  private static final String CAIN002_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON = "message/cardauth/CAIN002ReversalAdviceTransactionMessage.json";

  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";


  private static final String TRANSACTIONID = "transactionId";
  private static final String CORRELATIONID = "correlationId";
  private static final String REQUEST_ID = "requestId";

  @Mock
  private CAIN002MessageBuilder cain002MessageBuilder;

  @Mock
  private PaymentDecisionClient paymentDecisionClient;

  @Mock
  private MessageSender messageSender;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

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
  private ReconciliationHelper reconciliationHelper;

  @InjectMocks
  private CardAuthMessageService unit;

  private FileReaderUtil fileReader;

  private TransactionMessage message;
  private PaymentDecisionTransactionResponse paymentDecisionResponse;

  @BeforeEach
  public void setUp() {
    fileReader = new FileReaderUtil();
    message = createTransactionMessage(BigDecimal.valueOf(1000L));
    paymentDecisionResponse = new PaymentDecisionTransactionResponse();
    paymentDecisionResponse.setDecisionResponse(SUCCESS.name());
  }

  @Test
  public void shouldHandleCardAuthTransactionMessage() {
    assertEquals(CARD_AUTH, unit.getType());
  }

  @Test
  public void shouldValidateTransactionMessage() throws Exception {
    PaymentProcessResponse expected = new PaymentProcessResponse();
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    doNothing().when(processorHelper).generateTransactionAndCorrelationIds(message);
    doNothing().when(processorHelper).addTransactionStatus(message, RESERVE);
    when(messageValidator.validateMessage(message, CAIN001)).thenReturn(true);
    doNothing().when(messageSender).sendPaymentMessage("", message);
    when(processorHelper.getSubscriptionKey(message, CAIN001)).thenReturn("");
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, message))
        .thenReturn(expected);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(message);
    PaymentProcessResponse actual = unit.process(message, request);
    verify(messageSender).sendPaymentMessage("", message);
    verify(lmClient).postTransactionToLedger(any());
    assertEquals(expected, actual);
  }

  @Test
  public void shouldValidateTransactionMessageLMFailure() throws Exception {
    PaymentProcessResponse expected = new PaymentProcessResponse();
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);
    doNothing().when(processorHelper).generateTransactionAndCorrelationIds(message);
    doNothing().when(processorHelper).addTransactionStatus(message, RESERVE);
    when(messageValidator.validateMessage(message, CAIN001)).thenReturn(true);
    when(processorHelper.getSubscriptionKey(message, CAIN001)).thenReturn("");
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(responseConverter.buildPaymentProcessResponse(lmPostingResponse, message))
        .thenReturn(expected);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(message);
    PaymentProcessResponse actual = unit.process(message, request);
    verify(lmClient).postTransactionToLedger(any());
    assertEquals(expected, actual);
  }

  @Test
  public void shouldNotFailWhenNoRequest() throws IOException {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS), LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any(), anyBoolean())).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(buildCain002Response());
    unit.processCardAuth(message);
    verify(processorHelper, never()).addTracingHeaders(any(TransactionMessage.class), any());
  }

  @Test
  public void shouldGenerateTransactionAndCorrelationIds() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
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
  public void shouldReturnErrorMessageWhenNegativeAmountInCardAuth() throws IOException {
    TransactionMessage message = createTransactionMessage(BigDecimal.valueOf(-10L));
    TransactionMessage returnTransactionMessage = new TransactionMessage();
    CardAuthResponse cardAuthResponse = new CardAuthResponse();
    cardAuthResponse.setReason(new ReasonDto());
    PaymentDecisionTransactionResponse paymentDecisionResponse = new PaymentDecisionTransactionResponse();
    paymentDecisionResponse.setDecisionResponse("FAILED");

    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(returnTransactionMessage);
    when(cardAuthResponseBuilder
        .buildCardAuthResponse(paymentDecisionResponse, returnTransactionMessage))
        .thenReturn(cardAuthResponse);
    unit.processCardAuth(message, request);

    verify(cain002MessageBuilder)
        .getCain002Response(message, paymentDecisionResponse);
    verify(processorHelper, new Times(0)).addTracingHeaders(returnTransactionMessage, request);

    verify(lmClient, new Times(0)).postTransactionToLedger(returnTransactionMessage, false);
  }

  @Test
  public void shouldCallPaymentDecision() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    TransactionMessage cain002 = buildCain002Response();
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(buildCain002Response());
    unit.processCardAuth(message, request);
    verify(paymentDecisionClient).getPaymentDecision(message);
  }

  @Test
  public void shouldCallPaymentDecisionWithDEBIT_CREDIT_IndicatorEnum() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    TransactionMessage cain002 = buildCain002Response();
    message.getMessages().get(0).getAdditionalInfo().put(DEBIT_CREDIT_INDICATOR.name(), DEBIT);
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(buildCain002Response());
    unit.processCardAuth(message, request);
    verify(paymentDecisionClient).getPaymentDecision(message);
  }

  @Test
  public void whenReturnFailedFromPDFWithDecisionReasonDifferent() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    CardAuthResponse cardAuthResponse = buildCardAuthResponse(message);
    TransactionMessage cain002 = buildCain002Response();
    PaymentDecisionTransactionResponse paymentDecisionResponse = getFailurePaymentDecisionTransactionResponse();
    paymentDecisionResponse.getDecisionReason()
        .setCode(TransactionResponseReasonCodes.RULES_FAILED_CODE);
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);
    when(cardAuthResponseBuilder.buildCardAuthResponse(paymentDecisionResponse, cain002))
        .thenReturn(cardAuthResponse);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(
        buildReconciliationMessage(buildCain002Response(), Event.INT_REJECT));
    CardAuthResponse actual = unit.processCardAuth(message, request);
    verify(reconciliationHelper).saveReconciliationMessage(
        buildReconciliationMessage(buildCain002Response(), Event.INT_REJECT));
    verify(paymentDecisionClient).getPaymentDecision(message);
    assertEquals(cardAuthResponse, actual);
  }

  @Test
  public void whenReturnFailedFromPDFWithAmountBigInteger() throws Exception {
    message.getMessages().get(0).getMessage().put("TOTAL_AMOUNT", new BigInteger("2"));
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    CardAuthResponse cardAuthResponse = buildCardAuthResponse(message);
    TransactionMessage cain002 = buildCain002Response();
    PaymentDecisionTransactionResponse paymentDecisionResponse = getFailurePaymentDecisionTransactionResponse();
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);
    when(cardAuthResponseBuilder.buildCardAuthResponse(paymentDecisionResponse, cain002))
        .thenReturn(cardAuthResponse);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(
        buildReconciliationMessage(buildCain002Response(), Event.INT_REJECT_FUNDS));
    CardAuthResponse actual = unit.processCardAuth(message, request);
    verify(reconciliationHelper).saveReconciliationMessage(
        buildReconciliationMessage(buildCain002Response(), Event.INT_REJECT_FUNDS));
    verify(paymentDecisionClient).getPaymentDecision(message);
    assertEquals(cardAuthResponse, actual);
  }

  @Test
  public void whenReturnFailedFromPDFWithAmountNumber() throws Exception {
    message.getMessages().get(0).getMessage().put("TOTAL_AMOUNT", 5);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    CardAuthResponse cardAuthResponse = buildCardAuthResponse(message);
    TransactionMessage cain002 = buildCain002Response();
    PaymentDecisionTransactionResponse paymentDecisionResponse = getFailurePaymentDecisionTransactionResponse();
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);
    when(cardAuthResponseBuilder.buildCardAuthResponse(paymentDecisionResponse, cain002))
        .thenReturn(cardAuthResponse);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(
        buildReconciliationMessage(buildCain002Response(), Event.INT_REJECT_FUNDS));
    CardAuthResponse actual = unit.processCardAuth(message, request);
    verify(reconciliationHelper).saveReconciliationMessage(
        buildReconciliationMessage(buildCain002Response(), Event.INT_REJECT_FUNDS));
    verify(paymentDecisionClient).getPaymentDecision(message);
    assertEquals(cardAuthResponse, actual);
  }

  @Test
  public void whenReturnFailedFromPDF() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    CardAuthResponse cardAuthResponse = buildCardAuthResponse(message);
    TransactionMessage cain002 = buildCain002Response();
    PaymentDecisionTransactionResponse paymentDecisionResponse = getFailurePaymentDecisionTransactionResponse();
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);
    when(cardAuthResponseBuilder.buildCardAuthResponse(paymentDecisionResponse, cain002))
        .thenReturn(cardAuthResponse);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(
        buildReconciliationMessage(buildCain002Response(), Event.INT_REJECT_FUNDS));
    CardAuthResponse actual = unit.processCardAuth(message, request);
    verify(reconciliationHelper).saveReconciliationMessage(
        buildReconciliationMessage(buildCain002Response(), Event.INT_REJECT_FUNDS));
    verify(paymentDecisionClient).getPaymentDecision(message);
    assertEquals(cardAuthResponse, actual);
  }

  @Test
  public void shouldSetTransactionCodeForMagStripe() throws IOException {
    TransactionMessage transactionMessageForMagStripe = createTransactionMessage(
        BigDecimal.valueOf(1000L));
    transactionMessageForMagStripe.getAdditionalInfo()
        .put(PAYMENT_METHOD_TYPE.name(), POS_MAG_STRIPE.name());
    TransactionMessage cain002 = buildCain002();
    when(messageValidator
        .validateMessage(transactionMessageForMagStripe, PaymentMessageTypeEnum.CAIN001))
        .thenReturn(true);
    doNothing().when(processorHelper).sendtoKafka(cain002, PaymentMessageTypeEnum.CAIN002);
    when(paymentDecisionClient.getPaymentDecision(transactionMessageForMagStripe))
        .thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessageForMagStripe, paymentDecisionResponse))
        .thenReturn(cain002);
    when(cardAuthResponseBuilder.buildCardAuthResponse(paymentDecisionResponse, cain002))
        .thenReturn(buildCardAuthResponse(cain002));
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);

    CardAuthResponse actualCardAuthResponse = unit
        .processCardAuth(transactionMessageForMagStripe, request);

    ArgumentCaptor<ArrayList<Authorisations>> authorisations = ArgumentCaptor
        .forClass(ArrayList.class);
    ArgumentCaptor<TransactionMessage> transactionMessageArgumentCaptor = ArgumentCaptor
        .forClass(TransactionMessage.class);
    ArgumentCaptor<TransactionStatusValueEnum> enumCaptor = ArgumentCaptor
        .forClass(TransactionStatusValueEnum.class);
    verify(paymentAuthorisationsMock).saveAll(authorisations.capture());
    verify(processorHelper)
        .addTransactionStatus(transactionMessageArgumentCaptor.capture(), enumCaptor.capture());
    assertAuthorisation(expectedTransactionMessage(), authorisations.getValue().get(0), false);
    assertEquals(RESERVE, enumCaptor.getValue());
    assertEquals(transactionMessageForMagStripe, transactionMessageArgumentCaptor.getValue());
    assertCardAuthResponse(actualCardAuthResponse, cain002);
  }

  @Test
  public void shouldProcessPaymentsReversal() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    TransactionMessage cain002 = buildCain002Response();
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(buildCain002Response());
    unit.processCardAuth(message, request);
    verify(paymentDecisionClient).getPaymentDecision(message);
  }

  @Test
  public void shouldGenerateTransactionId() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    TransactionMessage cain002 = buildCain002Response();
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder.getCain002Response(message, paymentDecisionResponse))
        .thenReturn(buildCain002Response());

    unit.processCardAuth(message, request);
    verify(processorHelper).generateTransactionAndCorrelationIds(message);
  }

  @Test
  public void shouldBuildCardAuthCain002Response() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    TransactionMessage cain002 = buildCain002();
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);
    unit.processCardAuth(message, request);

    verify(cain002MessageBuilder)
        .getCain002Response(message, paymentDecisionResponse);
  }

  @Test
  public void shouldBuildCardAuthCain002ResponseForDuplicateRequest() {

    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_DATE.name(), "2018-05-11T08:19:53.000+0000");
    message.getMessages().get(0).getMessage()
        .put(TRANSACTION_AMOUNT.name(), new BigDecimal("20.00"));
    message.getMessages().get(0).getAdditionalInfo()
        .put(DEBIT_CREDIT_INDICATOR.name(), CREDIT.name());

    doReturn(Optional.of(new Authorisations()))
        .when(authorisationFinderMock)
        .getAuthorisation(anyString(), any(), any(), any(), anyBoolean());
    doReturn(new TransactionMessage()).when(cain002MessageBuilder).getCain002Response(message);
    unit.processCardAuth(message, request);

    verify(cain002MessageBuilder)
        .getCain002Response(message);
  }

  @Test
  public void shouldBuildCardAuthResponse() throws Exception {
    TransactionMessage cain002 = buildCain002Response();
    when(paymentDecisionClient.getPaymentDecision(message)).thenReturn(paymentDecisionResponse);
    when(cain002MessageBuilder
        .getCain002Response(message, paymentDecisionResponse))
        .thenReturn(cain002);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);
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
    message.getMessages().get(0).getAdditionalInfo()
        .put(DEBIT_CREDIT_INDICATOR.name(), CREDIT.name());

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
  public void shouldBuildCardAuthAndSaveAuthorisationsResponse() throws Exception {
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
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);

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
  public void shouldBuildCardAuthAndSaveAuthorisationsLMFailureResponse() throws Exception {
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
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);

    when(lmClient.postTransactionToLedger(cain002, false)).thenReturn(lmPostingResponse);

    unit.processCardAuth(message, request);

    ArgumentCaptor<ArrayList<Authorisations>> authorisations = ArgumentCaptor
        .forClass(ArrayList.class);

    verify(paymentAuthorisationsMock).saveAll(authorisations.capture());
    verifyNoMoreInteractions(paymentAuthorisationsMock);

    assertAuthorisation(expectedTransactionMessage(), authorisations.getValue().get(0), false);

    verify(cardAuthResponseBuilder)
        .buildCardAuthResponse(lmPostingResponse, message);
  }


  @Test
  public void provessAuthReversalAndSaveAuthorisation() throws IOException {
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
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(cain002, true)).thenReturn(lmPostingResponse);
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
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_CROSS_BORDER_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_CROSS_BORDER_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);
    when(processorHelper.getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.CAIN001))
        .thenReturn("12345");
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, cain002Message))
        .thenReturn(buildPaymentProcessResponse(paymentDecisionResponse, cain002Message));
    PaymentProcessResponse actualPaymentProcessResponse = unit.process(transactionMessage, request);
    verify(messageSender).sendPaymentMessage("12345", cain002Message);
    assertNotNull(actualPaymentProcessResponse);
    assertEquals("PMNT.CCRD.XBCW",
        actualPaymentProcessResponse.getTransactionMessage().getMessages().get(0)
            .getAdditionalInfo().get("TRANSACTION_CODE"));
  }

  @Test
  public void shouldCheckTransactionCodeForDebitCardWithdrawal() throws IOException {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_DEBIT_CARD_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(processorHelper.getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.CAIN001))
        .thenReturn("12345");
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_DEBIT_CARD_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, cain002Message))
        .thenReturn(buildPaymentProcessResponse(paymentDecisionResponse, cain002Message));

    PaymentProcessResponse actualPaymentProcessResponse = unit.process(transactionMessage, request);
    verify(messageSender).sendPaymentMessage("12345", cain002Message);
    assertNotNull(actualPaymentProcessResponse);
    assertEquals("PMNT.CCRD.POSD",
        actualPaymentProcessResponse.getTransactionMessage().getMessages().get(0)
            .getAdditionalInfo().get("TRANSACTION_CODE"));
  }

  @Test
  public void shouldCheckTransactionCodeForAbroadCashWithdrawal() throws IOException {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_ABROADCASH_WITHDRAWAL_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    when(processorHelper.getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.CAIN001))
        .thenReturn("12345");
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_ABROADCASH_WITHDRAWAL_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, cain002Message))
        .thenReturn(buildPaymentProcessResponse(paymentDecisionResponse, cain002Message));
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    PaymentProcessResponse actualPaymentProcessResponse = unit.process(transactionMessage, request);
    verify(messageSender).sendPaymentMessage("12345", cain002Message);
    assertNotNull(actualPaymentProcessResponse);
    assertEquals("PMNT.FCAT.FAFA",
        actualPaymentProcessResponse.getTransactionMessage().getMessages().get(0)
            .getAdditionalInfo().get("TRANSACTION_CODE"));
  }

  @Test
  public void shouldCheckTransactionCodeForForiegnSaleTransaction() throws IOException {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_FORIEGN_SALE_TRANSACTION_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    when(processorHelper.getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.CAIN001))
        .thenReturn("12345");
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_FORIEGN_SALE_TRANSACTION_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, cain002Message))
        .thenReturn(buildPaymentProcessResponse(paymentDecisionResponse, cain002Message));
    PaymentProcessResponse actualPaymentProcessResponse = unit.process(transactionMessage, request);
    verify(messageSender).sendPaymentMessage("12345", cain002Message);
    assertNotNull(actualPaymentProcessResponse);
    assertEquals("UNKNOWN",
        actualPaymentProcessResponse.getTransactionMessage().getMessages().get(0)
            .getAdditionalInfo().get("TRANSACTION_CODE"));
  }

  @Test
  public void shouldCheckTransactionCodeForReversalAdviceTransaction() throws IOException {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CAIN001_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(processorHelper.getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.CAIN001))
        .thenReturn("12345");
    when(paymentDecisionClient.getPaymentDecision(transactionMessage))
        .thenReturn(paymentDecisionResponse);
    TransactionMessage cain002Message = stringToJson(
        fileReader.getFileContent(CAIN002_REVERSAL_ADVICE_TRANSACTION_MESSAGE_JSON),
        TransactionMessage.class);
    when(cain002MessageBuilder
        .getCain002Response(transactionMessage, paymentDecisionResponse))
        .thenReturn(cain002Message);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(responseConverter.buildPaymentProcessResponse(paymentDecisionResponse, cain002Message))
        .thenReturn(buildPaymentProcessResponse(paymentDecisionResponse, cain002Message));
    when(lmClient.postTransactionToLedger(cain002Message)).thenReturn(lmPostingResponse);
    PaymentProcessResponse actualPaymentProcessResponse = unit.process(transactionMessage, request);
    verify(messageSender).sendPaymentMessage("12345", cain002Message);
    assertNotNull(actualPaymentProcessResponse);
    assertEquals("NOT_AVAILABLE",
        actualPaymentProcessResponse.getTransactionMessage().getMessages().get(0)
            .getAdditionalInfo().get("TRANSACTION_CODE"));
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

  private void assertCardAuthResponse(CardAuthResponse actualCardAuthResponse,
      TransactionMessage cain002) {
    assertEquals(cain002, actualCardAuthResponse.getCain002Response());
  }

  private CardAuthResponse buildCardAuthResponse(TransactionMessage cain002) {
    CardAuthResponse cardAuthResponse = new CardAuthResponse();
    cardAuthResponse.setCain002Response(cain002);
    cardAuthResponse.setCardAuthStatus("SUCCESS");
    return cardAuthResponse;
  }

  private TransactionMessage buildCain002() {
    TransactionMessage cain002 = new TransactionMessage();
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN002.name());
    Map<String, Object> messageMap = buildMessageMap();
    Map<String, Object> paymentAddtlInfo = buildPaymentAdditionalInfo();
    Map<String, Object> tmAddtnlInfo = buildTmAdditionalInfo();
    paymentMessage.setMessage(messageMap);
    paymentMessage.setAdditionalInfo(paymentAddtlInfo);
    cain002.setMessages(asList(paymentMessage));
    cain002.setAdditionalInfo(tmAddtnlInfo);
    return cain002;
  }

  private Map<String, Object> buildTmAdditionalInfo() {
    Map<String, Object> tmAddtnlInfo = new HashMap();
    tmAddtnlInfo.put(TRANSACTION_CORRELATION_ID.name(), CORRELATIONID);
    return tmAddtnlInfo;
  }

  private Map<String, Object> buildPaymentAdditionalInfo() {
    Map<String, Object> paymentAddtlInfo = new HashMap();
    paymentAddtlInfo.put(TRANSACTION_ID.name(), TRANSACTIONID);
    paymentAddtlInfo.put(PAYMENT_METHOD_TYPE.name(), POS_MAG_STRIPE.name());
    return paymentAddtlInfo;
  }

  private Map<String, Object> buildMessageMap() {
    Map<String, Object> messageMap = new HashMap();
    messageMap.put(AUTHORISATION_CODE.name(), "12345");
    messageMap.put(BANKNET_REFERENCE_NUMBER.name(), "1000");
    messageMap.put(MERCHANT_CATEGORY_CODE.name(), "5411");
    messageMap.put(TOTAL_AMOUNT.name(), BigDecimal.valueOf(1000L));
    messageMap.put(CARD_PROCESSOR_ACCOUNT_ID.name(), "1234");
    messageMap.put(Cain001Enum.TRANSACTION_DATE.name(), "2018-08-20T13:14:30.893+0000");
    return messageMap;
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage,
      Event event) {
    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(event).
        scope(TransactionMessageTypeEnum.CARD_AUTH).
        serviceName(ServiceNames.TRANSACTION_MANAGER).
        build();
  }

  private PaymentDecisionTransactionResponse getFailurePaymentDecisionTransactionResponse() {
    PaymentDecisionTransactionResponse paymentDecisionResponse = new PaymentDecisionTransactionResponse();
    paymentDecisionResponse.setDecisionResponse(FAILED.name());
    PaymentDecisionReasonDTO paymentDecisionReasonDTO = new PaymentDecisionReasonDTO();
    paymentDecisionReasonDTO
        .setCode(TransactionResponseReasonCodes.PAYMENT_RESERVE_FAILED_CODE);
    paymentDecisionResponse.setDecisionReason(paymentDecisionReasonDTO);
    return paymentDecisionResponse;
  }

  private TransactionMessage buildCain002Response() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put(TRANSACTION_CORRELATION_ID.name(), "67458990");
    transactionMessage.setAdditionalInfo(additionalInfo);
    return transactionMessage;
  }

  private void assertAuthorisation(TransactionMessage expected, Authorisations actual,
      boolean isReversal) {
    Map<String, Object> expectedMessage = expected.getMessages().get(0).getMessage();
    assertEquals(expectedMessage.get(AUTHORISATION_CODE.name()),
        actual.getId().getAuthorisationCode());
    assertEquals(expectedMessage.get(BANKNET_REFERENCE_NUMBER.name()),
        actual.getId().getBankNetReferenceNumber());
    assertEquals(expectedMessage.get(MERCHANT_CATEGORY_CODE.name()),
        actual.getMcc());
    assertEquals((expectedMessage.get(TOTAL_AMOUNT.name())),
        actual.getTransactionAmount());
    assertEquals(expectedMessage.get(CARD_PROCESSOR_ACCOUNT_ID.name()),
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

  private PaymentProcessResponse buildPaymentProcessResponse(
      PaymentDecisionTransactionResponse paymentDecisionResponse,
      TransactionMessage cain002Message) {
    PaymentProcessResponse paymentDecisionTransactionResponse = new PaymentProcessResponse();
    paymentDecisionTransactionResponse.setTransactionMessage(cain002Message);
    return paymentDecisionTransactionResponse;
  }

  private TransactionMessage createTransactionMessage(BigDecimal amount) {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.getHeader().setType(CARD_AUTH.name());
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN001.name());
    Map<String, Object> message = new HashMap();
    message.put(Cain001Enum.TRANSACTION_DATE.name(), "2018-08-20T13:14:30.893+0000");
    message.put(AUTHORISATION_CODE.name(), "12345");
    message.put(BANKNET_REFERENCE_NUMBER.name(), "1000");
    message.put(MERCHANT_CATEGORY_CODE.name(), Integer.valueOf("5411"));
    message.put(AMOUNT.name(), BigDecimal.valueOf(1000L));
    message.put(TOTAL_AMOUNT.name(), amount);
    message.put(TRANSACTION_CURRENCY_CODE.name(), "GBP");
    paymentMessage.setMessage(message);
    Map<String, Object> addtlInfo = new HashMap();
    addtlInfo.put(TRANSACTION_ID.name(), TRANSACTIONID);
    addtlInfo.put(PAYMENT_METHOD_TYPE.name(), DOMESTIC_CASH_WITHDRAWAL.name());
    paymentMessage.setAdditionalInfo(addtlInfo);
    transactionMessage.setMessages(singletonList(paymentMessage));
    addtlInfo.clear();
    addtlInfo.put(TRANSACTION_CORRELATION_ID.name(), CORRELATIONID);
    addtlInfo.put(TransactionMessageAdditionalInfoEnum.REQUEST_ID.name(), REQUEST_ID);
    addtlInfo.put(PAYMENT_METHOD_TYPE.name(), DOMESTIC_CASH_WITHDRAWAL.name());
    transactionMessage.setAdditionalInfo(addtlInfo);
    return transactionMessage;
  }

}
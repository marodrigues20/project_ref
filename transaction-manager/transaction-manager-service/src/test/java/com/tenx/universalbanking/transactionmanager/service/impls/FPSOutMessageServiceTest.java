package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.Pacs002Enum.OUTCOME;
import static com.tenx.universalbanking.transactionmessage.enums.Pain002Enum.PAYMENT_TYPE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PAIN002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_DEBIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.FPSOutPain001RequestHandler;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class FPSOutMessageServiceTest {

  @Mock
  private MessageSender messageSender;

  @Mock
  private HttpServletRequest request;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

  @Mock
  private FPSOutPain001RequestHandler handler;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @Mock
  private TMExceptionBuilder exceptionBuilder;

  @Mock
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @InjectMocks
  private FPSOutMessageService unit;

  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";

  private final String TRANSACTION_MESSAGE = "message/FPSOutTransactionMessageRequest.json";

  private final String FPSOutPain001ResponseSuccess = "message/FPSOutPainACKSuccessfulResponse.json";

  private final FileReaderUtil fileReader = new FileReaderUtil();

  private TransactionMessage transactionMessage;

  private com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage pdfTransactionMessage;

  private com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessage ppTransactionMessage;

  @BeforeEach
  public void init() throws IOException {
    transactionMessage = buildStub(TRANSACTION_MESSAGE, TransactionMessage.class);
  }

  @Test
  public void shouldHandleFpsOutMessage() {
    assertEquals(TransactionMessageTypeEnum.FPS_OUT, unit.getType());
  }

  @Test
  public void testInvalidNumberOfPaymentMessages() throws IOException {

    transactionMessage.getMessages().add(buildPaymentMessage());

    PaymentProcessResponse actual = unit.process(transactionMessage, request);
    assertEquals("FAILED", actual.getPaymentStatus());
    assertEquals(2100, actual.getReason().getCode());
  }

  @Test
  public void executeTransactionMessageSuccessTest() throws IOException {

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);

    TransactionMessage painACkTxResponse = stringToJson(
        fileReader.getFileContent(FPSOutPain001ResponseSuccess),
        TransactionMessage.class);

    ArgumentCaptor<TransactionMessage> transactionMessageArgumentCaptor =
        forClass(TransactionMessage.class);

    when(handler.process(any(), any())).thenReturn(painACkTxResponse);

    PaymentProcessResponse response = unit.process(transactionMessage, request);

    assertEquals(PaymentDecisionResponse.SUCCESS.name(), response.getPaymentStatus());

    verify(messageSender).sendPaymentMessage(eq(getSubscriptionKey(painACkTxResponse)),
        transactionMessageArgumentCaptor.capture());

    assertEquals(SUCCESS.name(), transactionMessageArgumentCaptor.getValue().getAdditionalInfo()
        .get(TRANSACTION_STATUS.name()));

    assertEquals(PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_DEBIT.getValue(),
        transactionMessageArgumentCaptor.getValue().getMessages().get(0).getAdditionalInfo()
            .get(TRANSACTION_CODE.name()));

    assertTrue(transactionMessageArgumentCaptor.getValue().getMessages().stream()
        .anyMatch(msg -> msg.getType().equals(PaymentMessageTypeEnum.PAIN001_ACK.name())));
  }

  @Test
  public void executeTransactionMessageOutcomeFailedTest() throws IOException {

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);

    TransactionMessage painACkTxResponse = stringToJson(
        fileReader.getFileContent(FPSOutPain001ResponseSuccess),
        TransactionMessage.class);
    painACkTxResponse.getMessages().get(0).getMessage().put("OUTCOME", "FAILED");
    painACkTxResponse.getMessages().get(0).getMessage().put("RESPONSE_CODE", "1");
    ArgumentCaptor<TransactionMessage> transactionMessageArgumentCaptor =
        forClass(TransactionMessage.class);

    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(handler.process(any(), any())).thenReturn(painACkTxResponse);

    PaymentProcessResponse response = unit.process(transactionMessage, request);

    assertEquals(PaymentDecisionResponse.FAILED.name(), response.getPaymentStatus());

    verify(messageSender).sendPaymentMessage(eq(getSubscriptionKey(painACkTxResponse)),
        transactionMessageArgumentCaptor.capture());

    assertEquals(FAILED.name(), transactionMessageArgumentCaptor.getValue().getAdditionalInfo()
        .get(TRANSACTION_STATUS.name()));

    assertEquals(PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_DEBIT.getValue(),
        transactionMessageArgumentCaptor.getValue().getMessages().get(0).getAdditionalInfo()
            .get(TRANSACTION_CODE.name()));

    assertTrue(transactionMessageArgumentCaptor.getValue().getMessages().stream()
        .anyMatch(msg -> msg.getType().equals(PaymentMessageTypeEnum.PAIN001_ACK.name())));
  }

  @Test
  public void executeTransactionMessageLMPostingFailureTest() throws IOException {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);

    TransactionMessage painACkTxResponse = stringToJson(
        fileReader.getFileContent(FPSOutPain001ResponseSuccess),
        TransactionMessage.class);

    when(responseConverter.buildPaymentProcessResponse(lmPostingResponse, painACkTxResponse))
        .thenReturn(buildLMFailurePaymentResponse());
    doNothing().when(messageServiceProcessorHelper).addTracingHeaders(any(), any());
    when(handler.process(any(), any())).thenReturn(painACkTxResponse);

    PaymentProcessResponse response = unit.process(transactionMessage, request);

    assertEquals(PaymentDecisionResponse.FAILED.name(), response.getPaymentStatus());
    assertNotNull(response.getReason());
    assertEquals("5301", response.getReason().getCode().toString());
  }

  @Test
  public void testLMPain001GateWayTimeoutFailureException() throws IOException {

    when(handler.process(any(), any()))
        .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));
    when(exceptionBuilder.buildFPSOutTransactionManagerException(
        INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getStatusCode(),
        INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
        .thenReturn(new FPSOutTransactionManagerException());
    assertThrows(FPSOutTransactionManagerException.class, () -> {
      unit.process(transactionMessage, request);
    });
  }

  @Test
  public void testLMPain001RestClientException() throws IOException {

    when(handler.process(any(), any()))
        .thenThrow(new RestClientException(""));

    PaymentProcessResponse actual = unit.process(transactionMessage, request);
    assertEquals("FAILED", actual.getPaymentStatus());
    assertEquals(2100, actual.getReason().getCode());
  }

  @Test
  public void testLMPain002GateWayTimeoutFailureException() throws IOException {

    when(exceptionBuilder.buildFPSOutTransactionManagerException(
        INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getStatusCode(),
        INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
        .thenReturn(new FPSOutTransactionManagerException());

    when(handler.process(any(), any()))
        .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

    assertThrows(FPSOutTransactionManagerException.class, () -> {
      unit.process(transactionMessage, request);
    });
  }


  private <T> T buildStub(String filename, Class<T> clazz) throws IOException {
    return
        stringToJson(fileReader.getFileContent(filename), clazz);
  }

  private String getSubscriptionKey(TransactionMessage transactionMessage) {
    return (String) transactionMessage.getMessages().get(0).getAdditionalInfo()
        .get(SUBSCRIPTION_KEY.name());
  }

  private PaymentProcessResponse buildLMFailurePaymentResponse() {
    PaymentProcessResponse response = new PaymentProcessResponse();
    response.setPaymentStatus("FAILED");
    ReasonDto reasonDto = new ReasonDto(5301, "LM Posting Failed");
    response.setReason(reasonDto);
    return response;
  }

  private PaymentMessage buildPaymentMessage() {

    Map<String, Object> paymentMap = new HashMap<>();
    Map<String, Object> addMap = new HashMap<>();
    Map<String, Object> addMapMsg = new HashMap<>();

    addMap.put(OUTCOME.name(), SUCCESS.name());
    addMap.put(PAYMENT_TYPE.name(), "10");
    addMapMsg.put(SUBSCRIPTION_KEY.name(), "0c4d8720-0993-4860-95d6-e0190643e3a8");
    paymentMap.put(REQUEST_ID.name(), "3605865611");
    paymentMap.put(TRANSACTION_CORRELATION_ID.name(),
        "013b73cb2b-1b1b-431a-81fa-4b1fc18971721557821617505");

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setMessage(addMap);
    paymentMessage.setAdditionalInfo(addMapMsg);
    paymentMessage.setType(PAIN002.name());

    return paymentMessage;
  }

}

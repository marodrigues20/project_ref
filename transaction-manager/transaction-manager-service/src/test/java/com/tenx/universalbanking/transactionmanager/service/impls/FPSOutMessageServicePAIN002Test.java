package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.FAILED;
import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.SUCCESS;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.api.PaymentDecisionControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.api.DomesticPaymentControllerApi;
import com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.api.DomesticControllerApi;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.mapper.PPMTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class FPSOutMessageServicePAIN002Test {

  @Mock
  private PaymentDecisionControllerApi paymentDecisionControllerApi;

  @Mock
  private DomesticPaymentControllerApi domesticPaymentControllerApi;

  @Mock
  private MessageSender messageSender;

  @Mock
  private PPMTransactionMessageMapper ppmTransactionMessageMapper;

  @Mock
  private HttpServletRequest request;

  @Mock
  private TMExceptionBuilder exceptionBuilder;

  @Mock
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @Mock
  private DomesticControllerApi domesticControllerApi;

  @InjectMocks
  private FPSOutMessageService unit;

  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";

  private final String TRANSACTION_MESSAGE = "message/FPSOutTransactionMessagePAIN002Request.json";
  private final String TRANSACTION_MESSAGE_RETURN = "message/FPSOutTransactionMessagePAIN002ReturnRequest.json";
  private final String TRANSACTION_MESSAGE_FDP = "message/FPSOutFDPTransactionMessagePAIN002Request.json";

  private final FileReaderUtil fileReader = new FileReaderUtil();

  private TransactionMessage transactionMessage;
  private TransactionMessage transactionReturnMessage;
  private TransactionMessage transactionMessageFdp;

  private com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage pdfTransactionMessage;

  private com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessage ppTransactionMessage;

  @BeforeEach
  public void init() throws IOException {
    transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE);
    transactionReturnMessage = buildTransactionMsg(TRANSACTION_MESSAGE_RETURN);
    transactionMessageFdp = buildTransactionMsg(TRANSACTION_MESSAGE_FDP);
    messageServiceProcessorHelper.addTracingHeaders(any(), any());
  }

  @Test
  public void executeTransactionMessageSuccessTest() throws IOException {

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(messageSender).sendPaymentMessage(any(), any());
    PaymentProcessResponse response = unit.process(transactionMessage, request);

    assertEquals(SUCCESS.name(), response.getPaymentStatus());
    assertNotNull(response.getTransactionMessage());

    verify(domesticControllerApi, never()).acceptPaymentConfirmation(any());
    verify(paymentDecisionControllerApi, never()).makePaymentDecision(any());
    verify(domesticPaymentControllerApi, never()).processFpsOutPayment(any(), any());
  }

  @Test
  public void executeTransactionMessageOutcomeFailedTest() throws IOException {

    transactionMessage.getMessages().get(0).getMessage().put("OUTCOME", "FAILED");

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    doNothing().when(messageSender).sendPaymentMessage(any(), any());
    PaymentProcessResponse response = unit.process(transactionMessage, request);

    assertEquals(FAILED.name(), response.getPaymentStatus());
    assertNotNull(response.getTransactionMessage());

    verify(domesticControllerApi, never()).acceptPaymentConfirmation(any());
    verify(paymentDecisionControllerApi, never()).makePaymentDecision(any());
    verify(domesticPaymentControllerApi, never()).processFpsOutPayment(any(), any());
  }

  @Test
  public void executeTransactionMessageSuccessTestForFDP() throws IOException {

    com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.TransactionMessage mappedMessage = new com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.TransactionMessage();
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(ppmTransactionMessageMapper.toClientTransactionMessage(transactionMessageFdp))
        .thenReturn(mappedMessage);
    doNothing().when(domesticControllerApi).acceptPaymentConfirmation(any());
    doNothing().when(messageSender).sendPaymentMessage(any(), any());
    PaymentProcessResponse response = unit.process(transactionMessageFdp, request);

    assertEquals(SUCCESS.name(), response.getPaymentStatus());
    assertNotNull(response.getTransactionMessage());

    verify(domesticControllerApi).acceptPaymentConfirmation(any());
    verify(paymentDecisionControllerApi, never()).makePaymentDecision(any());
    verify(domesticPaymentControllerApi, never()).processFpsOutPayment(any(), any());
  }

  @Test
  public void executeTransactionMessagePPMThrowsHttpServerErrorException() throws IOException {

    com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.TransactionMessage mappedMessage = new com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.TransactionMessage();
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(ppmTransactionMessageMapper.toClientTransactionMessage(transactionMessageFdp))
        .thenReturn(mappedMessage);
    doThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT)).when(domesticControllerApi)
        .acceptPaymentConfirmation(any());
    when(exceptionBuilder.buildFPSOutTransactionManagerException(
        INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE.getStatusCode(),
        INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
        .thenReturn(new FPSOutTransactionManagerException());

    assertThrows(FPSOutTransactionManagerException.class, () -> {
      unit.process(transactionMessageFdp, request);
    });
  }

  @Test
  public void executeTransactionMessagePPMThrowsRestClientException() throws IOException {
    com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.TransactionMessage mappedMessage = new com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.model.TransactionMessage();
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(messageSender).sendPaymentMessage(any(), any());
    when(ppmTransactionMessageMapper.toClientTransactionMessage(transactionMessageFdp))
        .thenReturn(mappedMessage);
    doThrow(RestClientException.class).when(domesticControllerApi)
        .acceptPaymentConfirmation(any());

    PaymentProcessResponse actual = unit.process(transactionMessageFdp, request);

    assertEquals("FAILED", actual.getPaymentStatus());
    assertEquals(2100, actual.getReason().getCode());
  }

  @Test
  public void executeTransactionReturnMessageSuccessTest() throws IOException {

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(messageSender).sendPaymentMessage(any(), any());
    PaymentProcessResponse response = unit.process(transactionReturnMessage, request);

    assertEquals(SUCCESS.name(), response.getPaymentStatus());
    assertNotNull(response.getTransactionMessage());

    verify(domesticControllerApi, never()).acceptPaymentConfirmation(any());
    verify(paymentDecisionControllerApi, never()).makePaymentDecision(any());
    verify(domesticPaymentControllerApi, never()).processFpsOutPayment(any(), any());
  }

  @Test
  public void executeTransactionMessageFailureTest() throws IOException {

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(messageSender).sendPaymentMessage(any(), any());
    PaymentProcessResponse response = unit.process(transactionMessage, request);

    assertEquals(SUCCESS.name(), response.getPaymentStatus());
    assertNotNull(response.getTransactionMessage());

    verify(paymentDecisionControllerApi, never()).makePaymentDecision(any());
    verify(domesticPaymentControllerApi, never()).processFpsOutPayment(any(), any());

  }

  @Test
  public void executeTransactionMessageFailureTestWhenLMPostingFail() throws IOException {

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);

    when(responseConverter.buildPaymentProcessResponse(eq(lmPostingResponse), any()))
        .thenReturn(buildLMFailurePaymentResponse());

    PaymentProcessResponse response = unit.process(transactionMessage, request);

    assertEquals(FAILED.name(), response.getPaymentStatus());
    assertNotNull(response.getTransactionMessage());

    verify(domesticControllerApi, never()).acceptPaymentConfirmation(any());
    verify(paymentDecisionControllerApi, never()).makePaymentDecision(any());
    verify(domesticPaymentControllerApi, never()).processFpsOutPayment(any(), any());

  }


  private <T> T buildTransactionMsg(String filename) throws IOException {
    return
        stringToJson(fileReader.getFileContent(filename), (Class<T>) TransactionMessage.class);
  }

  private PaymentProcessResponse buildLMFailurePaymentResponse() {
    PaymentProcessResponse response = new PaymentProcessResponse();
    response.setPaymentStatus("FAILED");
    ReasonDto reasonDto = new ReasonDto(5301, "LM Posting Failed");
    response.setReason(reasonDto);
    return response;
  }
}

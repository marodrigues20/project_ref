package com.tenx.universalbanking.transactionmanager.service.helpers;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PDF_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVICE_UNAVAILABLE;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.api.PaymentDecisionControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.api.DomesticPaymentControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.FpsOutPaymentResponse;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmanager.exception.PdfException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageCorrelationIdGenerator;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageTransactionIdGenerator;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.service.mapper.PPTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Pain001AckEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import com.tenx.validationlib.response.Error;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@RunWith(MockitoJUnitRunner.class)
public class FPSOutPain001RequestHandlerTest {

  @Mock
  private PaymentDecisionControllerApi paymentDecisionControllerApi;

  @Mock
  private DomesticPaymentControllerApi domesticPaymentControllerApi;

  @Mock
  private PDFTransactionMessageMapper pdfMapper;

  @Mock
  private PPTransactionMessageMapper ppMapper;

  @Mock
  @SuppressWarnings("unused")
  private TransactionMessageCorrelationIdGenerator correlationIdGenerator;

  @Mock
  @SuppressWarnings("unused")
  private TransactionMessageTransactionIdGenerator transactionIdGenerator;

  @Mock
  @SuppressWarnings("unused")
  private MessageSender messageSender;

  @Mock
  @SuppressWarnings("unused")
  private HttpServletRequest request;

  @Mock
  private TMExceptionBuilder exceptionBuilder;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @Rule
  public ExpectedException thrown = ExpectedException.none();


  @InjectMocks
  private FPSOutPain001RequestHandler unit;

  private final String TRANSACTION_MESSAGE = "message/FPSOutTransactionMessageRequest.json";

  private final String PAIN001_MESSAGE_CRITERIA_NOT_MET = "message/FPSOutPAIN001PaymentCriteriaNotMetMessageRequest.json";
  private final String PAIN001_MESSAGE = "message/FPSOutPAIN001PaymentMessageRequest.json";

  private final String TRANSACTION_FDP_MESSAGE = "message/FPSOutTransactionMessageFDPRequest.json";

  private final String PAIN001_FDP_MESSAGE = "message/FPSOutPAIN001PaymentMessageFDPRequest.json";

  private final String FPSOutResponseSuccess = "message/FPSOutSuccessResponseAck.json";

  private final String FPSOutResponseFailure = "message/FPSOutFailureResponseAck.json";

  private final FileReaderUtil fileReader = new FileReaderUtil();

  private TransactionMessage transactionMessage;

  private PaymentMessage pain001Msg;

  private TransactionMessage transactionFDPMessage;

  private PaymentMessage pain001FDPMsg;

  private com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage pdfTransactionMessage;

  private com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage pdfTransactionFDPMessage;

  private com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessage ppTransactionMessage;

  private com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessage ppTransactionFDPMessage;

  @Before
  public void init() throws IOException {
    transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE, TransactionMessage.class);

    pain001Msg = buildTransactionMsg(PAIN001_MESSAGE, PaymentMessage.class);

    transactionFDPMessage = buildTransactionMsg(TRANSACTION_FDP_MESSAGE, TransactionMessage.class);

    pain001FDPMsg = buildTransactionMsg(PAIN001_FDP_MESSAGE, PaymentMessage.class);

    pdfTransactionMessage =
        buildTransactionMsg(TRANSACTION_MESSAGE,
            com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage.class);

    ppTransactionMessage =
        buildTransactionMsg(TRANSACTION_MESSAGE,
            com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessage.class);

    ppTransactionFDPMessage =
        buildTransactionMsg(TRANSACTION_FDP_MESSAGE,
            com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessage.class);

    when(pdfMapper.toClientTransactionMessage(transactionMessage))
        .thenReturn(pdfTransactionMessage);

    when(ppMapper.toPPTransactionMessage(transactionMessage))
        .thenReturn(ppTransactionMessage);

    when(ppMapper.toPPTransactionMessage(transactionFDPMessage))
        .thenReturn(ppTransactionFDPMessage);

  }

  @Test(expected = InvalidTransactionMessageException.class)
  public void executeTransactionMessageThrowsException() throws IOException {

    PaymentMessage pain001MsgWrong = buildTransactionMsg(PAIN001_MESSAGE_CRITERIA_NOT_MET,
        PaymentMessage.class);

    unit.process(transactionMessage, pain001MsgWrong);

  }

  @Test
  public void executeTransactionMessageSuccessTest() throws IOException {

    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenReturn(createPaymentDecisionTransactionSuccessResponse());

    FpsOutPaymentResponse paymentProxyResponse = getPaymentProxyResponse(true);

    when(domesticPaymentControllerApi.processFpsOutPayment(ppTransactionMessage, "10000"))
        .thenReturn(paymentProxyResponse);
    when(ppMapper.toTMTransactionMessage(any()))
        .thenReturn(getMessageAsTMTransaction(paymentProxyResponse.getTransactionMessage()));

    TransactionMessage response = unit.process(transactionMessage, pain001Msg);

    Assert.assertEquals(PaymentDecisionResponse.SUCCESS.name(),
        response.getMessages().get(0).getMessage().get(Pain001AckEnum.OUTCOME.name()));

    Assert.assertEquals(PaymentMessageTypeEnum.PAIN001_ACK.name(),
        response.getMessages().get(0).getType());

  }

  @Test
  public void executeTransactionMessageFDP() throws IOException {

    when(paymentDecisionControllerApi.makePaymentDecision(any()))
        .thenReturn(createPaymentDecisionTransactionSuccessResponse());

    FpsOutPaymentResponse paymentProxyResponse = getPaymentProxyResponse(true);

    when(domesticPaymentControllerApi.processFpsOutPayment(ppTransactionFDPMessage, "10000"))
        .thenReturn(paymentProxyResponse);
    when(ppMapper.toTMTransactionMessage(any()))
        .thenReturn(getMessageAsTMTransaction(paymentProxyResponse.getTransactionMessage()));

    TransactionMessage response = unit.process(transactionFDPMessage, pain001FDPMsg);

    Assert.assertEquals(PaymentDecisionResponse.SUCCESS.name(),
        response.getMessages().get(0).getMessage().get(Pain001AckEnum.OUTCOME.name()));

    Assert.assertEquals(PaymentMessageTypeEnum.PAIN001_ACK.name(),
        response.getMessages().get(0).getType());

  }

  @Test
  public void executeTransactionMessageFailTest() throws IOException {

    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenReturn(createPaymentDecisionTransactionSuccessResponse());

    FpsOutPaymentResponse paymentProxyResponse = getPaymentProxyResponse(false);

    when(domesticPaymentControllerApi.processFpsOutPayment(ppTransactionMessage, "10000"))
        .thenReturn(paymentProxyResponse);
    when(ppMapper.toTMTransactionMessage(any()))
        .thenReturn(getMessageAsTMTransaction(paymentProxyResponse.getTransactionMessage()));

    TransactionMessage response = unit.process(transactionMessage, pain001Msg);

    Assert.assertEquals(PaymentDecisionResponse.FAILED.name(),
        response.getMessages().get(0).getMessage().get(Pain001AckEnum.OUTCOME.name()));

    Assert.assertEquals(PaymentMessageTypeEnum.PAIN001_ACK.name(),
        response.getMessages().get(0).getType());

  }

  @Test
  public void testFpsoutExceptionWhenForm3ReturnServerErrorResponse() throws IOException {

    Error error = new Error();
    error.setCode(123);
    error.setMessage("test");

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper
        .writeValueAsString(Collections.singletonMap("errors", Collections.singletonList(error)));

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE,
            HttpStatus.SERVICE_UNAVAILABLE.name(), null,
            body.getBytes(), null);

    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenReturn(createPaymentDecisionTransactionSuccessResponse());
    when(exceptionBuilder.buildFromDownstreamError(any()))
        .thenReturn(new FPSOutTransactionManagerException());
    when(domesticPaymentControllerApi.processFpsOutPayment(any(), any()))
        .thenThrow(httpServerErrorException);

    thrown.expect(FPSOutTransactionManagerException.class);

    unit.process(transactionMessage, pain001Msg);
  }

  @Test
  public void testFpsoutExceptionWhenForm3ReturnServerErrorResponseAndEncounterParsingError()
      throws IOException {

    Error error = new Error();
    error.setCode(123);
    error.setMessage("test");

    HttpServerErrorException httpServerErrorException =
        new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE,
            HttpStatus.SERVICE_UNAVAILABLE.name(), null,
            error.toString().getBytes(), null);

    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenReturn(createPaymentDecisionTransactionSuccessResponse());
    when(domesticPaymentControllerApi.processFpsOutPayment(any(), any()))
        .thenThrow(httpServerErrorException);

    thrown.expect(HttpServerErrorException.class);

    unit.process(transactionMessage, pain001Msg);
  }

  @Test(expected = RestClientException.class)
  public void testPDFThrowsException() {
    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenThrow(new RestClientException(""));

    unit.process(transactionMessage, pain001Msg);
  }

  @Test(expected = RestClientException.class)
  public void testPaymentProxyException() {
    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenReturn(createPaymentDecisionTransactionSuccessResponse());

    when(domesticPaymentControllerApi.processFpsOutPayment(ppTransactionMessage, "10000"))
        .thenThrow(new RestClientException("Payment proxy failed"));

    unit.process(transactionMessage, pain001Msg);

    Assert.fail("Expected exception message");
  }

  @Test(expected = FPSOutTransactionManagerException.class)
  public void testPaymentProxyGateWayTimeoutException() {

    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenReturn(createPaymentDecisionTransactionSuccessResponse());

    when(domesticPaymentControllerApi.processFpsOutPayment(ppTransactionMessage, "10000"))
        .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));
    when(exceptionBuilder
        .buildFPSOutTransactionManagerException(INTERNAL_SERVICE_UNAVAILABLE.getStatusCode(),
            INTERNAL_SERVICE_UNAVAILABLE.getMessage(), HttpStatus.SERVICE_UNAVAILABLE))
        .thenReturn(new FPSOutTransactionManagerException());
    unit.process(transactionMessage, pain001Msg);
  }

  @Test(expected = HttpServerErrorException.class)
  public void testPaymentProxyOtherThanGateWayTimeoutException() {

    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenReturn(createPaymentDecisionTransactionSuccessResponse());

    when(domesticPaymentControllerApi.processFpsOutPayment(ppTransactionMessage, "10000"))
        .thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));

    unit.process(transactionMessage, pain001Msg);
  }

  @Test(expected = FPSOutTransactionManagerException.class)
  public void testPaymentDecisionGateWayTimeoutException() throws IOException {

    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));
    when(exceptionBuilder
        .buildFPSOutTransactionManagerException(INTERNAL_SERVER_ERROR_PDF_FAILURE.getStatusCode(),
            INTERNAL_SERVER_ERROR_PDF_FAILURE.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
        .thenReturn(new FPSOutTransactionManagerException());
    unit.process(transactionMessage, pain001Msg);

  }

  @Test(expected = PdfException.class)
  public void executeTransactionMessageDecisionFailTest() {
    PaymentDecisionTransactionResponse pdfResponse = createPaymentDecisionTransactionFailedResponse();

    when(paymentDecisionControllerApi.makePaymentDecision(pdfTransactionMessage))
        .thenReturn(pdfResponse);
    when(exceptionBuilder.buildFromPdfResponse(eq(pdfResponse))).thenReturn(new PdfException());

    TransactionMessage response = unit.process(transactionMessage, pain001Msg);

    Assert.assertEquals(PaymentDecisionResponse.FAILED.name(),
        response.getMessages().get(0).getMessage().get(Pain001AckEnum.OUTCOME.name()));
  }

  private FpsOutPaymentResponse
  getPaymentProxyResponse(Boolean isSuccess) throws IOException {
    FpsOutPaymentResponse response = null;
    if (isSuccess) {
      response = buildTransactionMsg(FPSOutResponseSuccess,
          FpsOutPaymentResponse.class);
    } else {
      response = buildTransactionMsg(FPSOutResponseFailure,
          FpsOutPaymentResponse.class);
    }

    return response;
  }

  private TransactionMessage getMessageAsTMTransaction(
      com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.TransactionMessage msg) {
    return stringToJson(toJson(msg), TransactionMessage.class);
  }

  private <T> T buildTransactionMsg(String filename, Class<T> clazz) throws IOException {
    return
        stringToJson(fileReader.getFileContent(filename), clazz);
  }

  private PaymentDecisionTransactionResponse createPaymentDecisionTransactionSuccessResponse() {
    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse =
        new PaymentDecisionTransactionResponse();
    paymentDecisionTransactionResponse.setDecisionReason(createDecisionReason());
    paymentDecisionTransactionResponse.setDecisionResponse(PaymentDecisionResponse.SUCCESS.name());
    return paymentDecisionTransactionResponse;
  }

  private PaymentDecisionTransactionResponse createPaymentDecisionTransactionFailedResponse() {

    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse =
        new PaymentDecisionTransactionResponse();
    paymentDecisionTransactionResponse.setDecisionReason(createDecisionReason());
    paymentDecisionTransactionResponse.setDecisionResponse(PaymentDecisionResponse.FAILED.name());
    return paymentDecisionTransactionResponse;
  }

  private PaymentDecisionReasonDTO createDecisionReason() {

    PaymentDecisionReasonDTO paymentDecisionReasonDTO = new PaymentDecisionReasonDTO();
    paymentDecisionReasonDTO.setCode(200);
    paymentDecisionReasonDTO.setMessage(PaymentDecisionResponse.SUCCESS.name());
    return paymentDecisionReasonDTO;
  }

  private PaymentProcessResponse buildLMFailurePaymentResponse() {
    PaymentProcessResponse response = new PaymentProcessResponse();
    response.setPaymentStatus("FAILED");
    ReasonDto reasonDto = new ReasonDto(5301, "LM Posting Failed");
    response.setReason(reasonDto);
    return response;
  }
}

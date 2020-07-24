package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.REQUEST_PAYMENT_BY_CARD;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.fundaccountmanager.client.api.PaymentControllerApi;
import com.tenx.universalbanking.fundaccountmanager.client.model.SubmitPaymentResponse;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageCorrelationIdGenerator;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageTransactionIdGenerator;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.mapper.FAMTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestPaymentServiceTest {

  @Mock
  private FAMTransactionMessageMapper famTransactionMessageMapper;

  @Mock
  private PaymentControllerApi paymentControllerApi;

  @Mock
  private TransactionMessageCorrelationIdGenerator transactionMessageCorrelationIdGenerator;

  @Mock
  private TransactionMessageTransactionIdGenerator transactionMessageTransactionIdGenerator;

  @Mock
  private MessageSender messageSender;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @Mock
  private LedgerManagerClient ledgerManagerClient;

  @InjectMocks
  private RequestPaymentService requestPaymentService;

  @Mock
  private HttpServletRequest request;

  private static final String REQUEST_PAYMENT = "message/request-payment/requestPayment.json";

  private static final String REQUEST_PAYMENT_SUCCESS_RESPONSE = "message/request-payment/fam_success_response.json";

  private static final String REQUEST_PAYMENT_FAILURE_RESPONSE = "message/request-payment/fam_failure_response.json";

  private static final String REQUEST_PAYMENT_3DSECURE_RESPONSE = "message/request-payment/fam_3dsecure_response.json";


  private final FileReaderUtil fileReader = new FileReaderUtil();


  @Test
  public void shouldGetRequestPaymentHeaderWheanCallGetType() {
    assertThat(requestPaymentService.getType(), is(REQUEST_PAYMENT_BY_CARD));
  }

  @Test
  public void shouldProcessFAMRequestOnSuccess() {

    SubmitPaymentResponse submitPaymentResponse = createSubmitPaymentResponseDtoSuccess();

    when(paymentControllerApi.submitPayment(anyString(), any())).thenReturn(submitPaymentResponse);
    TransactionMessage paymentRequest = createPaymentRequestDto();

    TransactionMessage caaa002 = createCaaa002SuccessMessage();

    when(famTransactionMessageMapper.toTransactionMessage(any())).thenReturn(caaa002);

    LedgerPostingResponse ledgerPostingResponse = createLedgerPostingResponseSuccessDto();

    when(ledgerManagerClient.postTransactionToLedger(any())).thenReturn(ledgerPostingResponse);
    doNothing().when(messageSender)
        .sendPaymentMessage("5bc7a718-c141-472b-99f9-b1955725fab6", caaa002);
    doNothing().when(transactionMessageCorrelationIdGenerator).addCorrelationId(paymentRequest);
    doNothing().when(transactionMessageTransactionIdGenerator)
        .addTransactionId(paymentRequest.getMessages().get(0));
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    PaymentProcessResponse paymentProcessResponse = requestPaymentService
        .process(paymentRequest, request);

    TransactionMessage transactionMessage = paymentProcessResponse.getTransactionMessage();

    assertEquals("SUCCESS", paymentProcessResponse.getPaymentStatus());
    assertEquals("REQUEST_PAYMENT_BY_CARD", transactionMessage.getHeader().getType());

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.CAAA001.name()
            .equals(item.getType())));

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.CAAA002.name()
            .equals(item.getType())));

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.POSTING_AMOUNT.name()
            .equals(item.getType())));

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.PROCESSOR_ACTUAL_FEE.name()
            .equals(item.getType())));

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.PROCESSOR_AGREED_FEE.name()
            .equals(item.getType())));
  }

  @Test
  public void shouldProcessFAMRequestOnFailure() {

    SubmitPaymentResponse submitPaymentResponse = createSubmitPaymentResponseDtoFailure();

    when(paymentControllerApi.submitPayment(anyString(), any())).thenReturn(submitPaymentResponse);
    TransactionMessage paymentRequest = createPaymentRequestDto();

    TransactionMessage caaa002 = createCaaa002FailureMessage();

    when(famTransactionMessageMapper.toTransactionMessage(any())).thenReturn(caaa002);

    LedgerPostingResponse ledgerPostingResponse = createLedgerPostingResponseFailureDto();

    when(ledgerManagerClient.postTransactionToLedger(any())).thenReturn(ledgerPostingResponse);
    doNothing().when(transactionMessageCorrelationIdGenerator).addCorrelationId(paymentRequest);
    doNothing().when(transactionMessageTransactionIdGenerator)
        .addTransactionId(paymentRequest.getMessages().get(0));
    PaymentProcessResponse paymentProcessResponse = requestPaymentService
        .process(paymentRequest, request);

    TransactionMessage transactionMessage = paymentProcessResponse.getTransactionMessage();

    assertEquals("FAILURE", paymentProcessResponse.getPaymentStatus());
    assertEquals("REQUEST_PAYMENT_BY_CARD", transactionMessage.getHeader().getType());

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.CAAA001.name()
            .equals(item.getType())));

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.CAAA002.name()
            .equals(item.getType())));
  }

  @Test
  public void shouldProcessFAMThrowsError() {

    when(paymentControllerApi.submitPayment(anyString(), any())).thenThrow(RuntimeException.class);
    TransactionMessage paymentRequest = createPaymentRequestDto();
    doNothing().when(transactionMessageCorrelationIdGenerator).addCorrelationId(paymentRequest);
    doNothing().when(transactionMessageTransactionIdGenerator)
        .addTransactionId(paymentRequest.getMessages().get(0));
    PaymentProcessResponse actual = requestPaymentService
        .process(paymentRequest, request);

    assertEquals("FAILURE", actual.getPaymentStatus());
  }

  @Test
  public void shouldProcessFAMRequestOn3dsecure() {

    SubmitPaymentResponse submitPaymentResponse = createSubmitPaymentResponseDto3Dsecure();

    when(paymentControllerApi.submitPayment(anyString(), any())).thenReturn(submitPaymentResponse);
    TransactionMessage paymentRequest = createPaymentRequestDto();

    TransactionMessage caaa002 = createCaaa0023dsecureMessage();

    when(famTransactionMessageMapper.toTransactionMessage(any())).thenReturn(caaa002);

    LedgerPostingResponse ledgerPostingResponse = createLedgerPostingResponseFailureDto();

    when(ledgerManagerClient.postTransactionToLedger(any())).thenReturn(ledgerPostingResponse);
    doNothing().when(transactionMessageCorrelationIdGenerator).addCorrelationId(paymentRequest);
    doNothing().when(transactionMessageTransactionIdGenerator)
        .addTransactionId(paymentRequest.getMessages().get(0));
    PaymentProcessResponse paymentProcessResponse = requestPaymentService
        .process(paymentRequest, request);

    TransactionMessage transactionMessage = paymentProcessResponse.getTransactionMessage();

    assertEquals("FAILURE", paymentProcessResponse.getPaymentStatus());
    assertEquals("REQUEST_PAYMENT_BY_CARD", transactionMessage.getHeader().getType());

    assertEquals("http://threedsecure.com", transactionMessage.getMessages().stream()
        .filter(item -> item.getType().equals(PaymentMessageTypeEnum.CAAA002.name())).findFirst()
        .get()
        .getMessage().get(Caaa002Enum.THREE_DS_REDIRECT_URL.name()));

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.CAAA001.name()
            .equals(item.getType())));

    assertTrue(transactionMessage.getMessages().stream()
        .anyMatch(item -> PaymentMessageTypeEnum.CAAA002.name()
            .equals(item.getType())));
  }

  private TransactionMessage createPaymentRequestDto() {

    TransactionMessage transactionMessage = null;

    try {
      transactionMessage = stringToJson(fileReader.getFileContent(REQUEST_PAYMENT),
          TransactionMessage.class);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return transactionMessage;
  }

  private SubmitPaymentResponse createSubmitPaymentResponseDtoSuccess() {
    com.tenx.universalbanking.fundaccountmanager.client.model.TransactionMessage transactionMessage = null;
    try {
      transactionMessage = stringToJson(
          fileReader.getFileContent(REQUEST_PAYMENT_SUCCESS_RESPONSE),
          com.tenx.universalbanking.fundaccountmanager.client.model.TransactionMessage.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    SubmitPaymentResponse submitPaymentResponse = new SubmitPaymentResponse();
    submitPaymentResponse.setTransactionMessage(transactionMessage);
    return submitPaymentResponse;
  }

  private SubmitPaymentResponse createSubmitPaymentResponseDtoFailure() {
    com.tenx.universalbanking.fundaccountmanager.client.model.TransactionMessage transactionMessage = null;
    try {
      transactionMessage = stringToJson(
          fileReader.getFileContent(REQUEST_PAYMENT_FAILURE_RESPONSE),
          com.tenx.universalbanking.fundaccountmanager.client.model.TransactionMessage.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    SubmitPaymentResponse submitPaymentResponse = new SubmitPaymentResponse();
    submitPaymentResponse.setTransactionMessage(transactionMessage);
    return submitPaymentResponse;
  }

  private SubmitPaymentResponse createSubmitPaymentResponseDto3Dsecure() {
    com.tenx.universalbanking.fundaccountmanager.client.model.TransactionMessage transactionMessage = null;
    try {
      transactionMessage = stringToJson(
          fileReader.getFileContent(REQUEST_PAYMENT_3DSECURE_RESPONSE),
          com.tenx.universalbanking.fundaccountmanager.client.model.TransactionMessage.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    SubmitPaymentResponse submitPaymentResponse = new SubmitPaymentResponse();
    submitPaymentResponse.setTransactionMessage(transactionMessage);
    return submitPaymentResponse;
  }

  private TransactionMessage createCaaa002SuccessMessage() {

    TransactionMessage transactionMessage = new TransactionMessage();
    try {
      transactionMessage = stringToJson(fileReader.getFileContent(REQUEST_PAYMENT_SUCCESS_RESPONSE),
          TransactionMessage.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return transactionMessage;
  }

  private TransactionMessage createCaaa002FailureMessage() {

    TransactionMessage transactionMessage = new TransactionMessage();
    try {
      transactionMessage = stringToJson(fileReader.getFileContent(REQUEST_PAYMENT_FAILURE_RESPONSE),
          TransactionMessage.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return transactionMessage;
  }

  private TransactionMessage createCaaa0023dsecureMessage() {

    TransactionMessage transactionMessage = new TransactionMessage();
    try {
      transactionMessage = stringToJson(
          fileReader.getFileContent(REQUEST_PAYMENT_3DSECURE_RESPONSE), TransactionMessage.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return transactionMessage;
  }


  private LedgerPostingResponse createLedgerPostingResponseSuccessDto() {
    LedgerPostingResponse ledgerPostingResponse = new LedgerPostingResponse();
    ledgerPostingResponse.setPostingSuccess(true);
    return ledgerPostingResponse;
  }

  private LedgerPostingResponse createLedgerPostingResponseFailureDto() {
    LedgerPostingResponse ledgerPostingResponse = new LedgerPostingResponse();
    ledgerPostingResponse.setPostingSuccess(false);
    ReasonDto reasonDto = new ReasonDto();
    reasonDto.setCode(1000);
    reasonDto.setMessage("Message send to ledger is not successful");
    ledgerPostingResponse.setReason(reasonDto);
    return ledgerPostingResponse;
  }

}
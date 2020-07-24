package com.tenx.universalbanking.transactionmanager.service.helpers;

import static com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes.WORLDPAY_TRANSACTION_FAILURE_KEY_CODE;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_MERCHANT_CARD_TRANSACTIONS_OTHER_ACCOUNT_FUNDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.api.PaymentDecisionControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.api.WorldPayAdapterControllerApi;
import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.AdapterErrorResponse;
import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.AdapterResponse;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageCorrelationIdGenerator;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageTransactionIdGenerator;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.service.mapper.WorldpayTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.service.messagebuilder.CAAA002MessageBuilder;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class CardFundProcessHelperTest {

  private static final String CARD_FUND_TRANSACTION_MESSAGE_REQUEST = "message/cardFundTransactionMessageRequest.json";
  private static final String WORLDPAY_RESPONSE_SUCCESS = "message/cardfund/worldpay-success-response.json";
  private static final String WORLDPAY_RESPONSE_FAILED = "message/cardfund/worldpay-failure-response.json";
  private static final String WORLDPAY_RESPONSE_FAILED_FRAUD = "message/cardfund/worldpay-failure-fraud.json";
  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_Failure = "message/LM-Posting-Failure.json";

  private FileReaderUtil fileReader;

  @InjectMocks
  private CardFundProcessHelper processHelper;

  @Mock
  private PDFTransactionMessageMapper pdfMapper;

  @Spy
  private WorldpayTransactionMessageMapper worldpayMapper;

  @Mock
  private PaymentDecisionControllerApi paymentDecisionControllerApi;

  @Mock
  private MessageSender messageSender;

  @Mock
  private WorldPayAdapterControllerApi worldPayAdapterControllerApi;

  @Mock
  private CAAA002MessageBuilder caa002MessageBuilder;

  @Mock
  private TransactionMessageCorrelationIdGenerator correlationIdGenerator;

  @Mock
  private TransactionMessageTransactionIdGenerator transactionIdGenerator;

  @Mock
  private HttpServletRequest request;

  @Mock
  private TracingHeadersFilter tracingHeadersFilter;

  @Mock
  private LedgerManagerClient lmClient;

  @BeforeEach
  public void setup() {
    fileReader = new FileReaderUtil();
  }

  @Test
  public void handleDecisionSuccessProcess() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    PaymentDecisionTransactionResponse paymentDecisionResponse = buildDecisionSuccessResponse();

    when(pdfMapper.toClientTransactionMessage(transactionMessage)).thenReturn(
        new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage());

    when(paymentDecisionControllerApi.makePaymentDecision(any()))
        .thenReturn(paymentDecisionResponse);

    Optional<PaymentProcessResponse> optionalPaymentProcessResponse =
        processHelper.handleDecisionProcess(transactionMessage, request);

    assertFalse(optionalPaymentProcessResponse.isPresent());
  }

  @Test
  public void fundAccountTest() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.TransactionMessage transactionMessageMapped = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.TransactionMessage.class);

    AdapterResponse worldpayAdapterResponse = new AdapterResponse();
    worldpayAdapterResponse.setTransactionMessage(transactionMessageMapped);

    when(worldPayAdapterControllerApi.worldPayPaymentSubmitOrderUsingPOST(any()))
        .thenReturn(worldpayAdapterResponse);

    TransactionMessage actual = processHelper.fundAccount(transactionMessage);

    assertEquals(transactionMessage, actual);
  }

  @Test
  public void handleDecisionFailureLMSuccessProcess() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    PaymentDecisionTransactionResponse paymentDecisionResponse = buildDecisionFailureResponse();

    when(pdfMapper.toClientTransactionMessage(transactionMessage)).thenReturn(
        new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage());

    when(paymentDecisionControllerApi.makePaymentDecision(any()))
        .thenReturn(paymentDecisionResponse);
    when(caa002MessageBuilder
        .caaa002MessageBuilder(any(TransactionMessage.class), any(PaymentProcessResponse.class)))
        .thenReturn(transactionMessage);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(messageSender).sendPaymentMessage(anyString(), any(TransactionMessage.class));

    Optional<PaymentProcessResponse> optionalPaymentProcessResponse =
        processHelper.handleDecisionProcess(transactionMessage, request);

    assertTrue(optionalPaymentProcessResponse.isPresent());
  }

  @Test
  public void handleDecisionFailureLMFailureProcess() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_Failure),
        LedgerPostingResponse.class);
    PaymentDecisionTransactionResponse paymentDecisionResponse = buildDecisionFailureResponse();

    when(pdfMapper.toClientTransactionMessage(transactionMessage)).thenReturn(
        new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage());

    when(paymentDecisionControllerApi.makePaymentDecision(any()))
        .thenReturn(paymentDecisionResponse);
    when(caa002MessageBuilder
        .caaa002MessageBuilder(any(TransactionMessage.class), any(PaymentProcessResponse.class)))
        .thenReturn(transactionMessage);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);

    Optional<PaymentProcessResponse> optionalPaymentProcessResponse =
        processHelper.handleDecisionProcess(transactionMessage, request);

    assertTrue(optionalPaymentProcessResponse.isPresent());
  }


  @Test
  public void handleDecisionFailureProcess_whenPdfFails() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);

    when(pdfMapper.toClientTransactionMessage(transactionMessage)).thenReturn(
        new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage());

    when(paymentDecisionControllerApi.makePaymentDecision(any())).thenThrow(new RuntimeException());

    when(caa002MessageBuilder.caaa002MessageBuilder(any(), any())).thenReturn(transactionMessage);

    when(tracingHeadersFilter.filter(request))
        .thenReturn(Collections.singletonMap("header", "value"));
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(messageSender).sendPaymentMessage(anyString(), any(TransactionMessage.class));
    Optional<PaymentProcessResponse> optionalPaymentProcessResponse =
        processHelper.handleDecisionProcess(transactionMessage, request);

    assertEquals("FAILED", optionalPaymentProcessResponse.get().getPaymentStatus());
    assertEquals(2100, (int) optionalPaymentProcessResponse.get().getReason().getCode());
    assertEquals("Transaction failed",
        optionalPaymentProcessResponse.get().getReason().getMessage());
    verify(messageSender).sendPaymentMessage(anyString(), any(TransactionMessage.class));
  }

  @Test
  public void setCardFundTransactionCode() throws IOException {
    AdapterResponse adapterResponse = stringToJson(
        fileReader.getFileContent(WORLDPAY_RESPONSE_SUCCESS),
        AdapterResponse.class);

    TransactionMessage transactionMessage = worldpayMapper
        .clientTmToTmUtl(adapterResponse.getTransactionMessage());

    processHelper.setCardFundTransactionCode(transactionMessage);

    assertEquals(PAYMENTS_MERCHANT_CARD_TRANSACTIONS_OTHER_ACCOUNT_FUNDING.getValue(),
        transactionMessage.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name()));
  }

  @Test
  public void setCardFundTransactionCodeFailure() throws IOException {
    AdapterResponse adapterResponse = stringToJson(
        fileReader.getFileContent(WORLDPAY_RESPONSE_FAILED),
        AdapterResponse.class);

    TransactionMessage transactionMessage = worldpayMapper
        .clientTmToTmUtl(adapterResponse.getTransactionMessage());

    processHelper.setCardFundTransactionCode(transactionMessage);

    assertNull(
        transactionMessage.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name()));
  }

  @Test
  public void setCardFundTransactionCodeFailureFraud() throws IOException {
    AdapterErrorResponse adapterResponse = stringToJson(
        fileReader.getFileContent(WORLDPAY_RESPONSE_FAILED_FRAUD),
        AdapterErrorResponse.class);

    TransactionMessage transactionMessage = worldpayMapper
        .clientTmToTmUtl(adapterResponse.getTransactionMessage());

    processHelper.setCardFundTransactionCode(transactionMessage);

    assertNull(
        transactionMessage.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name()));
  }

  @Test
  public void buildResponseSuccess() throws IOException {
    AdapterResponse adapterResponse = stringToJson(
        fileReader.getFileContent(WORLDPAY_RESPONSE_SUCCESS),
        AdapterResponse.class);

    TransactionMessage transactionMessage = worldpayMapper
        .clientTmToTmUtl(adapterResponse.getTransactionMessage());

    PaymentProcessResponse response = processHelper.buildResponse(transactionMessage);
    assertEquals("SUCCESS", response.getPaymentStatus());
    assertEquals(transactionMessage, response.getTransactionMessage());
  }

  @Test
  public void buildResponseFailure() throws IOException {
    AdapterResponse adapterResponse = stringToJson(
        fileReader.getFileContent(WORLDPAY_RESPONSE_FAILED),
        AdapterResponse.class);

    TransactionMessage transactionMessage = worldpayMapper
        .clientTmToTmUtl(adapterResponse.getTransactionMessage());

    PaymentProcessResponse response = processHelper.buildResponse(transactionMessage);
    assertEquals("FAILED", response.getPaymentStatus());
    assertEquals(WORLDPAY_TRANSACTION_FAILURE_KEY_CODE, (int) response.getReason().getCode());
    assertEquals("Invalid payment details", response.getReason().getMessage());
  }

  @Test
  public void handleGenericFailureLMPostingSuccess() throws IOException {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(caa002MessageBuilder.caaa002MessageBuilder(any(), any())).thenReturn(transactionMessage);
    doNothing().when(messageSender).sendPaymentMessage(anyString(), any(TransactionMessage.class));
    when(tracingHeadersFilter.filter(request))
        .thenReturn(Collections.singletonMap("header", "value"));
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    PaymentProcessResponse actual = processHelper.handleGenericFailure(transactionMessage, request);
    verify(messageSender).sendPaymentMessage(anyString(), any(TransactionMessage.class));
    assertNotNull(actual);
  }

  @Test
  public void handleGenericFailureLMPostingFailure() throws IOException {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_Failure),
        LedgerPostingResponse.class);
    when(caa002MessageBuilder.caaa002MessageBuilder(any(), any())).thenReturn(transactionMessage);

    when(tracingHeadersFilter.filter(request))
        .thenReturn(Collections.singletonMap("header", "value"));
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    PaymentProcessResponse actual = processHelper.handleGenericFailure(transactionMessage, request);
    verify(messageSender, times(0)).sendPaymentMessage(anyString(), any(TransactionMessage.class));
    assertNotNull(actual);
  }

  private PaymentDecisionTransactionResponse buildDecisionSuccessResponse() {
    PaymentDecisionTransactionResponse decisionTransactionResponse = new PaymentDecisionTransactionResponse();
    decisionTransactionResponse.setDecisionResponse(PaymentDecisionResponse.SUCCESS.name());
    return decisionTransactionResponse;
  }

  private PaymentDecisionTransactionResponse buildDecisionFailureResponse() {
    PaymentDecisionTransactionResponse decisionTransactionResponse = new PaymentDecisionTransactionResponse();
    decisionTransactionResponse.setDecisionResponse(PaymentDecisionResponse.FAILED.name());
    PaymentDecisionReasonDTO reasonDTO = new PaymentDecisionReasonDTO();
    decisionTransactionResponse.setDecisionReason(reasonDTO);
    return decisionTransactionResponse;
  }
}
package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.TOP_UP_BY_CARD;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAAA002;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.worldpayadapter.model.AdapterResponse;
import com.tenx.universalbanking.transactionmanager.enums.TransactionReason;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.CardFundProcessHelper;
import com.tenx.universalbanking.transactionmanager.service.mapper.WorldpayTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.validationlib.response.Errors;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class CardFundMessageServiceTest {

  private static final String WORLDPAY_RESPONSE_SUCCESS = "message/cardfund/worldpay-success-response.json";
  private static final String WORLDPAY_RESPONSE_FAILED_FRAUD = "message/cardfund/worldpay-failure-fraud.json";
  private static final String CARD_FUND_TRANSACTION_MESSAGE_REQUEST = "message/cardFundTransactionMessage.json";
  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";

  @Mock
  private CardFundProcessHelper processorHelper;

  @Spy
  private WorldpayTransactionMessageMapper worldpayMapper;

  @InjectMocks
  private CardFundMessageService cardFundMessageService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @Mock
  private LedgerManagerClient lmClient;

  private FileReaderUtil fileReader;

  @BeforeEach
  public void init() {
    fileReader = new FileReaderUtil();
  }

  @Test
  public void shouldGetCardFundMessageWhenCallGetType() {
    assertEquals(TOP_UP_BY_CARD, cardFundMessageService.getType());
  }

  @Test
  public void testProcessSuccess() throws IOException {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    when(processorHelper.handleDecisionProcess(transactionMessage, request))
        .thenReturn(Optional.ofNullable(null));

    AdapterResponse adapterResponse = stringToJson(
        fileReader.getFileContent(WORLDPAY_RESPONSE_SUCCESS),
        AdapterResponse.class);

    TransactionMessage worldpayTransactionMessage =
        worldpayMapper.clientTmToTmUtl(adapterResponse.getTransactionMessage());

    when(processorHelper.fundAccount(transactionMessage))
        .thenReturn(worldpayTransactionMessage);

    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();

    when(processorHelper.buildResponse(worldpayTransactionMessage))
        .thenReturn(paymentProcessResponse);

    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    PaymentProcessResponse actual = cardFundMessageService
        .process(transactionMessage, request);

    assertEquals(paymentProcessResponse, actual);
  }

  @Test
  public void testLMPostingFailure() throws IOException {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    when(processorHelper.handleDecisionProcess(transactionMessage, request))
        .thenReturn(Optional.ofNullable(null));

    AdapterResponse adapterResponse = stringToJson(
        fileReader.getFileContent(WORLDPAY_RESPONSE_SUCCESS),
        AdapterResponse.class);

    TransactionMessage worldpayTransactionMessage =
        worldpayMapper.clientTmToTmUtl(adapterResponse.getTransactionMessage());

    when(processorHelper.fundAccount(transactionMessage))
        .thenReturn(worldpayTransactionMessage);

    when(processorHelper.buildLMPostingFailureResponse(any(), any()))
        .thenReturn(buildLMFailurePaymentResponse());
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    PaymentProcessResponse paymentProcessResponse2 = cardFundMessageService
        .process(transactionMessage, request);
    assertEquals("FAILED", paymentProcessResponse2.getPaymentStatus());
    assertNotNull(paymentProcessResponse2.getReason());
    assertEquals("5301", paymentProcessResponse2.getReason().getCode().toString());
  }

  @Test
  public void shouldReturnFailure_whenPdfReturnsFailureResponse() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();

    when(processorHelper.handleDecisionProcess(transactionMessage, request))
        .thenReturn(Optional.of(paymentProcessResponse));

    PaymentProcessResponse actual = cardFundMessageService
        .process(transactionMessage, request);

    assertEquals(paymentProcessResponse, actual);
  }

  @Test
  public void shouldReturnFailure_whenWorldPayCallFails() throws IOException {

    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    when(processorHelper.handleDecisionProcess(transactionMessage, request))
        .thenReturn(Optional.ofNullable(null));
    doNothing().when(processorHelper).sendtoKafka(transactionMessage, CAAA002);
    when(processorHelper.buildCaa002(any(), any(), any())).thenReturn(transactionMessage);
    when(processorHelper.fundAccount(transactionMessage))
        .thenThrow(new ResourceAccessException("failed"));

    when(processorHelper.getGenericFailureReason()).thenReturn(
        new ReasonDto(TransactionReason.GENERIC_FAILURE.getFailureCode(),
            TransactionReason.GENERIC_FAILURE.getFailureMessage()));
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);

    PaymentProcessResponse paymentProcessResponse = cardFundMessageService
        .process(transactionMessage, request);

    assertEquals(TransactionReason.GENERIC_FAILURE.getFailureMessage(),
        paymentProcessResponse.getReason().getMessage());
    assertEquals(TransactionReason.GENERIC_FAILURE.getFailureCode(),
        (int) paymentProcessResponse.getReason().getCode());
    assertEquals("FAILED", paymentProcessResponse.getPaymentStatus());
  }

  @Test
  public void shouldReturnFailure_whenWorldPayCallFailsAndLMCallFails() throws IOException {

    PaymentProcessResponse processResponse = new PaymentProcessResponse();
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    when(processorHelper.handleDecisionProcess(transactionMessage, request))
        .thenReturn(Optional.ofNullable(null));
    when(processorHelper.buildCaa002(any(), any(), any())).thenReturn(transactionMessage);
    when(processorHelper.buildLMPostingFailureResponse(lmPostingResponse, transactionMessage))
        .thenReturn(processResponse);
    when(processorHelper.fundAccount(transactionMessage))
        .thenThrow(new ResourceAccessException("failed"));

    when(processorHelper.getGenericFailureReason()).thenReturn(
        new ReasonDto(TransactionReason.GENERIC_FAILURE.getFailureCode(),
            TransactionReason.GENERIC_FAILURE.getFailureMessage()));
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);

    PaymentProcessResponse actual = cardFundMessageService
        .process(transactionMessage, request);

    verify(processorHelper).buildLMPostingFailureResponse(lmPostingResponse, transactionMessage);
    assertEquals(processResponse, actual);
  }

  @Test
  public void shouldReturnFailure_whenWorldPayReturnsError() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);

    when(processorHelper.handleDecisionProcess(transactionMessage, request))
        .thenReturn(Optional.ofNullable(null));

    HttpClientErrorException clientErrorException =
        new HttpClientErrorException(HttpStatus.BAD_REQUEST, "",
            fileReader.getFileContent(WORLDPAY_RESPONSE_FAILED_FRAUD).getBytes(), null);
    when(processorHelper.fundAccount(transactionMessage)).thenThrow(clientErrorException);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    PaymentProcessResponse paymentProcessResponse = cardFundMessageService
        .process(transactionMessage, request);
    assertEquals("FAILED", paymentProcessResponse.getPaymentStatus());
    assertEquals(22005, (int) paymentProcessResponse.getReason().getCode());
    assertEquals(
        "Payment Refused - Fraud Suspicion - Applies only if you have a Worldpay fraud detection service",
        paymentProcessResponse.getReason().getMessage());
  }

  @Test
  public void shouldReturnFailure_whenWorldPayReturnsErrorandEncounterParsingError() throws IOException {

    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CARD_FUND_TRANSACTION_MESSAGE_REQUEST),
        TransactionMessage.class);
    PaymentProcessResponse expected = new PaymentProcessResponse();
    expected.setPaymentStatus("FAILED");
    ReasonDto reasonDto = new ReasonDto();
    reasonDto.setCode(22005);
    reasonDto.setMessage("Payment Refused - Fraud Suspicion - Applies only if you have a Worldpay fraud detection service");
    expected.setReason(reasonDto);
    Errors errors = new Errors();
    when(processorHelper.handleDecisionProcess(transactionMessage, request))
        .thenReturn(Optional.ofNullable(null));

    HttpClientErrorException clientErrorException =
        new HttpClientErrorException(HttpStatus.BAD_REQUEST, "",
            errors.toString().getBytes(), null);
    when(processorHelper.fundAccount(transactionMessage)).thenThrow(clientErrorException);
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(processorHelper.buildLMPostingFailureResponse(lmPostingResponse, transactionMessage)).thenReturn(expected);
    PaymentProcessResponse actual = cardFundMessageService
        .process(transactionMessage, request);
    assertEquals(expected, actual);
  }

  private PaymentProcessResponse buildLMFailurePaymentResponse() {
    PaymentProcessResponse response = new PaymentProcessResponse();
    response.setPaymentStatus("FAILED");
    ReasonDto reasonDto = new ReasonDto(5301, "LM Posting Failed");
    response.setReason(reasonDto);
    return response;
  }
}



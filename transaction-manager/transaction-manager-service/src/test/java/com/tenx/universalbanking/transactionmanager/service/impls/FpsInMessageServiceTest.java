package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.Pacs008Enum.INTERBANK_SETTLEMENT_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Pacs008Enum.INTERBANK_SETTLEMENT_CURRENCY;
import static com.tenx.universalbanking.transactionmessage.enums.Pacs008Enum.IS_RETURN;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.FPS_IN;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.REVERSE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PACS002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_CREDIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.PdfException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.messagebuilder.PACS002MessageBuilder;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class FpsInMessageServiceTest {

  private static final String SUBSCRIPTION_ID = "12345";
  @Mock
  private MessageServiceProcessorHelper processorHelper;

  @Mock
  private MessageSender messageSender;

  @Mock
  private PACS002MessageBuilder pacs002MessageBuilder;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

  @InjectMocks
  private FpsInMessageService unit;

  @Mock
  private HttpServletRequest request;

  @Mock
  private TMExceptionBuilder exceptionBuilder;

  private static final PaymentDecisionTransactionResponse PAYMENT_DECISION_TRANSACTION_RESPONSE =
      new PaymentDecisionTransactionResponse();
  private static final String TRANSACTION_CODE = "TRANSACTION_CODE";
  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";

  private FileReaderUtil fileReader = new FileReaderUtil();
  ;

  private final String PACS002_FILE = "message/pacs002MessageResponse.json";

  @Test
  public void shouldHandleFpsInMessage() {
    assertEquals(FPS_IN, unit.getType());
  }

  @Test
  public void shouldGetPaymentResponseSuccessWhenPaymentDecisionSuccess() throws Exception {

    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    when(pacs002MessageBuilder.buildPacs002Response(any(), any()))
        .thenReturn(createPac002Message());
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
    givenIsPaymentDecisionSuccess();
    givenPaymentProcessOnLM();
    givenSubscriptionKey();
    TransactionMessage TRANSACTION_MESSAGE = buildTransactionMessage(false);

    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    doNothing().when(messageSender).sendPaymentMessage(captorStr.capture(), captor.capture());

    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);

    assertEquals(PaymentDecisionResponse.SUCCESS.name(), response.getPaymentStatus());
    verify(processorHelper).generateTransactionAndCorrelationIds(TRANSACTION_MESSAGE);
    verify(messageSender).sendPaymentMessage(captorStr.capture(), captor.capture());
    TransactionMessage message = captor.getValue();
    assertEquals(PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_CREDIT.getValue(),
        message.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE));
    assertEquals(SUCCESS, message.getAdditionalInfo().get(TRANSACTION_STATUS.name()));
  }

  @Test
  public void shouldGetPaymentResponseSuccessWhenPaymentDecisionSuccessAndLMFailure()
      throws Exception {

    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    when(pacs002MessageBuilder.buildPacs002Response(any(), any()))
        .thenReturn(createPac002Message());
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
    givenIsPaymentDecisionSuccess();
    givenSubscriptionKey();
    TransactionMessage transactionMessage = buildTransactionMessage(false);
    PaymentProcessResponse response = new PaymentProcessResponse();
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);

    when(responseConverter.buildPaymentProcessResponse(lmPostingResponse, transactionMessage))
        .thenReturn(response);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);

    PaymentProcessResponse actual = unit.process(transactionMessage, request);

    assertEquals(response, actual);
    verify(processorHelper).generateTransactionAndCorrelationIds(transactionMessage);
    verify(messageSender, times(0)).sendPaymentMessage(captorStr.capture(), captor.capture());

  }

  @Test
  public void shouldGetPaymentResponseSuccessWhenPaymentDecisionReverse() throws Exception {
    // Given
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
    givenIsPaymentDecisionSuccess();
    givenPaymentProcessOnLM();
    givenSubscriptionKey();
    TransactionMessage TRANSACTION_MESSAGE = buildTransactionMessage(true);

    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    when(pacs002MessageBuilder.buildPacs002Response(any(), any()))
        .thenReturn(createPac002Message());
    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);

    // Then
    assertEquals(PaymentDecisionResponse.SUCCESS.name(), response.getPaymentStatus());
    verify(processorHelper, never()).generateTransactionAndCorrelationIds(TRANSACTION_MESSAGE);
    verify(messageSender).sendPaymentMessage(captorStr.capture(), captor.capture());
    TransactionMessage message = captor.getValue();
    assertEquals(PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_CREDIT.getValue(),
        message.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE));
    assertEquals(REVERSE, message.getAdditionalInfo().get(TRANSACTION_STATUS.name()));
  }

  @Test
  public void shouldGetPaymentResponseFailedWhenPaymentDecisionFailed() throws IOException {
    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = buildPaymentDecisionTransactionResponse();
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    when(pacs002MessageBuilder.buildPacs002Response(any(), any()))
        .thenReturn(createPac002Message());
    givenIsPaymentDecisionFalse();
    when(exceptionBuilder.buildFromPdfResponse(any())).thenReturn(new PdfException());

    // When
    assertThrows(PdfException.class, () -> {
      unit.process(buildTransactionMessage(false), request);
    });

  }

  @Test
  public void shouldGetReasonInResponseWhenPaymentDecisionFailed() throws IOException {
    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = buildPaymentDecisionTransactionResponse();
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    when(pacs002MessageBuilder.buildPacs002Response(any(), any()))
        .thenReturn(createPac002Message());
    givenIsPaymentDecisionFalse();
    when(exceptionBuilder.buildFromPdfResponse(any())).thenReturn(new PdfException());

    assertThrows(PdfException.class, () -> {
      unit.process(buildTransactionMessage(false), request);
    });
  }

  @Test
  public void shouldGetPacs002MessageInResponse() throws Exception {

    givenPaymentProcessOnLM();
    givenIsPaymentDecisionSuccess();
    givenSubscriptionKey();

    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    when(pacs002MessageBuilder.buildPacs002Response(any(), any()))
        .thenReturn(createPac002Message());
    PaymentProcessResponse response = unit.process(buildTransactionMessage(false), request);

    assertEquals(PACS002.name(), response.getTransactionMessage().getMessages().get(0).getType());
  }

  private TransactionMessage createPac002Message() throws IOException {
    return stringToJson(fileReader.getFileContent(PACS002_FILE), TransactionMessage.class);
  }

  private void givenSubscriptionKey() {
    when(processorHelper.getSubscriptionKey(any(), any()))
        .thenReturn(SUBSCRIPTION_ID);
  }

  private void givenIsPaymentDecisionSuccess() {
    when(processorHelper.isPaymentDecisionSuccess(any())).thenReturn(true);
  }

  private void givenIsPaymentDecisionFalse() {
    when(processorHelper.isPaymentDecisionSuccess(any())).thenReturn(false);
  }

  private void givenPaymentProcessOnLM() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
  }

  private PaymentDecisionTransactionResponse buildPaymentDecisionTransactionResponse() {
    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = new PaymentDecisionTransactionResponse();
    PaymentDecisionReasonDTO paymentDecisionReasonDTO = new PaymentDecisionReasonDTO();
    paymentDecisionReasonDTO.setCode(000);
    paymentDecisionReasonDTO.setMessage("Failed");
    paymentDecisionTransactionResponse.setDecisionReason(paymentDecisionReasonDTO);
    return paymentDecisionTransactionResponse;
  }

  private TransactionMessage buildTransactionMessage(Boolean isReverse) {
    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionMessageHeader header = new TransactionMessageHeader();

    List<PaymentMessage> messages = new ArrayList();
    PaymentMessage paymentMessage = new PaymentMessage();
    Map<String, Object> message = new HashMap();
    message.put(INTERBANK_SETTLEMENT_AMOUNT.name(), Double.parseDouble("10.10"));
    message.put(INTERBANK_SETTLEMENT_CURRENCY.name(), "GBP");
    message.put(IS_RETURN.name(), isReverse);
    paymentMessage.setMessage(message);
    messages.add(paymentMessage);
    transactionMessage.setMessages(messages);
    Map<String, Object> additionalInfo = new HashMap();
    additionalInfo
        .put(TransactionMessageAdditionalInfoEnum.REQUEST_ID.name(), UUID.randomUUID().toString());
    additionalInfo.put(TRANSACTION_CORRELATION_ID.name(), "123409867");
    transactionMessage.setHeader(header);
    transactionMessage.setAdditionalInfo(additionalInfo);
    transactionMessage.setMessages(messages);
    return transactionMessage;
  }
}

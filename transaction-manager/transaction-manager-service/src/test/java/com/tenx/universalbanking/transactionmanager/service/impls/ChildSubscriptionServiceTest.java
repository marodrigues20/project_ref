package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.FAILED;
import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.SUCCESS;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CHILD_SUBSCRIPTION_TRANSFER;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_MISCELLANEOUS_CREDIT_OPERATION_OTHER_INTERNAL_CREDIT_POOL_TRANSFER;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_MISCELLANEOUS_DEBIT_OPERATION_OTHER_INTERNAL_DEBIT_POOL_TRANSFER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class ChildSubscriptionServiceTest {

  @Mock
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Mock
  private MessageSender messageSender;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

  @InjectMocks
  private ChildSubscriptionService unit;

  @Mock
  private HttpServletRequest request;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";

  private static final TransactionMessage TRANSACTION_MESSAGE = new TransactionMessage();
  private static final PaymentDecisionTransactionResponse PAYMENT_DECISION_TRANSACTION_RESPONSE =
      new PaymentDecisionTransactionResponse();

  private static final String CHILD_SUBSCRIPTION_TRANSACTION_MESSAGE =
      "message/ChildSubscriptionTransactionMessageRequest.json";
  private static final String TRANSACTION_CODE = "TRANSACTION_CODE";
  private final FileReaderUtil fileReader = new FileReaderUtil();


  @Test
  public void shouldGetFPSINHeaderWhenCallGetType() {
    String actual = unit.getType().name();
    assertEquals(CHILD_SUBSCRIPTION_TRANSFER.name(), actual);
  }

  @Test
  public void shouldGetPaymentResponseSuccessWhenPaymentDecisionSuccess() throws Exception {
    when(messageServiceProcessorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    givenIsPaymentDecisionSuccess();
    givenSubscriptionKey();
    givenPaymentProcessOnLM();
    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);
    assertEquals(SUCCESS.name(), response.getPaymentStatus());
  }

  @Test
  public void shouldGetPaymentResponseSuccessAndLMThrowsException() throws Exception {
    when(messageServiceProcessorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    givenIsPaymentDecisionSuccess();
    givenSubscriptionKey();
    when(lmClient.postTransactionToLedger(any()))
        .thenThrow(FPSOutTransactionManagerException.class);
    assertThrows(FPSOutTransactionManagerException.class, () -> {
      unit.process(TRANSACTION_MESSAGE, request);
    });
  }

  @Test
  public void shouldGetPaymentResponseFailureWhenLMPostingFailure() throws Exception {
    when(messageServiceProcessorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    givenIsPaymentDecisionSuccess();
    givenSubscriptionKey();
    givenPaymentProcessOnLMFailure(TRANSACTION_MESSAGE);
    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);
    assertEquals(FAILED.name(), response.getPaymentStatus());
  }

  @Test
  public void shouldGetPaymentResponseFailedWhenPaymentDecisionFailed() throws Exception {
    givenIsPaymentDecisionFalse();
    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);
    assertEquals(FAILED.name(), response.getPaymentStatus());
  }

  @Test
  public void shouldGetReasonInResponseWhenPaymentDecisionFailed() throws Exception {
    givenIsPaymentDecisionFalse();
    givenReasonDto();
    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);
    assertNotNull(response.getReason());
  }

  @Test
  public void getPaymentResponseSuccess_For_Child_ChildSubscription() throws Exception {
    when(messageServiceProcessorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(CHILD_SUBSCRIPTION_TRANSACTION_MESSAGE),
        TransactionMessage.class);
    transactionMessage.getHeader().setType(CHILD_SUBSCRIPTION_TRANSFER.name());
    TransactionMessage outputMessage = verifyTransaction(transactionMessage);
    assertEquals(
        PAYMENTS_MISCELLANEOUS_DEBIT_OPERATION_OTHER_INTERNAL_DEBIT_POOL_TRANSFER.getValue(),
        outputMessage.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE));
    assertEquals(
        PAYMENTS_MISCELLANEOUS_CREDIT_OPERATION_OTHER_INTERNAL_CREDIT_POOL_TRANSFER.getValue(),
        outputMessage.getMessages().get(1).getAdditionalInfo().get(TRANSACTION_CODE));
  }

  @Test
  public void saveReconMessageEventSuccessTest() {
    when(messageServiceProcessorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
    when(messageServiceProcessorHelper.isPaymentDecisionSuccess(any())).thenReturn(true);
    when(lmClient.postTransactionToLedger(any())).thenReturn(getLedgerPostingResponse());
    unit.process(getTransactionMessage(), request);
    verify(reconciliationHelper).saveReconciliationMessage(getReconMsgDto());
  }

  @Test
  public void saveReconMessageEventIntRejectTest() {
    when(messageServiceProcessorHelper.callPaymentDecisionService(any()))
        .thenReturn(getPaymentDecisionTransactionResponse());
    unit.process(getTransactionMessage(), request);
    ReconciliationMessageDto reconciliationMessageDto = getReconMsgDto();
    reconciliationMessageDto.setEvent(Event.INT_REJECT);
    verify(reconciliationHelper).saveReconciliationMessage(reconciliationMessageDto);
  }

  private PaymentDecisionTransactionResponse getPaymentDecisionTransactionResponse() {
    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = new PaymentDecisionTransactionResponse();
    paymentDecisionTransactionResponse.setDecisionResponse("");
    PaymentDecisionReasonDTO paymentDecisionReasonDTO = new PaymentDecisionReasonDTO();
    paymentDecisionTransactionResponse.setDecisionReason(paymentDecisionReasonDTO);
    return paymentDecisionTransactionResponse;
  }


  private LedgerPostingResponse getLedgerPostingResponse() {
    LedgerPostingResponse ledgerPostingResponse = new LedgerPostingResponse();
    ledgerPostingResponse.setPostingSuccess(true);
    return ledgerPostingResponse;
  }

  private ReconciliationMessageDto getReconMsgDto() {
    return ReconciliationMessageDto.builder().event(Event.SUCCESS)
        .serviceName(ServiceNames.TRANSACTION_MANAGER)
        .transactionCorrelationId("54545545454")
        .scope(CHILD_SUBSCRIPTION_TRANSFER)
        .build();
  }

  private TransactionMessage getTransactionMessage() {
    TransactionMessage transactionMessage = new TransactionMessage();

    Map<String, Object> paymentMap = new HashMap<>();
    Map<String, Object> addMap = new HashMap<>();
    Map<String, Object> addMapMsg = new HashMap<>();
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType("CHILD_SUBSCRIPTION_TRANSFER");
    header.setUrl("");

    paymentMap.put(REQUEST_ID.name(), "3605865611");
    paymentMap.put(TRANSACTION_CORRELATION_ID.name(), "54545545454");
    paymentMap.put(TENANT_PARTY_KEY.name(), "10000");

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setMessage(addMap);
    paymentMessage.setAdditionalInfo(addMapMsg);
    paymentMessage.setType(PAIN001.name());

    List<PaymentMessage> paymentList = new ArrayList<>();
    paymentList.add(paymentMessage);

    transactionMessage.setMessages(paymentList);
    transactionMessage.setAdditionalInfo(paymentMap);
    transactionMessage.setHeader(header);
    return transactionMessage;
  }

  private TransactionMessage verifyTransaction(TransactionMessage inboungMsg) throws Exception {
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
    givenIsPaymentDecisionSuccess();
    givenSubscriptionKey();
    givenPaymentProcessOnLM();
    PaymentProcessResponse response = unit.process(inboungMsg, request);
    assertEquals(SUCCESS.name(), response.getPaymentStatus());
    verify(messageSender).sendPaymentMessage(captorStr.capture(), captor.capture());
    return captor.getValue();
  }

  private void givenReasonDto() {

    ReasonDto reason = new ReasonDto(123, "testReason");
    when(messageServiceProcessorHelper.getFailureReason(any())).thenReturn(reason);
  }

  private void givenSubscriptionKey() {
    when(messageServiceProcessorHelper.getSubscriptionKey(any(), any())).thenReturn("12345");
  }

  private void givenIsPaymentDecisionSuccess() {
    when(messageServiceProcessorHelper.isPaymentDecisionSuccess(any())).thenReturn(true);
  }

  private void givenIsPaymentDecisionFalse() {
    when(messageServiceProcessorHelper.isPaymentDecisionSuccess(any())).thenReturn(false);
    when(messageServiceProcessorHelper.callPaymentDecisionService(any()))
        .thenReturn(buildPaymentDecisionResponse());
  }

  private PaymentDecisionTransactionResponse buildPaymentDecisionResponse() {
    PaymentDecisionTransactionResponse response = new PaymentDecisionTransactionResponse();
    response.setTransactionMessage(buildTransactionMessage());
    response.setDecisionReason(buildDecisionReason());
    return response;
  }

  private PaymentDecisionReasonDTO buildDecisionReason() {
    PaymentDecisionReasonDTO reasonDTO = new PaymentDecisionReasonDTO();
    reasonDTO.setCode(8006);
    reasonDTO.setMessage("Rules failed");
    return reasonDTO;
  }

  private com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage buildTransactionMessage() {
    com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage transactionMessage = new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage();
    transactionMessage.setHeader(buildHeader());
    return transactionMessage;
  }

  private com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessageHeader buildHeader() {
    com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessageHeader header = new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessageHeader();
    header.setType(TransactionMessageTypeEnum.FPS_IN.name());
    return header;
  }

  private void givenPaymentProcessOnLM() throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
  }


  private void givenPaymentProcessOnLMFailure(TransactionMessage message) throws Exception {
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(responseConverter.buildPaymentProcessResponse(lmPostingResponse, message))
        .thenReturn(buildLMFailurePaymentResponse());
  }

  private PaymentProcessResponse buildLMFailurePaymentResponse() {
    PaymentProcessResponse response = new PaymentProcessResponse();
    response.setPaymentStatus("FAILED");
    ReasonDto reasonDto = new ReasonDto(5301, "LM Posting Failed");
    response.setReason(reasonDto);
    return response;
  }

}

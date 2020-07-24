package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.FAILED;
import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.SUCCESS;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.ON_US;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PACS008;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_MISCELLANEOUS_CREDIT_OPERATION_OTHER_INTERNAL_CREDIT;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_MISCELLANEOUS_DEBIT_OPERATION_OTHER_INTERNAL;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ONUSMessageServiceTest {

  @Mock
  private Logger logger;

  @Mock
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Mock
  private MessageSender messageSender;

  @Mock
  private LedgerManagerClient lmClient;

  @InjectMocks
  private ONUSMessageService unit;

  @Mock
  private HttpServletRequest request;

  @Mock
  private PDFTransactionMessageMapper pdfTransactionMessageMapper;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

  private static final TransactionMessage TRANSACTION_MESSAGE = new TransactionMessage();
  private static final PaymentDecisionTransactionResponse PAYMENT_DECISION_TRANSACTION_RESPONSE =
      new PaymentDecisionTransactionResponse();

  private static final String ONUSPAYEEPAYERTRANSACTION_MESSAGE = "message/ONUSTransactionMessageRequest.json";
  private static final String ONUS_PROCESS_FAILURE_ON_FAILED_DECISION_MESSAGE = "message/ONUSTransactionMessageFailedRequest.json";
  private static final String TRANSACTION_CODE = "TRANSACTION_CODE";
  private final FileReaderUtil fileReader = new FileReaderUtil();
  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";

  @Before
  public void setup() {
    when(messageServiceProcessorHelper.callPaymentDecisionService(any()))
        .thenReturn(PAYMENT_DECISION_TRANSACTION_RESPONSE);
  }

  @Test
  public void shouldGetFPSINHeaderWheanCallGetType() {
    assertThat(unit.getType(), is(ON_US));
  }

  @Test
  public void shouldGetPaymentResponseSuccessWhenPaymentDecisionSuccess() throws Exception{
    givenIsPaymentDecisionSuccess();
    givenSubscriptionKey();
    givenPaymentProcessOnLM();
    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);
    assertEquals(SUCCESS.name(), response.getPaymentStatus());
  }

  @Test
  public void shouldGetPaymentResponseFailedWhenPaymentDecisionFailed() throws Exception{
    givenIsPaymentDecisionFalse();
    givenPaymentProcessOnLM();
    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);
    assertEquals(FAILED.name(), response.getPaymentStatus());
  }

  @Test
  public void shouldGetReasonInResponseWhenPaymentDecisionFailed() throws Exception{
    givenIsPaymentDecisionFalse();
    givenReasonDto();
    givenPaymentProcessOnLM();
    PaymentProcessResponse response = unit.process(TRANSACTION_MESSAGE, request);
    assertNotNull(response.getReason());
  }

  @Test
  public void getPaymentResponseSuccess_WithTransactionCode_For_Payee() throws Exception {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(ONUSPAYEEPAYERTRANSACTION_MESSAGE),
        TransactionMessage.class);
    transactionMessage.getHeader().setType(PAIN001.name());
    TransactionMessage outputMessage = verifyTransaction(transactionMessage);
    assertEquals(outputMessage.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE),
        PAYMENTS_MISCELLANEOUS_DEBIT_OPERATION_OTHER_INTERNAL.getValue());
  }

  @Test
  public void getPaymentResponseSuccess_WithTransactionCode_For_Payer() throws Exception {
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(ONUSPAYEEPAYERTRANSACTION_MESSAGE),
        TransactionMessage.class);
    transactionMessage.getHeader().setType(PACS008.name());
    TransactionMessage outputMessage = verifyTransaction(transactionMessage);
    assertEquals(outputMessage.getMessages().get(1).getAdditionalInfo().get(TRANSACTION_CODE),
        PAYMENTS_MISCELLANEOUS_CREDIT_OPERATION_OTHER_INTERNAL_CREDIT.getValue());
  }

  @Test
  public void processFailureOnFailedDecision() throws Exception {
    //Given
    TransactionMessage transactionMessage = stringToJson(
        fileReader.getFileContent(ONUS_PROCESS_FAILURE_ON_FAILED_DECISION_MESSAGE),
        TransactionMessage.class);
    givenIsPaymentDecisionFalse();
    givenSubscriptionKey();

    //When
    PaymentProcessResponse response = unit.process(transactionMessage, request);

    //Then
    assertEquals(FAILED.name(), response.getPaymentStatus());
  }

  private TransactionMessage verifyTransaction(TransactionMessage inboungMsg) throws Exception{
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
    when(messageServiceProcessorHelper.getFailureReason(any())).thenReturn(buildDecisionReasonDto());
    when(messageServiceProcessorHelper.callPaymentDecisionService(any())).thenReturn(buildPaymentDecisionResponse());
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

  private ReasonDto buildDecisionReasonDto() {
    ReasonDto reasonDTO = new ReasonDto();
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

  private void givenPaymentProcessOnLM() throws Exception{
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_SUCCESS),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
  }

  private void givenPaymentProcessOnLMFailed() throws Exception{
    LedgerPostingResponse lmPostingResponse = stringToJson(
        fileReader.getFileContent(LM_POSTING_FAILURE),
        LedgerPostingResponse.class);
    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    when(responseConverter.buildPaymentProcessResponse(lmPostingResponse, TRANSACTION_MESSAGE)).thenReturn(buildLMFailurePaymentResponse());
  }

  private PaymentProcessResponse buildLMFailurePaymentResponse(){
    PaymentProcessResponse response = new PaymentProcessResponse();
    response.setPaymentStatus("FAILED");
    ReasonDto reasonDto = new ReasonDto(5301, "LM Posting Failed");
    response.setReason(reasonDto);
    return response;
  }
}
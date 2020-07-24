package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS_REASON;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.BACS_DD_IN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.BacsDDStatus;
import com.tenx.universalbanking.transactionmanager.enums.BacsDDStatusReason;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class BacsDirectDebitTransferServiceTest {

  @Mock
  private MessageServiceProcessorHelper processorHelper;

  @Mock
  private MessageSender messageSender;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private HttpServletRequest request;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @InjectMocks
  private BacsDirectDebitTransferService bacsDirectDebitTransferService;

  private PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = new PaymentDecisionTransactionResponse();

  private TransactionMessage transactionMessage;

  private LedgerPostingResponse lmPostingResponse;

  private static final String SUBSCRIPTION_KEY = "abc";

  @BeforeEach
  public void setupBefore() throws IOException {
    transactionMessage = buildTransactionMessage();
    lmPostingResponse = new LedgerPostingResponse();
  }

  @AfterEach
  public void setupAfter() throws IOException {
    paymentDecisionTransactionResponse.setDecisionResponse(null);
    paymentDecisionTransactionResponse.setDecisionReason(null);
  }

  @Test
  public void getTypeTest() {
    TransactionMessageTypeEnum actual = bacsDirectDebitTransferService.getType();
    assertEquals(BACS_DD_IN, actual);
  }

  @Test
  public void directDebitProcessReturnsSuccess() {
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    paymentDecisionTransactionResponse.setDecisionResponse("SUCCESS");
    lmPostingResponse.setPostingSuccess(true);
    doNothing().when(processorHelper).generateTransactionAndCorrelationIds(transactionMessage);
    when(processorHelper
        .isPaymentDecisionSuccess(paymentDecisionTransactionResponse)).thenReturn(true);
    when(lmClient
        .postTransactionToLedger(transactionMessage)).thenReturn(lmPostingResponse);
    when(processorHelper
        .getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.PACS003))
        .thenReturn(SUBSCRIPTION_KEY);
    doNothing().when(processorHelper).addTracingHeaders(transactionMessage, request);
    doNothing().when(messageSender).sendPaymentMessage(SUBSCRIPTION_KEY, transactionMessage);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    PaymentProcessResponse actual = bacsDirectDebitTransferService
        .process(transactionMessage, request);
    assertEquals("SUCCESS", actual.getPaymentStatus());
  }

  @Test
  public void directDebitProcessWhenRecivedDDInReversalStatus() {
    transactionMessage.getAdditionalInfo()
        .put(TRANSACTION_STATUS_REASON.name(), BacsDDStatusReason.DD_IN_REVERSAL.name());
    doNothing().when(messageSender).sendPaymentMessageBacs(transactionMessage);
    PaymentProcessResponse actual = bacsDirectDebitTransferService
        .process(transactionMessage, request);
    assertEquals("SUCCESS", actual.getPaymentStatus());
    assertEquals("NEW",
        actual.getTransactionMessage().getAdditionalInfo().get(TRANSACTION_STATUS.name())
            .toString());
  }

  @Test
  public void directDebitProcessWhenRecivedRejectStatus() {
    transactionMessage.getAdditionalInfo()
        .put(TRANSACTION_STATUS.name(), BacsDDStatus.REJECT.name());
    doNothing().when(messageSender).sendPaymentMessageBacs(transactionMessage);
    PaymentProcessResponse actual = bacsDirectDebitTransferService
        .process(transactionMessage, request);
    assertEquals("SUCCESS", actual.getPaymentStatus());
    assertEquals("FAILED",
        actual.getTransactionMessage().getAdditionalInfo().get(TRANSACTION_STATUS.name())
            .toString());
  }


  @Test
  public void directDebitProcessFailureForInsufficientBalanceFirstTime() {
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    paymentDecisionTransactionResponse.setDecisionResponse("FAILED");
    PaymentDecisionReasonDTO reasonDTO = new PaymentDecisionReasonDTO();
    reasonDTO.setCode(1);
    reasonDTO.setMessage("abc");
    paymentDecisionTransactionResponse.setDecisionReason(reasonDTO);
    transactionMessage.getAdditionalInfo().put(TRANSACTION_STATUS.name(), "NEW");
    when(processorHelper.isPaymentDecisionSuccess(paymentDecisionTransactionResponse))
        .thenReturn(false);
    doNothing().when(messageSender).sendPaymentMessageBacs(transactionMessage);
    PaymentProcessResponse actual = bacsDirectDebitTransferService
        .process(transactionMessage, request);
    assertEquals("FAILED",
        actual.getTransactionMessage().getAdditionalInfo().get(TRANSACTION_STATUS.name())
            .toString());
  }

  @Test
  public void directDebitProcessFailureForInsufficientBalanceSecondTime() {
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    paymentDecisionTransactionResponse.setDecisionResponse("FAILED");
    PaymentDecisionReasonDTO reasonDTO = new PaymentDecisionReasonDTO();
    reasonDTO.setCode(1);
    reasonDTO.setMessage("abc");
    paymentDecisionTransactionResponse.setDecisionReason(reasonDTO);
    transactionMessage.getAdditionalInfo().put(TRANSACTION_STATUS.name(), "FAILED");
    transactionMessage.getAdditionalInfo()
        .put(TRANSACTION_STATUS_REASON.name(), "FIRST_ATTEMPT_UNAVAILABLE_BALANCE");
    when(processorHelper.isPaymentDecisionSuccess(paymentDecisionTransactionResponse))
        .thenReturn(false);
    doNothing().when(messageSender).sendPaymentMessageBacs(transactionMessage);
    PaymentProcessResponse actual = bacsDirectDebitTransferService
        .process(transactionMessage, request);
    assertEquals("RESERVE",
        actual.getTransactionMessage().getAdditionalInfo().get(TRANSACTION_STATUS.name())
            .toString());
  }


  @Test
  public void directDebitProcessReturnsFailure() {
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    paymentDecisionTransactionResponse.setDecisionResponse("FAILED");
    PaymentDecisionReasonDTO reasonDTO = new PaymentDecisionReasonDTO();
    reasonDTO.setCode(1);
    reasonDTO.setMessage("abc");
    paymentDecisionTransactionResponse.setDecisionReason(reasonDTO);
    doNothing().when(processorHelper).generateTransactionAndCorrelationIds(transactionMessage);

    when(processorHelper
        .isPaymentDecisionSuccess(paymentDecisionTransactionResponse)).thenReturn(false);
    doNothing().when(messageSender).sendPaymentMessageBacs(transactionMessage);
    PaymentProcessResponse actual = bacsDirectDebitTransferService
        .process(transactionMessage, request);
    assertEquals("FAILED", actual.getPaymentStatus());
    assertEquals("abc", actual.getReason().getMessage());
  }

  @Test
  public void directDebitLMPostingFailure() {
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();
    paymentProcessResponse.setPaymentStatus("FAILED");
    paymentDecisionTransactionResponse.setDecisionResponse("SUCCESS");
    lmPostingResponse.setPostingSuccess(false);
    doNothing().when(processorHelper).generateTransactionAndCorrelationIds(transactionMessage);
    when(processorHelper
        .isPaymentDecisionSuccess(paymentDecisionTransactionResponse)).thenReturn(true);
    when(lmClient
        .postTransactionToLedger(transactionMessage)).thenReturn(lmPostingResponse);
    when(responseConverter.buildPaymentProcessResponse(lmPostingResponse, transactionMessage))
        .thenReturn(paymentProcessResponse);
    when(processorHelper
        .getSubscriptionKey(transactionMessage, PaymentMessageTypeEnum.PACS003))
        .thenReturn(SUBSCRIPTION_KEY);
    doNothing().when(processorHelper).addTracingHeaders(transactionMessage, request);

    PaymentProcessResponse actual = bacsDirectDebitTransferService
        .process(transactionMessage, request);
    assertEquals("FAILED", actual.getPaymentStatus());

  }

  private TransactionMessage buildTransactionMessage() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> paymentMap = new HashMap<>();
    transactionMessage = buildPaymentMessage(transactionMessage, paymentMap);
    return transactionMessage;
  }

  private TransactionMessage buildPaymentMessage(TransactionMessage transactionMessage,
      Map<String, Object> paymentMap) {

    paymentMap.put("REQUEST_ID", "05e056db86-e933-4f34-ae5e-11629990cce51558535469387");
    paymentMap
        .put("TRANSACTION_CORRELATION_ID", "0149959d69-6991-4044-ac0a-1d544307fd7e1563259309963");
    paymentMap.put("TRANSACTION_STATUS", "FAILED");
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setAdditionalInfo(paymentMap);
    List<PaymentMessage> paymentList = new ArrayList<>();
    paymentList.add(paymentMessage);
    transactionMessage.setMessages(paymentList);
    transactionMessage.setAdditionalInfo(paymentMap);
    return transactionMessage;
  }
}
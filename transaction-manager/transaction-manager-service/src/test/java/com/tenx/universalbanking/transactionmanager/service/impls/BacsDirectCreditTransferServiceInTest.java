package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.SubscriptionRequest.SubscriptionStatusEnum.ACTIVE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_FOR_BACS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS_CREDIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.BacsDCPaymentStatus;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.BacsTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class BacsDirectCreditTransferServiceInTest {

  @Mock
  private MessageServiceProcessorHelper processorHelper;

  @Mock
  private MessageSender messageSender;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @Mock
  private PaymentProcessResponseConverter responseConverter;

  @Mock
  private TMExceptionBuilder exceptionBuilder;

  @InjectMocks
  private BacsDirectCreditTransferServiceIn bacsDirectCreditTransferServiceIn;

  @Mock
  private HttpServletRequest request;

  private static final PaymentDecisionTransactionResponse paymentDecisionTransactionResponse =
      new PaymentDecisionTransactionResponse();
  private static final String TRANSACTION_CODE = "TRANSACTION_CODE";

  @Test
  public void shouldHandleBacsInMessage() {
    assertEquals(TransactionMessageTypeEnum.BACS_CT_IN,
        bacsDirectCreditTransferServiceIn.getType());
  }

  @Test
  public void shouldGenerateTransactionAndCorrelationIds() {
    TransactionMessage transactionMessage = buildTransactionMessage();
    bacsDirectCreditTransferServiceIn.process(transactionMessage, request);
    verify(processorHelper).generateTransactionAndCorrelationIds(transactionMessage);
  }

  @Test
  public void testProcess_shouldGetPaymentResponseSuccess_WhenSubNotFound() {
    TransactionMessage transactionMessage = buildTransactionMessage();
    PaymentProcessResponse response = bacsDirectCreditTransferServiceIn
        .process(transactionMessage, request);
    assertEquals(PaymentDecisionResponse.SUCCESS.name(), response.getPaymentStatus());
    verify(messageSender).sendPaymentMessageBacs(transactionMessage);
    assertEquals(PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS_CREDIT.getValue(),
        transactionMessage.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE));
  }

  @Test
  public void testProcessShouldGetPaymentResponseSuccessWhenPaymentDecisionSuccessAndAccountInactive() {
    TransactionMessage transactionMessage = buildTransactionMessageForFailure();
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    when(processorHelper.isPaymentDecisionSuccess(any())).thenReturn(true);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    PaymentProcessResponse actual = bacsDirectCreditTransferServiceIn
        .process(transactionMessage, request);

    assertEquals(PaymentDecisionResponse.SUCCESS.name(), actual.getPaymentStatus());
  }


  @Test
  public void testProcess_shouldGetReasonInResponse_WhenPaymentDecisionFailed() {
    TransactionMessage transactionMessage = buildTransactionMessageForFailure();
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    givenIsPaymentDecisionFalse();
    givenReasonDto();
    PaymentProcessResponse response = bacsDirectCreditTransferServiceIn
        .process(transactionMessage, request);
    assertNotNull(response.getReason());
  }

  @Test
  public void testProcess_shouldGetReasonInResponse_WhenPaymentDecisionFailedAndSubscriptionStatusNotPresent() {
    TransactionMessage transactionMessage = buildTransactionMessageForFailure();
    transactionMessage.getAdditionalInfo().put("SUBSCRIPTION_STATUS", null);

    doNothing().when(messageSender).sendPaymentMessageBacs(transactionMessage);

    PaymentProcessResponse response = bacsDirectCreditTransferServiceIn
        .process(transactionMessage, request);
    verify(messageSender).sendPaymentMessageBacs(transactionMessage);
    assertEquals(PaymentDecisionResponse.SUCCESS.name(), response.getPaymentStatus());
  }

  @Test
  public void testProcessWhenLMReturnTrue() {
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    TransactionMessage message = buildTransactionMessageNew();
    LedgerPostingResponse ledgerPostingResponse = buildLedgerPostingResponse();
    ledgerPostingResponse.setPostingSuccess(true);
    when(lmClient.postTransactionToLedger(message)).thenReturn(ledgerPostingResponse);
    when(processorHelper.isPaymentDecisionSuccess(paymentDecisionTransactionResponse))
        .thenReturn(true);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    doNothing().when(processorHelper).sendtoKafka(message, PaymentMessageTypeEnum.PACS008);
    PaymentProcessResponse response = bacsDirectCreditTransferServiceIn.process(message, request);

    assertEquals(PaymentDecisionResponse.SUCCESS.name(), response.getPaymentStatus());
    assertEquals(PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS_CREDIT.getValue(),
        message.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE));
  }

  @Test
  public void unknownPaymentStatusThrowsException() {

    TransactionMessage message = buildTransactionMessageNew();
    message.getAdditionalInfo()
        .put("TRANSACTION_STATUS", BacsDCPaymentStatus.PROCESSOR_FAILED_APPLIED.name());
    when(exceptionBuilder
        .buildBacsTransactionManagerException(INTERNAL_SERVER_ERROR_FOR_BACS.getStatusCode(),
            INTERNAL_SERVER_ERROR_FOR_BACS.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
        .thenThrow(
            BacsTransactionManagerException.class);
    assertThrows(BacsTransactionManagerException.class, () -> {
      bacsDirectCreditTransferServiceIn.process(message, request);
    });

  }


  @Test
  public void testProcessWhenLMReturnFalse() {
    when(processorHelper.callPaymentDecisionService(any()))
        .thenReturn(paymentDecisionTransactionResponse);
    TransactionMessage message = buildTransactionMessageNew();
    PaymentProcessResponse paymentProcessResponse = new PaymentProcessResponse();
    paymentProcessResponse.setPaymentStatus("FAILED");
    LedgerPostingResponse ledgerPostingResponse = buildLedgerPostingResponse();
    ledgerPostingResponse.setPostingSuccess(false);
    when(lmClient.postTransactionToLedger(message)).thenReturn(ledgerPostingResponse);
    when(processorHelper.isPaymentDecisionSuccess(paymentDecisionTransactionResponse))
        .thenReturn(true);
    when(responseConverter.buildPaymentProcessResponse(ledgerPostingResponse, message))
        .thenReturn(paymentProcessResponse);

    PaymentProcessResponse response = bacsDirectCreditTransferServiceIn.process(message, request);

    assertEquals(PaymentDecisionResponse.FAILED.name(), response.getPaymentStatus());
    assertEquals(PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS_CREDIT.getValue(),
        message.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE));
  }

  private LedgerPostingResponse buildLedgerPostingResponse() {
    return new LedgerPostingResponse();
  }

  private TransactionMessage buildTransactionMessageNew() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> paymentMap = new HashMap<>();
    paymentMap
        .put("TRANSACTION_STATUS", BacsDCPaymentStatus.NEW.name());
    transactionMessage = buildPaymentMessage(transactionMessage, paymentMap);
    return transactionMessage;
  }

  private void givenReasonDto() {
    ReasonDto reason = new ReasonDto();
    when(processorHelper.getGenericFailureReason()).thenReturn(reason);
  }


  private void givenIsPaymentDecisionFalse() {
    when(processorHelper.isPaymentDecisionSuccess(any())).thenReturn(false);
  }

  private TransactionMessage buildTransactionMessage() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> paymentMap = new HashMap<>();
    paymentMap
        .put("TRANSACTION_STATUS", BacsDCPaymentStatus.SUB_NOT_FOUND_NEW.name());
    transactionMessage = buildPaymentMessage(transactionMessage, paymentMap);
    return transactionMessage;
  }

  private TransactionMessage buildTransactionMessageForFailure() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> paymentMap = new HashMap<>();
    paymentMap
        .put("TRANSACTION_STATUS", BacsDCPaymentStatus.SUB_NOT_ACTIVE_NEW.name());
    transactionMessage = buildPaymentMessage(transactionMessage, paymentMap);
    return transactionMessage;
  }

  private TransactionMessage buildPaymentMessage(TransactionMessage transactionMessage,
      Map<String, Object> paymentMap) {
    paymentMap.put("TRANSACTION_CODE",
        PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_BACS_DIRECT_CREDIT_TRANSFERS);
    paymentMap.put("REQUEST_ID", "12345");
    paymentMap.put("TRANSACTION_CORRELATION_ID", "4545454");
    paymentMap.put("SUBSCRIPTION_STATUS", ACTIVE.name());
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setAdditionalInfo(paymentMap);
    List<PaymentMessage> paymentList = new ArrayList<>();
    paymentList.add(paymentMessage);
    transactionMessage.setMessages(paymentList);
    transactionMessage.setAdditionalInfo(paymentMap);
    return transactionMessage;
  }
}
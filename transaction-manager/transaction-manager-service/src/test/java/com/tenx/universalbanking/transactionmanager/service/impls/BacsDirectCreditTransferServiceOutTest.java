package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.SubscriptionRequest.SubscriptionStatusEnum.ACTIVE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_FOR_BACS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.enums.BacsDCPaymentStatus;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.BacsTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
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
public class BacsDirectCreditTransferServiceOutTest {

  @Mock
  private MessageServiceProcessorHelper processorHelper;

  @Mock
  private MessageSender messageSender;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @Mock
  private TMExceptionBuilder exceptionBuilder;

  @InjectMocks
  private BacsDirectCreditTransferServiceOut bacsDirectCreditTransferServiceOut;

  @Mock
  private HttpServletRequest request;

  @Test
  public void shouldHandleBacsInMessage() {
    assertEquals(TransactionMessageTypeEnum.BACS_CT_OUT, bacsDirectCreditTransferServiceOut.getType());
  }

  @Test
  public void processKnownPaymentStatusTest() {
    TransactionMessage transactionMessage = buildTransactionMessage();
    doNothing().when(messageSender).sendPaymentMessageBacs(transactionMessage);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    PaymentProcessResponse actual = bacsDirectCreditTransferServiceOut
        .process(transactionMessage, request);
    verify(messageSender).sendPaymentMessageBacs(transactionMessage);
    verify(reconciliationHelper).saveReconciliationMessage(any());
    assertEquals(PaymentDecisionResponse.SUCCESS.name(), actual.getPaymentStatus());
  }

  @Test
  public void unknownPaymentStatusThrowsException() {

    TransactionMessage message = buildTransactionMessageNew();
    message.getAdditionalInfo()
        .put("TRANSACTION_STATUS", BacsDCPaymentStatus.PROCESSOR_FAILED_NEW.name());
    when(exceptionBuilder
        .buildBacsTransactionManagerException(INTERNAL_SERVER_ERROR_FOR_BACS.getStatusCode(),
            INTERNAL_SERVER_ERROR_FOR_BACS.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
        .thenThrow(
            BacsTransactionManagerException.class);
    assertThrows(BacsTransactionManagerException.class, () -> {
      bacsDirectCreditTransferServiceOut.process(message, request);
    });
  }

  private TransactionMessage buildTransactionMessageNew() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> paymentMap = new HashMap<>();
    paymentMap
        .put("TRANSACTION_STATUS", BacsDCPaymentStatus.NEW.name());
    transactionMessage = buildPaymentMessage(transactionMessage, paymentMap);
    return transactionMessage;
  }

  private TransactionMessage buildTransactionMessage() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> paymentMap = new HashMap<>();
    paymentMap
        .put("TRANSACTION_STATUS", BacsDCPaymentStatus.SUB_NOT_ACTIVE_APPLIED.name());
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
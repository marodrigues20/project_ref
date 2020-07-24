package com.tenx.universalbanking.transactionmanager.rest.controller;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.factory.TransactionServiceFactory;
import com.tenx.universalbanking.transactionmanager.rest.controllers.PaymentProcessorController;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.impls.BacsDirectCreditTransferServiceIn;
import com.tenx.universalbanking.transactionmanager.service.impls.ONUSMessageService;
import com.tenx.universalbanking.transactionmanager.service.impls.RequestPaymentService;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PaymentProcessorControllerTest {

  @InjectMocks
  private PaymentProcessorController paymentProcessorController = new PaymentProcessorController();

  @Mock
  private TransactionServiceFactory transactionServiceFactory;

  @Mock
  private ONUSMessageService onusMessageService;

  @Mock
  private RequestPaymentService requestPaymentService;

  @Mock
  private BacsDirectCreditTransferServiceIn bacsDirectCreditTransferServiceIn;

  @Mock
  private HttpServletRequest request;

  private static final String TRANSACTION_MESSAGE = "message/transactionMessageRequest.json";

  private static final String REQUEST_PAYMENT_TRANSACTION_MESSAGE = "message/request-payment/requestPayment.json";

  private static final String BACS_DC_PAYMENT_TRANSACTION_MESSAGE = "message/bacs/transactionMessageRequest.json";

  private static final String SUCCESS = "SUCCESS";

  private static final String FAILED = "FAILED";

  private static final String MESSAGE = "Message";

  private final FileReaderUtil fileReader = new FileReaderUtil();


  @Test
  public void processPaymentSuccessTest() throws IOException {

    TransactionMessage transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE);
    PaymentProcessResponse paymentProcessResponce = getPaymentProcessSuccessResponse();
    when(transactionServiceFactory.getTransactionMessageService(transactionMessage)).thenReturn(
        onusMessageService);

    when(onusMessageService.process(transactionMessage, request))
        .thenReturn(paymentProcessResponce);

    PaymentProcessResponse response = paymentProcessorController.processPayment(transactionMessage);

    assertEquals(SUCCESS, response.getPaymentStatus());
  }


  @Test
  public void processPaymentFailureTest() throws IOException {

    TransactionMessage transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE);
    PaymentProcessResponse paymentProcessResponce = getPaymentProcessFailureResponse();
    when(transactionServiceFactory.getTransactionMessageService(transactionMessage)).thenReturn(
        onusMessageService);

    when(onusMessageService.process(transactionMessage, request))
        .thenReturn(paymentProcessResponce);

    PaymentProcessResponse response = paymentProcessorController.processPayment(transactionMessage);

    assertEquals(FAILED, response.getPaymentStatus());
  }

  @Test
  public void processRequestPaymentSuccessTest() throws IOException {

    TransactionMessage transactionMessage = buildTransactionMsg(
        REQUEST_PAYMENT_TRANSACTION_MESSAGE);
    PaymentProcessResponse paymentProcessResponce = getPaymentProcessSuccessResponse();
    when(transactionServiceFactory.getTransactionMessageService(transactionMessage)).thenReturn(
        requestPaymentService);

    when(requestPaymentService.process(transactionMessage, request))
        .thenReturn(paymentProcessResponce);

    PaymentProcessResponse response = paymentProcessorController.processPayment(transactionMessage);

    assertEquals(SUCCESS, response.getPaymentStatus());
  }

  @Test
  public void processRequestPaymentFailureTest() throws IOException {

    TransactionMessage transactionMessage = buildTransactionMsg(
        REQUEST_PAYMENT_TRANSACTION_MESSAGE);
    PaymentProcessResponse paymentProcessResponce = getPaymentProcessFailureResponse();
    when(transactionServiceFactory.getTransactionMessageService(transactionMessage)).thenReturn(
        requestPaymentService);

    when(requestPaymentService.process(transactionMessage, request))
        .thenReturn(paymentProcessResponce);

    PaymentProcessResponse response = paymentProcessorController.processPayment(transactionMessage);

    assertEquals(FAILED, response.getPaymentStatus());
  }

  @Test
  public void processRequestPayment3DsecureTest() throws IOException {

    TransactionMessage transactionMessage = buildTransactionMsg(
        REQUEST_PAYMENT_TRANSACTION_MESSAGE);
    PaymentProcessResponse paymentProcessResponce = getPaymentProcessFailureResponse();
    when(transactionServiceFactory.getTransactionMessageService(transactionMessage)).thenReturn(
        requestPaymentService);

    when(requestPaymentService.process(transactionMessage, request))
        .thenReturn(paymentProcessResponce);

    PaymentProcessResponse response = paymentProcessorController.processPayment(transactionMessage);

    assertEquals(FAILED, response.getPaymentStatus());
  }

  private PaymentProcessResponse getPaymentProcessSuccessResponse() {
    ReasonDto reason = new ReasonDto(200, MESSAGE);
    return new PaymentProcessResponse(SUCCESS, reason);
  }


  private PaymentProcessResponse getPaymentProcessFailureResponse() {
    ReasonDto reason = new ReasonDto(200, MESSAGE);
    return new PaymentProcessResponse(FAILED, reason);
  }

  private TransactionMessage buildTransactionMsg(String requestBody) throws IOException {
    return stringToJson(
        fileReader.getFileContent(requestBody),
        TransactionMessage.class);
  }

  @Test
  public void processBACS_DC_PaymentSuccessTest() throws IOException {

    TransactionMessage transactionMessage = buildTransactionMsg(
        BACS_DC_PAYMENT_TRANSACTION_MESSAGE);
    PaymentProcessResponse paymentProcessResponce = getPaymentProcessSuccessResponse();
    when(transactionServiceFactory.getTransactionMessageService(transactionMessage)).thenReturn(
        bacsDirectCreditTransferServiceIn);
    when(bacsDirectCreditTransferServiceIn.process(transactionMessage, request))
        .thenReturn(paymentProcessResponce);
    PaymentProcessResponse response = paymentProcessorController.processPayment(transactionMessage);
    assertEquals(SUCCESS, response.getPaymentStatus());
  }
}

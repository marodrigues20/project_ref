package com.tenx.universalbanking.transactionmanager.exception.builder;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVICE_UNAVAILABLE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.SUBSCRIPTION_KEY_NOTFOUND;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionReason.GENERIC_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.errormapping.PPErrorMapping.FPSOUT_INITIATION_SERVICE_UNAVAILABLE;
import static com.tenx.universalbanking.transactionmanager.enums.errormapping.PdfErrorMapping.DECISION_INSUFFICIENT_BALANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions;
import com.tenx.universalbanking.transactionmanager.enums.TransactionReason;
import com.tenx.universalbanking.transactionmanager.exception.BacsTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.PdfException;
import com.tenx.universalbanking.transactionmanager.exception.TransactionManagerException;
import com.tenx.validationlib.response.Error;
import com.tenx.validationlib.response.Errors;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class TMExceptionBuilderTest {
  
  @InjectMocks
  private TMExceptionBuilder tmExceptionBuilder;

  @Test
  public void shouldGetLionExceptionWhenCallingBuildTransactionException() {

    TransactionManagerExceptions transactionManagerExceptions =
        SUBSCRIPTION_KEY_NOTFOUND;
    TransactionManagerException lionException =
        tmExceptionBuilder.buildTransactionException(transactionManagerExceptions);

    assertEquals((int) lionException.getErrorCode(), transactionManagerExceptions.getStatusCode());
    assertEquals(lionException.getErrorMessage(), transactionManagerExceptions.getMessage());
  }


  @Test
  public void testFPSOutTMExceptionWhenCallingBuildTransactionException() {

    TransactionManagerExceptions transactionManagerExceptions = INTERNAL_SERVICE_UNAVAILABLE;
    FPSOutTransactionManagerException fpsOutTransactionManagerException =
        tmExceptionBuilder.buildFPSOutTransactionManagerException(INTERNAL_SERVICE_UNAVAILABLE.getStatusCode(),
            INTERNAL_SERVICE_UNAVAILABLE.getMessage(),HttpStatus.GATEWAY_TIMEOUT);

    assertEquals((int) fpsOutTransactionManagerException.getErrorCode(),
        transactionManagerExceptions.getStatusCode());
    assertEquals(fpsOutTransactionManagerException.getErrorMessage(), transactionManagerExceptions.getMessage());
    assertEquals(fpsOutTransactionManagerException.getHttpStatus(),HttpStatus.GATEWAY_TIMEOUT);
  }

  @Test
  public void testBuildFPSOutTransactionManagerException() {

    FPSOutTransactionManagerException fpsOutTransactionManagerException =
        tmExceptionBuilder.buildFPSOutTransactionManagerException(123, "test", HttpStatus.SERVICE_UNAVAILABLE);

    assertEquals(Integer.valueOf(123), fpsOutTransactionManagerException.getErrorCode());
    assertEquals("test", fpsOutTransactionManagerException.getErrorMessage());
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, fpsOutTransactionManagerException.getHttpStatus());
  }

  @Test
  public void buildBacsTransactionManagerExceptionTest() {

    BacsTransactionManagerException bacsTransactionManagerException =
        tmExceptionBuilder.buildBacsTransactionManagerException(123, "test", HttpStatus.SERVICE_UNAVAILABLE);

    assertEquals(Integer.valueOf(123), bacsTransactionManagerException.getErrorCode());
    assertEquals("test", bacsTransactionManagerException.getErrorMessage());
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, bacsTransactionManagerException.getHttpStatus());
  }

  @Test
  public void buildGenericPDFExceptionTest() {

    PdfException pdfException = PdfException.builder()
        .errorCode(TransactionReason.GENERIC_FAILURE.getFailureCode())
        .errorMessage(TransactionReason.GENERIC_FAILURE.getFailureMessage())
        .httpStatus(HttpStatus.OK).build();

    assertEquals(GENERIC_FAILURE.getFailureCode(), pdfException.getErrorCode());
    assertEquals(GENERIC_FAILURE.getFailureMessage(), pdfException.getErrorMessage());
    assertEquals(HttpStatus.OK, pdfException.getHttpStatus());
  }

  @Test
  public void buildFromPdfResponseTest() {

    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = buildPaymentDecisionTransactionResponse();
    PdfException pdfException =
        tmExceptionBuilder.buildFromPdfResponse(paymentDecisionTransactionResponse);

    assertEquals(DECISION_INSUFFICIENT_BALANCE.getTransactionManagerErrorCode(), pdfException.getErrorCode());
    assertEquals("test", pdfException.getErrorMessage());
    assertEquals(DECISION_INSUFFICIENT_BALANCE.getStatus(), pdfException.getHttpStatus());
  }

  @Test
  public void buildGenericPDFExceptionTestWithUnknownDownstreamCode() {

    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = buildPaymentDecisionTransactionResponse();
    paymentDecisionTransactionResponse.getDecisionReason().setCode(1);
    PdfException pdfException =
        tmExceptionBuilder.buildFromPdfResponse(paymentDecisionTransactionResponse);

    assertEquals(GENERIC_FAILURE.getFailureCode(), pdfException.getErrorCode());
    assertEquals(GENERIC_FAILURE.getFailureMessage(), pdfException.getErrorMessage());
    assertEquals(HttpStatus.OK, pdfException.getHttpStatus());
  }

  @Test
  public void buildFromDownstreamErrorTest() {

    Errors errors = buildErrors();
    FPSOutTransactionManagerException fpsOutTransactionManagerException =
        tmExceptionBuilder.buildFromDownstreamError(errors);

    assertEquals(FPSOUT_INITIATION_SERVICE_UNAVAILABLE.getPaymentProxyErrorCode(), fpsOutTransactionManagerException.getErrorCode());
    assertEquals("test", fpsOutTransactionManagerException.getErrorMessage());
    assertEquals(FPSOUT_INITIATION_SERVICE_UNAVAILABLE.getStatus(), fpsOutTransactionManagerException.getHttpStatus());
  }

  @Test
  public void buildFromDownstreamErrorTestWithUnknownDownstreamCOde() {

    Errors errors = buildErrors();
    errors.getErrors().get(0).setCode(1);
    FPSOutTransactionManagerException fpsOutTransactionManagerException =
        tmExceptionBuilder.buildFromDownstreamError(errors);

    assertEquals(GENERIC_FAILURE.getFailureCode(), fpsOutTransactionManagerException.getErrorCode());
    assertEquals(GENERIC_FAILURE.getFailureMessage(), fpsOutTransactionManagerException.getErrorMessage());
    assertEquals(HttpStatus.OK, fpsOutTransactionManagerException.getHttpStatus());
  }

  private PaymentDecisionTransactionResponse buildPaymentDecisionTransactionResponse() {
    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = new PaymentDecisionTransactionResponse();
    PaymentDecisionReasonDTO reasonDto = new PaymentDecisionReasonDTO();
    reasonDto.setCode(8002);
    reasonDto.setMessage("test");
    paymentDecisionTransactionResponse.setDecisionReason(reasonDto);
    return paymentDecisionTransactionResponse;
  }

  private Errors buildErrors() {
    Errors errors = new Errors();
    Error error = new Error();
    error.setCode(21101);
    error.setMessage("test");
    errors.setErrors(Collections.singletonList(error));
    return errors;
  }
}

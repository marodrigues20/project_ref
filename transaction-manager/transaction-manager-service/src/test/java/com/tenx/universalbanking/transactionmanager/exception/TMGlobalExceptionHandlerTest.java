package com.tenx.universalbanking.transactionmanager.exception;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.FAILED;
import static com.tenx.universalbanking.transactionmanager.enums.errormapping.PPErrorMapping.FPSOUT_INITIATION_SERVICE_UNAVAILABLE;
import static com.tenx.universalbanking.transactionmanager.exception.ErrorType.KAFKA_CONNECTION_TIMEOUT_EXP;
import static com.tenx.universalbanking.transactionmanager.exception.ErrorType.TM_UNKNOWN_SETTLEMENT_EXP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.validationlib.response.Error;
import com.tenx.validationlib.response.Errors;
import com.tenx.validationlib.util.ErrorMessageBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class TMGlobalExceptionHandlerTest {

  @Mock
  private ErrorAttributes errorAttributes;

  @Mock
  private ErrorMessageBuilder errorMessageBuilder;

  @Mock
  private HttpServletResponse httpServletResponse;

  @Mock
  private HttpServletRequest httpServletRequest;

  @InjectMocks
  private TMGlobalExceptionHandler tmGlobalExceptionHandler;

  @Test
  public void handleKafkaConnectionExceptionTest() {
    Error error = buildError();
    TMKafkaException tmKafkaException = new TMKafkaException(KAFKA_CONNECTION_TIMEOUT_EXP, "test");
    when(errorMessageBuilder
        .build(KAFKA_CONNECTION_TIMEOUT_EXP.getCode(), KAFKA_CONNECTION_TIMEOUT_EXP.getMessage()))
        .thenReturn(error);
    Errors actual = tmGlobalExceptionHandler
        .handleKafkaConnectionException(httpServletResponse, tmKafkaException);
    assertEquals(error, actual.getErrors().get(0));
    verify(httpServletResponse).setStatus(KAFKA_CONNECTION_TIMEOUT_EXP.getHttpStatus().value());
  }

  @Test
  public void handleTransactionManagerExceptionTest() throws Exception {
    TransactionManagerException exception = new TransactionManagerException();
    exception.setErrorCode(1);
    exception.setErrorMessage("test");

    Errors actual = tmGlobalExceptionHandler
        .handleTransactionManagerException(httpServletResponse, httpServletRequest, exception);
    assertEquals(1, actual.getErrors().get(0).getCode());
    assertEquals("test", actual.getErrors().get(0).getMessage());
    verify(httpServletResponse).setStatus(PRECONDITION_FAILED.value());
  }

  @Test
  public void handleGeneralTMExceptionTest() throws Exception {
    Error error = buildError();
    FPSOutTransactionManagerException exception = new FPSOutTransactionManagerException();
    exception.setErrorCode(1);
    exception.setErrorMessage("test");
    exception.setHttpStatus(HttpStatus.BAD_GATEWAY);
    when(httpServletRequest.getRequestURI()).thenReturn("/transaction-manager/settlement");
    when(errorMessageBuilder
        .build(TM_UNKNOWN_SETTLEMENT_EXP.getCode(), TM_UNKNOWN_SETTLEMENT_EXP.getMessage()))
        .thenReturn(error);
    Errors actual = tmGlobalExceptionHandler
        .handleGeneralTMException(httpServletResponse, httpServletRequest, exception);
    assertEquals(error, actual.getErrors().get(0));
    verify(httpServletResponse).setStatus(TM_UNKNOWN_SETTLEMENT_EXP.getHttpStatus().value());
  }

  @Test
  public void handleGeneralTMExceptionWithWrongSettlementURITest() throws Exception {
    FPSOutTransactionManagerException exception = new FPSOutTransactionManagerException();
    exception.setErrorCode(1);
    exception.setErrorMessage("test");
    exception.setHttpStatus(HttpStatus.BAD_GATEWAY);
    when(httpServletRequest.getRequestURI()).thenReturn("/transaction-manager");
    assertThrows(Exception.class,() -> {
      tmGlobalExceptionHandler
          .handleGeneralTMException(httpServletResponse, httpServletRequest, exception);
    });
  }

  @Test
  public void handlePaymentProcessExceptionTest() throws Exception {
    FPSOutTransactionManagerException exception = new FPSOutTransactionManagerException();
    exception.setErrorCode(1);
    exception.setErrorMessage("test");
    exception.setHttpStatus(HttpStatus.BAD_GATEWAY);

    Errors actual = tmGlobalExceptionHandler
        .handlePaymentProcessException(httpServletResponse, exception);
    assertEquals(1, actual.getErrors().get(0).getCode());
    assertEquals("test", actual.getErrors().get(0).getMessage());
    verify(httpServletResponse).setStatus(exception.getHttpStatus().value());
  }

  @Test
  public void handlePaymentDecisionFailureTest() {
    PdfException exception = new PdfException();
    exception.setErrorCode(1);
    exception.setErrorMessage("test");
    exception.setHttpStatus(HttpStatus.BAD_GATEWAY);

    ResponseEntity<PaymentProcessResponse> actual = tmGlobalExceptionHandler
        .handlePaymentDecisionFailure(exception);
    assertEquals(1, actual.getBody().getReason().getCode());
    assertEquals("test", actual.getBody().getReason().getMessage());
    assertEquals(FAILED.name(), actual.getBody().getPaymentStatus());
    assertEquals(HttpStatus.BAD_GATEWAY, actual.getStatusCode());
  }

  private Error buildError() {
    Error error = new Error();
    error.setCode(21101);
    error.setMessage("test");

    return error;
  }
}

package com.tenx.universalbanking.transactionmanager.exception;

import static com.tenx.universalbanking.transactionmanager.exception.ErrorType.TM_UNKNOWN_SETTLEMENT_EXP;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.validationlib.response.Errors;
import com.tenx.validationlib.response.Error;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import com.tenx.validationlib.util.ErrorMessageBuilder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
class TMGlobalExceptionHandler {

  private final Logger LOGGER = getLogger(getClass());

  @Autowired
  private ErrorAttributes errorAttributes;

  @Autowired
  private ErrorMessageBuilder errorMessageBuilder;

  private final String SETTLEMENT_URL = "/transaction-manager/settlement";

  @ExceptionHandler(TMKafkaException.class)
  @ResponseBody
  public Errors handleKafkaConnectionException(HttpServletResponse response, TMKafkaException ex) {
    LOGGER.error("Error occurred servicing the request", ex);
    ErrorType errorType = ex.getErrorType();

    response.setStatus(errorType.getHttpStatus().value());

    Errors errors = new Errors();
    errors.setErrors(
        singletonList(errorMessageBuilder.build(errorType.getCode(), errorType.getMessage())));

    return errors;
  }

  @ExceptionHandler(TransactionManagerException.class)
  @ResponseBody
  public Errors handleTransactionManagerException(HttpServletResponse response,
      HttpServletRequest request, TransactionManagerException ex)
      throws Exception {

    LOGGER.error("Error occurred servicing the request", ex);

    String url = request.getRequestURI();

    Errors errors = new Errors();
    Error error = new Error();
    error.setCode(ex.getErrorCode());
    error.setMessage(ex.getErrorMessage());
    response.setStatus(HttpStatus.PRECONDITION_FAILED.value());
    errors.setErrors(singletonList(error));
    return errors;
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public Errors handleGeneralTMException(HttpServletResponse response, HttpServletRequest request, Exception ex)
      throws Exception {

    LOGGER.error("Error occurred servicing the request", ex);

    String url = request.getRequestURI();

    Errors errors = new Errors();

    if(SETTLEMENT_URL.equals(url)){
      response.setStatus(TM_UNKNOWN_SETTLEMENT_EXP.getHttpStatus().value());
      errors.setErrors(singletonList(errorMessageBuilder.build(TM_UNKNOWN_SETTLEMENT_EXP.getCode(), TM_UNKNOWN_SETTLEMENT_EXP.getMessage())));
      return errors;
    }else{
      throw new Exception();
    }
  }

  @ExceptionHandler(FPSOutTransactionManagerException.class)
  @ResponseBody
  public Errors handlePaymentProcessException(HttpServletResponse response,
      FPSOutTransactionManagerException ex) {

    LOGGER.error("Error occurred servicing the request", ex);

    Errors errors = new Errors();
    Error error = new Error();
    error.setCode(ex.getErrorCode());
    error.setMessage(ex.getErrorMessage());
    response.setStatus(ex.getHttpStatus().value());
    errors.setErrors(singletonList(error));
    return errors;
  }

  @ExceptionHandler(PdfException.class)
  @ResponseBody
  public ResponseEntity<PaymentProcessResponse> handlePaymentDecisionFailure(PdfException pdfException) {

    PaymentProcessResponse response = new PaymentProcessResponse();
    response.setPaymentStatus(PaymentDecisionResponse.FAILED.name());
    response.setReason(new ReasonDto(pdfException.getErrorCode(), pdfException.getErrorMessage()));

    return new ResponseEntity(response, pdfException.getHttpStatus());
  }
}

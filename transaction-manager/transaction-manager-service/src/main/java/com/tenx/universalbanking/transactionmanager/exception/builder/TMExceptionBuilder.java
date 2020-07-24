package com.tenx.universalbanking.transactionmanager.exception.builder;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.enums.errormapping.PPErrorMapping;
import com.tenx.universalbanking.transactionmanager.enums.errormapping.PdfErrorMapping;
import com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions;
import com.tenx.universalbanking.transactionmanager.enums.TransactionReason;
import com.tenx.universalbanking.transactionmanager.exception.BacsTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.PdfException;
import com.tenx.universalbanking.transactionmanager.exception.TransactionManagerException;
import com.tenx.validationlib.response.Errors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TMExceptionBuilder {

  public TransactionManagerException buildTransactionException(
      TransactionManagerExceptions transactionManagerException) {

    TransactionManagerException exception = new TransactionManagerException();
    exception.setErrorCode(transactionManagerException.getStatusCode());
    exception.setErrorMessage(transactionManagerException.getMessage());
    exception.setFieldName(null);

    return exception;
  }

  public FPSOutTransactionManagerException buildFPSOutTransactionManagerException(int errorCode,
      String errorMessage,
      HttpStatus httpStatus) {

    FPSOutTransactionManagerException exception = new FPSOutTransactionManagerException();
    exception.setErrorCode(errorCode);
    exception.setErrorMessage(errorMessage);
    exception.setHttpStatus(httpStatus);
    exception.setFieldName(null);

    return exception;
  }

  public BacsTransactionManagerException buildBacsTransactionManagerException(int errorCode,
      String errorMessage,
      HttpStatus httpStatus) {

    return new BacsTransactionManagerException().builder().errorCode(errorCode)
        .errorMessage(errorMessage).httpStatus(httpStatus).fieldName(null).build();
  }

  public FPSOutTransactionManagerException buildFromDownstreamError(Errors errors) {

    FPSOutTransactionManagerException fpsOutException = new FPSOutTransactionManagerException();
    PPErrorMapping mapping = PPErrorMapping.getEnum(errors.getErrors().get(0).getCode());

    if (!Objects.isNull(mapping)) {
      fpsOutException.setHttpStatus(mapping.getStatus());
      fpsOutException.setErrorCode(mapping.getPaymentProxyErrorCode());
      fpsOutException.setErrorMessage(errors.getErrors().get(0).getMessage());

      return fpsOutException;
    }

    fpsOutException.setHttpStatus(HttpStatus.OK);
    fpsOutException.setErrorCode(TransactionReason.GENERIC_FAILURE.getFailureCode());
    fpsOutException.setErrorMessage(TransactionReason.GENERIC_FAILURE.getFailureMessage());

    return fpsOutException;
  }

  public PdfException buildFromPdfResponse(PaymentDecisionTransactionResponse response) {
    PdfErrorMapping mapping = PdfErrorMapping.getEnum(response.getDecisionReason().getCode());

    if (!Objects.isNull(mapping)) {

      return PdfException.builder()
          .errorCode(mapping.getTransactionManagerErrorCode())
          .errorMessage(response.getDecisionReason().getMessage())
          .httpStatus(mapping.getStatus()).build();
    }

    return buildGenericPDFException();
  }

  private PdfException buildGenericPDFException() {
    return PdfException.builder()
        .errorCode(TransactionReason.GENERIC_FAILURE.getFailureCode())
        .errorMessage(TransactionReason.GENERIC_FAILURE.getFailureMessage())
        .httpStatus(HttpStatus.OK).build();
  }
}

package com.tenx.universalbanking.transactionmanager.enums.errormapping;

import com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public enum PPErrorMapping {

  FPSOUT_INITIATION_SERVICE_UNAVAILABLE(21101,
      TransactionManagerExceptionCodes.FPSOUT_INITIATION_SERVICE_UNAVAILABLE,
      HttpStatus.SERVICE_UNAVAILABLE),
  FPSOUT_SUBMISSION_SERVICE_UNAVAILABLE(21102,
      TransactionManagerExceptionCodes.FPSOUT_SUBMISSION_SERVICE_UNAVAILABLE,
      HttpStatus.SERVICE_UNAVAILABLE),
  FPSOUT_INITIATION_TIMEOUT_ERROR(21103,
      TransactionManagerExceptionCodes.FPSOUT_INITIATION_TIMEOUT_ERROR,
      HttpStatus.INTERNAL_SERVER_ERROR),
  FPSOUT_SUBMISSION_TIMEOUT_ERROR(21104,
      TransactionManagerExceptionCodes.FPSOUT_SUBMISSION_TIMEOUT_ERROR,
      HttpStatus.INTERNAL_SERVER_ERROR);

  private final int downstreamErrorCode;
  private final int paymentProxyErrorCode;
  private final HttpStatus status;

  PPErrorMapping(int downstreamErrorCode, int paymentProxyErrorCode,
      HttpStatus status) {
    this.downstreamErrorCode = downstreamErrorCode;
    this.paymentProxyErrorCode = paymentProxyErrorCode;
    this.status = status;
  }

  public static PPErrorMapping getEnum(int downstreamErrorCode) {

    Optional<PPErrorMapping> errorMapping = Arrays.stream(PPErrorMapping.values())
        .filter(emap -> emap.getDownstreamErrorCode() == downstreamErrorCode)
        .findAny();

    return errorMapping.orElse(null);

  }

  private int getDownstreamErrorCode() {
    return downstreamErrorCode;
  }

  public int getPaymentProxyErrorCode() {
    return paymentProxyErrorCode;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
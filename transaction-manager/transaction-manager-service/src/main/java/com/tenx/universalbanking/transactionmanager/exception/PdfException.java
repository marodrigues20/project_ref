package com.tenx.universalbanking.transactionmanager.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PdfException extends RuntimeException {

  private Integer errorCode;
  private String errorMessage;
  private HttpStatus httpStatus;
}

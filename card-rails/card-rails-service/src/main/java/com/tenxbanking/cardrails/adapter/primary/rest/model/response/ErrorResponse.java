package com.tenxbanking.cardrails.adapter.primary.rest.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorResponse {

  private String message;
  private String code;

  public ErrorResponse(String message) {
    this.message = message;
  }
}

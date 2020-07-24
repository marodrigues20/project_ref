package com.tenx.universalbanking.transactionmanager.rest.request.turbine;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Transaction {

  @ApiModelProperty(required = true, value = "The transaction amounts and currencies.")
  @JsonProperty("amounts")
  private Amounts amounts;

  @ApiModelProperty(value = "As per DE-63.1")
  @JsonProperty("networkCode")
  private String networkCode;

  @ApiModelProperty(value = "The banknet reference number from DE-63.8.")
  @JsonProperty("banknetReferenceNumber")
  private String banknetReferenceNumber;

  @JsonProperty("dates")
  private TransactionRelatedDates transactionRelatedDates ;

  @JsonProperty("retrievalReferenceNumber")
  private String retrievalReferenceNumber;

}


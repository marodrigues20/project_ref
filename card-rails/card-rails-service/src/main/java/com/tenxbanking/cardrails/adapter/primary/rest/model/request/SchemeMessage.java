package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class SchemeMessage {

  @ApiModelProperty
  @JsonProperty("messageType")
  private MessageTypeEnum messageType;

  @ApiModelProperty(required = true, value = "The contents of DE-3 untouched. This represents the type of processing this message relates to.")
  @NotNull
  @JsonProperty("processingCode")
  private String processingCode;

  @ApiModelProperty(required = true, value = "The contents of DE-11 untouched. This is the STAN.")
  @NotNull
  @JsonProperty("systemTraceNumber")
  private String systemTraceNumber;

  @ApiModelProperty(required = false)
  //@NotNull
  @Valid
  @JsonProperty("transaction")
  private Transaction transaction;

  @ApiModelProperty(required = true)
  @NotNull
  @Valid
  @JsonProperty("card")
  private Card card;

  @ApiModelProperty(required = true)
  @NotNull
  @Valid
  @JsonProperty("acquirer")
  private Acquirer acquirer;

  @ApiModelProperty(required = true)
  @NotNull
  @Valid
  @JsonProperty("merchant")
  private Merchant merchant;

  @ApiModelProperty(required = true)
  @NotNull
  @Valid
  @JsonProperty("pos")
  private Pos pos;

  @JsonProperty("additionalData")
  private AdditionalData additionalData;

  @JsonProperty("fraudScoreData")
  private FraudScoringData fraudScoreData;

  @JsonProperty("reversalAmounts")
  private ReversalAmounts reversalAmounts;

  @ApiModelProperty(value = "This is only populated on a Reversal or Advice. It is the unique Id for an authorization as per DE-38")
  @Size(min = 6, max = 6)
  @JsonProperty("authCode")
  private String authCode;

  @ApiModelProperty(value = "This is only populated on a Reversal or Advice as defined by DE-39")
  @Size(min = 2, max = 2)
  @JsonProperty("authResponseCode")
  private String authResponseCode;

}


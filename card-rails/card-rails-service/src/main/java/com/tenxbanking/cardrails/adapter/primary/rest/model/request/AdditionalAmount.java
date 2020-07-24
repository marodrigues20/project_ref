package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class AdditionalAmount {

  @ApiModelProperty(value = "Account type as per DE-54.1")
  @Pattern(regexp = "^[0-9]")
  @Size(min = 2, max = 2)
  @JsonProperty("accountType")
  private String accountType = null;

  @ApiModelProperty(value = "Account type as per DE-54.2")
  @Pattern(regexp = "^[0-9]")
  @Size(min = 2, max = 2)
  @JsonProperty("amountType")
  private String amountType = null;

  @ApiModelProperty(value = "Currency code as per DE-54.3")
  @Pattern(regexp = "^[A-Za-z]")
  @Size(min = 3, max = 3)
  @JsonProperty("currencyCode")
  private String currencyCode = null;

  @ApiModelProperty(value = "Amount type as per DE-54.4")
  @JsonProperty("creditOrDebit")
  private CreditDebitEnum creditOrDebit = null;

  @ApiModelProperty(value = "Amount type as per DE-54.4")
  @JsonProperty("amount")
  private BigDecimal amount = null;

}


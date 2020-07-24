package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Amounts {

  @ApiModelProperty(required = true, value = "The transaction amount and currency. DE-4 and DE-49.")
  @NotNull
  @Valid
  @JsonProperty("transaction")
  private Money transaction;

  @ApiModelProperty(value = "The Settlement amount and currency. DE-5.")
  @Valid
  @JsonProperty("settlement")
  private Money settlement;

  @ApiModelProperty(value = "The card holder billling amount and currency. DE-6 and DE-51.")
  @Valid
  @JsonProperty("billing")
  private Money billing;

  @JsonProperty("fee")
  private Money fee;

  @ApiModelProperty(value = "All the additional amounts from DE-54")
  @JsonProperty("additionalAmounts")
  @Valid
  private List<AdditionalAmount> additionalAmounts;

  @ApiModelProperty(required = true, value = "The rate used to convert from the transaction amount to the billing amount DE-10.")
  @NotNull
  @Valid
  @JsonProperty("conversionRate")
  private BigDecimal conversionRate;


}


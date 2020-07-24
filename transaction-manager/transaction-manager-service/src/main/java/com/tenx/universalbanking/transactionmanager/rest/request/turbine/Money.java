package com.tenx.universalbanking.transactionmanager.rest.request.turbine;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Money {

  @ApiModelProperty(required = true, value = "The amount of money this represents.")
  @NotNull
  @Valid
  @JsonProperty("amount")
  private BigDecimal amount;

  @ApiModelProperty(required = true, value = "The currency of the amount.")
  @NotNull
  @Pattern(regexp = "^[A-Za-z]")
  @Size(min = 3, max = 3)
  @JsonProperty("currency")
  private String currency;


}


package com.tenx.universalbanking.transactionmanager.rest.request.turbine;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
class ReversalAmounts {

  @JsonProperty("transaction")
  private BigDecimal transaction = null;

  @JsonProperty("settlement")
  private BigDecimal settlement = null;

  @JsonProperty("billing")
  private BigDecimal billing = null;

}


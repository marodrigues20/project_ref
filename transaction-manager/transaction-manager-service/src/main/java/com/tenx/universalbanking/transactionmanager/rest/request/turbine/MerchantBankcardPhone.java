package com.tenx.universalbanking.transactionmanager.rest.request.turbine;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
class MerchantBankcardPhone {

  @JsonProperty("phoneNumberDialed")
  private String phoneNumberDialed;

  @JsonProperty("abbreviation")
  private String abbreviation;

  @JsonProperty("callDuration")
  private BigDecimal callDuration;

  @JsonProperty("callOriginCity")
  private String callOriginCity;

  @JsonProperty("callOriginStateOrCountryCode")
  private String callOriginStateOrCountryCode;

}


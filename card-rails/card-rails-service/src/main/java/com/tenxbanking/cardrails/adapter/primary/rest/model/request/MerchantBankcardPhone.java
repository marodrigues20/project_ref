package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class MerchantBankcardPhone {

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


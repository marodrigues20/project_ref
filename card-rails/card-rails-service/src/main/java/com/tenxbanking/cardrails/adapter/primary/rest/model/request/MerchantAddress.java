package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@ApiModel(description = "The address of the merchant from DE-43.")
@Validated
@Data
public class MerchantAddress {

  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 0, max = 13)
  @JsonProperty("cityName")
  private String cityName = null;

  @Pattern(regexp = "^[0-9]")
  @Size(min = 3, max = 3)
  @JsonProperty("countryCode")
  private String stateOrCountryCode;

  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(max = 10)
  @JsonProperty("postCode")
  private String postCode = null;


}


package com.tenx.universalbanking.transactionmanager.rest.request.turbine;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
class Acquirer {

  @ApiModelProperty(value = "The country code of the acquiring institution DE-19.")
  @JsonProperty("id")
  private String id;

  @ApiModelProperty(value = "The Id of the acquiring institution DE-32.")
  @JsonProperty("countryCode")
  private String countryCode;

}


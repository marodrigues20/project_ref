package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class Acquirer {

  @ApiModelProperty(value = "The country code of the acquiring institution DE-19.")
  @JsonProperty("id")
  private String id;

  @ApiModelProperty(value = "The Id of the acquiring institution DE-32.")
  @JsonProperty("countryCode")
  private String countryCode;

}


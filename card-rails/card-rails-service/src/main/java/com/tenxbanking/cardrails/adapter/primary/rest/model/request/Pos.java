package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Pos {

  @ApiModelProperty(value = "As per DE-25")
  @JsonProperty(required = true, value = "conditionCode")
  private String conditionCode;

  @ApiModelProperty(required = true, value = "As per DE-61")
  @NotNull
  @JsonProperty("additionalPosDetail")
  private String additionalPosDetail;

  @ApiModelProperty(value = "As per DE-22")
  @JsonProperty("posEntryMode")
  private String posEntryMode;

  @ApiModelProperty(value = "As per DE-48.61")
  @JsonProperty("extendedDataConditionCodes")
  private String extendedDataConditionCodes;


}


package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class AdditionalData {

  @ApiModelProperty(value = "DE-108")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 0, max = 999)
  @JsonProperty("moneysendRef")
  private String moneysendRef;

  @ApiModelProperty(value = "DE-48.23")
  @Size(min = 2, max = 2)
  @JsonProperty("paymentInitiationChannel")
  private String paymentInitiationChannel;

  @ApiModelProperty(value = "DE-48.26")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 3, max = 3)
  @JsonProperty("walletProgramData")
  private String walletProgramData;

  @ApiModelProperty(value = "DE-48.33")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 25, max = 25)
  @JsonProperty("panMappingFileInformation")
  private String panMappingFileInformation;

  @ApiModelProperty(value = "DE-48.63")
  @Size(min = 15, max = 15)
  @JsonProperty("traceId")
  private String traceId;

  @ApiModelProperty(value = "DE-48.42")
  @Pattern(regexp = "^[0-9]")
  @Size(min = 3, max = 3)
  @JsonProperty("eCommerceIndicator")
  private String eCommerceIndicator;

  @ApiModelProperty(value = "DE-48.66")
  @Pattern(regexp = "^[0-9]")
  @Size(min = 3, max = 3)
  @JsonProperty("authenticationProtocol")
  private String authenticationProtocol;

  @ApiModelProperty(value = "DE-48.71")
  @Size(min = 40, max = 40)
  @JsonProperty("onBehalfOfServices")
  private String onBehalfOfServices;

  @ApiModelProperty(value = "DE-48.83")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 1, max = 1)
  @JsonProperty("AVSResponse")
  private String avSResponse;

}


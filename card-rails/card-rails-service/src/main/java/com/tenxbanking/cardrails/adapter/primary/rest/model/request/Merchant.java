package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Merchant {

  @ApiModelProperty(value = "The merchant name. DE-43")
  @JsonProperty("name")
  private String name;

  @JsonProperty("address")
  private MerchantAddress address;

  @ApiModelProperty(value = "The Id of the terminal DE-41")
  @JsonProperty("terminalId")
  private String terminalId;

  @ApiModelProperty(value = "The MCC of the merchant DE-18")
  @JsonProperty("categoryCode")
  private String categoryCode;

  @ApiModelProperty(value = "The acceptor code DE-42")
  @JsonProperty("acceptorIdCode")
  private String acceptorIdCode;

  @JsonProperty("bankcardPhone")
  private MerchantBankcardPhone bankcardPhone;

}


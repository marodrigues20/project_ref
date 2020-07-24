package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class Card {

  @ApiModelProperty(required = true, value = "The Id used to idenfity the card and is mapped to a subscription. DE-102")
  @NotNull
  @JsonProperty("id")
  private String id;

  @ApiModelProperty(required = true, value = "The expiry date of the card MMYY. DE-14")
  @Size(min = 4, max = 4)
  @JsonProperty("expiryDate")
  private String expiryDate;

}


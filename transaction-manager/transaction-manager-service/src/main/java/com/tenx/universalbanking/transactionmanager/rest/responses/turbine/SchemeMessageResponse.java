package com.tenx.universalbanking.transactionmanager.rest.responses.turbine;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Money;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemeMessageResponse   {

  @ApiModelProperty(required = true, value = "The unique code to identify the authorization.")
  @NotNull
  @JsonProperty("authCode")
  private String authCode = null;

  @ApiModelProperty(required = true, value = "The available balance after the transaction has been processed.")
  @NotNull
  @JsonProperty("updatedBalance")
  private Money updatedBalance = null;

  @ApiModelProperty(required = true, value = "This is the response as defined by the scheme to populate field DE-39.")
  @NotNull
  @JsonProperty("reasonCode")
  private String reasonCode = null;

}


package com.tenxbanking.cardrails.adapter.primary.rest.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tenxbanking.cardrails.adapter.primary.rest.model.request.Money;
import com.tenxbanking.cardrails.domain.model.Cain002;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemeMessageResponse {

  public static SchemeMessageResponse of(Cain002 cain002) {
    return new SchemeMessageResponse(
        cain002.getAuthCode(),
        cain002.getUpdatedBalance() == null ? null : Money.fromDomain(cain002.getUpdatedBalance()),
        cain002.getAuthResponseCode().toString());
  }

  @ApiModelProperty(required = true, value = "The unique code to identify the authorization.")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 6, max = 6)
  @JsonProperty("authCode")
  private String authCode;

  @ApiModelProperty(required = true, value = "The available balance after the transaction has been processed.")
  @NotNull
  @Valid
  @JsonProperty("updatedBalance")
  private Money updatedBalance;

  @ApiModelProperty(required = true, value = "This is the response as defined by the scheme to populate field DE-39.")
  @NotNull
  @Pattern(regexp = "^[0-9]")
  @Size(min = 2, max = 2)
  @JsonProperty("reasonCode")
  private String reasonCode;

}


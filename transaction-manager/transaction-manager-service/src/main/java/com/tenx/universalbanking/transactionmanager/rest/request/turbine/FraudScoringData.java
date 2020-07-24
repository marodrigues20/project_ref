package com.tenx.universalbanking.transactionmanager.rest.request.turbine;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
class FraudScoringData {

  @ApiModelProperty(value = "this is the contents of DE-48.75 it represents the  fraud score data")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 32, max = 32)
  @JsonProperty("rawData")
  private String rawData = null;

  @ApiModelProperty(value = "The risk score as calculated by the Way4 risk module DE-112.50")
  @Pattern(regexp = "^[0-9]")
  @Size(min = 4, max = 4)
  @JsonProperty("way4RiskScore")
  private String way4RiskScore = null;

  @ApiModelProperty(value = "Part of DE-48.75")
  @Valid
  @DecimalMin("0")
  @DecimalMax("999")
  @JsonProperty("fraudScore")
  private BigDecimal fraudScore = null;

  @ApiModelProperty(value = "Part of DE-48.75")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 2, max = 2)
  @JsonProperty("fraudScoreReasonCode")
  private String fraudScoreReasonCode = null;

  @ApiModelProperty(value = "Part of DE-48.75")
  @Valid
  @DecimalMin("0")
  @DecimalMax("999")
  @JsonProperty("rulesScore")
  private BigDecimal rulesScore = null;

  @ApiModelProperty(value = "Part of DE-48.75")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 2, max = 2)
  @JsonProperty("rulesReasonCode1")
  private String rulesReasonCode1 = null;

  @ApiModelProperty(value = "Part of DE-48.75")
  @Pattern(regexp = "^[A-Za-z0-9]")
  @Size(min = 2, max = 2)
  @JsonProperty("rulesReasonCode2")
  private String rulesReasonCode2 = null;


}


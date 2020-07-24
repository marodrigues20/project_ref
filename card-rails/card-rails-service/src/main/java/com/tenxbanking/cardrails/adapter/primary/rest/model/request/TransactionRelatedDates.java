package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class TransactionRelatedDates {

  @JsonProperty("transactionDate")
  private LocalDate transactionDate;

  @Pattern(regexp = "^[0-9]")
  @Size(min = 6, max = 6)
  @JsonProperty("transactionTime")
  private String transactionTime;

  @ApiModelProperty(required = true, value = "The settlement date DE-15.")
  @NotNull
  @Valid
  @JsonProperty("settlementDate")
  private LocalDate settlementDate;

  @ApiModelProperty(value = "The effective date of the conversion to billing currency DE-16")
  @JsonProperty("conversionDate")
  private LocalDate conversionDate;

  @ApiModelProperty(required = true, value = "The date and time of the transmission in UTC as per DE-7")
  @NotNull
  @Valid
  @JsonProperty("transmissionDateTime")
  private OffsetDateTime transmissionDateTime;

}


package com.tenxbanking.cardrails.adapter.primary.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.logstash.logback.encoder.org.apache.commons.lang3.math.NumberUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Money {

  @ApiModelProperty(required = true, value = "The amount of money this represents.")
  @NotNull
  @Valid
  @JsonProperty("amount")
  private BigDecimal amount;

  @ApiModelProperty(required = true, value = "The currency of the amount.")
  @NotNull
  @Pattern(regexp = "^[A-Za-z]")
  @Size(min = 3, max = 3)
  @JsonProperty("currency")
  private String currency;

  public com.tenxbanking.cardrails.domain.model.Money toDomain() {
    return com.tenxbanking.cardrails.domain.model.Money.of(amount, currency);
  }

  public static Money fromDomain(com.tenxbanking.cardrails.domain.model.Money domain) {
    return new Money(domain.getAmount(), String.valueOf(domain.getCurrency().getNumericCode()));
  }

}


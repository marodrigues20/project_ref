package com.tenxbanking.cardrails.domain.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Address {

  private final String cityName;
  private final String stateCode;
  private final String countryCode;
  private final String postCode;

}

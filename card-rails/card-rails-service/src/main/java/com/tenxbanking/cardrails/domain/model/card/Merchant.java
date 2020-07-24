package com.tenxbanking.cardrails.domain.model.card;

import com.tenxbanking.cardrails.domain.model.transaction.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Merchant {

  private final String name;
  private final Address address;
  private final String terminalId;
  private final String categoryCode;
  private final String acceptorIdCode;

}

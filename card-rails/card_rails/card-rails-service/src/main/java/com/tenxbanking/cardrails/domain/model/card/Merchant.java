package com.tenxbanking.cardrails.domain.model.card;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Merchant {

  // == Fields ==

  private String name;
  //private Address Address;
  private String terminalId;
  private String categoryCode;
  private String acceptorIdCode;

}

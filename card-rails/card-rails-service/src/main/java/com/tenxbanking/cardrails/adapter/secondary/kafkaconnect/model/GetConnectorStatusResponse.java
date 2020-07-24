package com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class GetConnectorStatusResponse {

  private String name;
  private ConnectorStatus connector;
}



package com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class GetConnectorResponse {

  private String name;
  private Map<String, Object> config;
}

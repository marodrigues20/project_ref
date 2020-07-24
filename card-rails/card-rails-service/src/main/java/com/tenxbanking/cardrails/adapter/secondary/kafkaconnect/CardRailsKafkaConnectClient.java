package com.tenxbanking.cardrails.adapter.secondary.kafkaconnect;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model.GetConnectorResponse;
import com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model.GetConnectorStatusResponse;
import java.util.Map;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "cardRailsKafkaConnectClient", url = "${downstream.cardrailskafkaconnect.host}")
public interface CardRailsKafkaConnectClient {

  @PostMapping(value = "/connectors",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<Void> createConnector(
      @NonNull final Map<String, Object> requestBody);

  @GetMapping(value = "/connectors/{connectorName}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<GetConnectorResponse> getConnector(
      @PathVariable("connectorName") @NonNull final String connectorName);

  @GetMapping(value = "/connectors/{connectorName}/status",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<GetConnectorStatusResponse> getConnectorStatus(
      @PathVariable("connectorName") @NonNull final String connectorName);

  @DeleteMapping(value = "/connectors/{connectorName}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  ResponseEntity<Void> deleteConnector(
      @PathVariable("connectorName") @NonNull final String request);
}

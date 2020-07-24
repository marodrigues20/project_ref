package com.tenxbanking.cardrails.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model.GetConnectorResponse;
import com.tenxbanking.cardrails.adapter.secondary.kafkaconnect.model.GetConnectorStatusResponse;
import java.util.Map;

public class CardRailsKafkaConnectWiremockStubs {

  private static final String CREATE_CONNECTOR_URL = "/connectors";
  private static final String GET_CONNECTOR_URL = "/connectors/%s";
  private static final String GET_CONNECTOR_STATUS_URL = "/connectors/%s/status";
  private static final String DELETE_CONNECTOR_URL = "/connectors/%s";

  public static void stubPostCreateConnector(Map<String, Object> request, boolean successResponse)
      throws JsonProcessingException {

    String requestBody = new ObjectMapper().writeValueAsString(request);

    ResponseDefinitionBuilder response = successResponse ? ok() : serverError();

    stubFor(post(urlEqualTo(CREATE_CONNECTOR_URL))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .withRequestBody(equalToJson(requestBody))
        .willReturn(response
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }

  public static void stubGetConnector(String connectorName, GetConnectorResponse response)
      throws JsonProcessingException {

    String responseBody = new ObjectMapper().writeValueAsString(response);

    String url = format(GET_CONNECTOR_URL, connectorName);
    stubFor(get(urlEqualTo(url))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withBody(responseBody)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        )
    );
  }

  public static void stubGetConnectorStatus(String connectorName,
      GetConnectorStatusResponse response)
      throws JsonProcessingException {

    String responseBody = new ObjectMapper().writeValueAsString(response);

    String url = format(GET_CONNECTOR_STATUS_URL, connectorName);
    stubFor(get(urlEqualTo(url))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .willReturn(ok().withBody(responseBody)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }

  public static void stubDeleteConnector(String connectorName) {

    String url = format(DELETE_CONNECTOR_URL, connectorName);
    stubFor(get(urlEqualTo(url))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .willReturn(ok().withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }
}

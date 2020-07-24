package com.tenxbanking.cardrails.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeResponse;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeTransactionRequest;

public class FeesAndChargesWiremockStubs {

  private static final String FEES_AND_CHARGES_ENDPOINT = "/fees-charges-calculator/v2/transaction";

  public static void stubPostFeesCharges(FeeTransactionRequest request,
      FeeResponse response)
      throws JsonProcessingException {

    String requestBody = new ObjectMapper().writeValueAsString(request);
    String responseBody = new ObjectMapper().writeValueAsString(response);

    stubFor(post(urlEqualTo(FEES_AND_CHARGES_ENDPOINT))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .withRequestBody(equalToJson(requestBody))
        .willReturn(ok()
            .withBody(responseBody)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }

}


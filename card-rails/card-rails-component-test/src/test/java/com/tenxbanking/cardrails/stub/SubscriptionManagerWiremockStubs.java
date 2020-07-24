package com.tenxbanking.cardrails.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;

public class SubscriptionManagerWiremockStubs {

  private static final String SUBSCRPTION_PRODUCTS_ENDPOINT = "/subscription-manager/v1/subscriptions/%s/products";

  public static void stubGetSubscriptionProducts(SubscriptionProductsResponse response)
      throws Exception {

    final String responseBody = new ObjectMapper().writeValueAsString(response);

    final String url = String.format(SUBSCRPTION_PRODUCTS_ENDPOINT,
        response.getSubscriptionKey());

    stubFor(get(urlEqualTo(url))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withBody(responseBody)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }
}

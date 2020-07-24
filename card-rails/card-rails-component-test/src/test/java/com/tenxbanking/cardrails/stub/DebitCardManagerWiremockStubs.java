package com.tenxbanking.cardrails.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;

public class DebitCardManagerWiremockStubs {

  private static final String DEBIT_CARD_BY_PAN_HASH = "/v1/debit-cards/by-pan-hash";
  private static final String GET_CARD_SETTINGS_PAN_HASH = "/v1/debit-cards/%s/settings";

  public static void stubPostCardByPanHashSuccess(GetCardRequest request, GetCardResponse response)
      throws JsonProcessingException {

    String requestBody = new ObjectMapper().writeValueAsString(request);
    String responseBody = new ObjectMapper().writeValueAsString(response);

    stubFor(post(urlEqualTo(DEBIT_CARD_BY_PAN_HASH))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .withRequestBody(equalToJson(requestBody))
        .willReturn(ok()
            .withBody(responseBody)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }

  public static void stubGetCardSettings(DebitCardSettingsResponse response) throws JsonProcessingException {

    stubFor(get(urlEqualTo(String.format(GET_CARD_SETTINGS_PAN_HASH, CARD_ID)))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withBody(new ObjectMapper().writeValueAsString(response))
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }

}

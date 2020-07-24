package com.tenxbanking.cardrails.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.math.BigDecimal.TEN;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.AmountDto;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.BalanceDto;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.BalanceResponse;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.ReserveResponse;
import java.math.BigDecimal;

public class LedgerManagerWiremockStubs {

  private static final String RESERVE_ENDPOINT = "/ledger-manager/v2/reserve";
  private static final String REVERSE_ENDPOINT =  "/ledger-manager/v1/authorisations/reversal";
  private static final String CONFIRM_RESERVATION_ENDPOINT =  "/ledger-manager/v1/processPayments";

  public static void stubPostReserve(ReserveResponse response)
      throws JsonProcessingException {

    String responseBody = new ObjectMapper().writeValueAsString(response);

    stubFor(post(urlEqualTo(RESERVE_ENDPOINT))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withBody(responseBody)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }

  public static void stubConfirmReservation() {

    stubFor(post(urlEqualTo(CONFIRM_RESERVATION_ENDPOINT))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }

  public static void stubPostReverse(BalanceResponse response)
      throws JsonProcessingException {


    String responseBody = new ObjectMapper().writeValueAsString(response);

    stubFor(post(urlEqualTo(REVERSE_ENDPOINT))
        .withHeader(ACCEPT, equalTo(APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withBody(responseBody)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
    );
  }
}

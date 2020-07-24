package com.tenx.tsys.proxybatch.componenttest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.tenx.tsys.proxybatch.componenttest.util.FileUtility.getFileContent;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.net.URISyntaxException;

public class WireMockStubs {

  private static final String DEBITCARD_MANAGER_SUCCESS_RESPONSE = "testdata/SettlementResponseSuccessFile.json";
  private static final String TRANSACTION_MANAGER_SUCCESS_RESPONSE = "testdata/TransactionResponseSuccessFile.json";

  static void stubDebitCardManagerResponse(WireMockServer server)
      throws IOException, URISyntaxException {
    server.stubFor(get(urlEqualTo("/v1/debit-cards/1234567890123456"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withBody(getFileContent(DEBITCARD_MANAGER_SUCCESS_RESPONSE))));
  }

  static void stubTransactionManagerResponse(WireMockServer server)
      throws IOException, URISyntaxException {

    server.stubFor(
        post(urlMatching(
            "/transaction-manager/settlement"))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withBody(getFileContent(TRANSACTION_MANAGER_SUCCESS_RESPONSE))));
  }

}

package com.tenx.universalbanking.transactionmanager.componenttest.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.io.IOException;

class WireMockUtils {

  public static boolean verify(WireMockServer server, RequestPatternBuilder requestPattern, int count) {
    boolean match = true;
    try {
      server.verify(count, requestPattern);
    } catch (VerificationException ae) {
      match = false;
    }
    return match;
  }

  public static void verifyTransactionEventsWereSent(WireMockServer server, int times,
      String fileContainingRequest) throws IOException {
    verifyWiremockCall(fileContainingRequest, "/event-data/event-data-transaction", times,
        server);
  }

  private static void verifyWiremockCall(String fileContainingRequest, String url, int count,
      WireMockServer wireMockServer)
      throws IOException {
    RequestPatternBuilder expectedRequest = buildExpectedRequest(fileContainingRequest,
        url);

    await().atMost(TEN_SECONDS)
        .until(() -> verify(wireMockServer, expectedRequest, count));
  }

  private static RequestPatternBuilder buildExpectedRequest(String fileContainingRequest,
      String url) throws IOException {
    RequestPatternBuilder request = postRequestedFor(urlMatching(url));

    if (fileContainingRequest != null) {
      String expectedEventDataReqBody = FileUtils.getFileContent(fileContainingRequest);
      request = request
          .withRequestBody(
              equalToJson(expectedEventDataReqBody, Constants.IGNORE_ARRAY_ORDER, Constants.IGNORE_EXTRA_ELEMENTS));
    }

    return request;
  }
}

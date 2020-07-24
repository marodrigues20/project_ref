package com.tenx.tsys.proxybatch.componenttest.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.tenx.tsys.proxybatch.componenttest.util.FileUtility.getFileContent;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tenx.tsys.proxybatch.componenttest.ComponentTest;
import java.io.IOException;
import java.net.URISyntaxException;

public class WiremockUtil {

  private static final int WIREMOCK_PORT = 8888;
  private static final WireMockRule server = new WireMockRule(wireMockConfig().port(WIREMOCK_PORT));

  public static WireMockRule getServer() {
    return server;
  }


  public static void stubServicePost(String url, String response)
      throws IOException, URISyntaxException {
    ComponentTest.WIREMOCK_SERVER.stubFor(
        post(urlMatching(
            url))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withBody(getFileContent(response))));
  }

}

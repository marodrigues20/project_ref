package com.tenx.tsys.proxybatch.componenttest;


import static com.tenx.tsys.proxybatch.utils.JsonUtils.stringToJson;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.tenx.tsys.proxybatch.componenttest.util.FileUtility;
import com.tenx.tsys.proxybatch.dto.request.SettlementRequestDto;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TsysProxyBatchComponentTest extends ComponentTest {

  private static final String TSYS_PROXY_BATCH_REQUEST_VALID_FILE = "testdata/SettlementRequestValidFile.json";
  private static final String TSYS_PROXY_BATCH_REQUEST_INVALID_FILE = "testdata/SettlementRequestInvalidFile.json";
  private static final String REPAIR_TOPIC_MESSAGE_FILE = "testdata/RepairTopicMessageFile.txt";

  private static final boolean NO_TRACING = false;

  @Autowired
  private RestTemplate restTemplate;

  @LocalServerPort
  int randomServerPort;

  @Test
  public void tsysProxyBatchMethod_ShouldReturnStatusOkWhenValidDataSent()
      throws IOException, URISyntaxException {
    //given
    WireMockStubs.stubDebitCardManagerResponse(WIREMOCK_SERVER);
    WireMockStubs.stubTransactionManagerResponse(WIREMOCK_SERVER);
    String url = "http://localhost:" + randomServerPort + "/v1/settlement-records";
    //when
    HttpEntity<SettlementRequestDto> entity = buildRequest(TSYS_PROXY_BATCH_REQUEST_VALID_FILE);
    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST,
        entity, Void.class);
    //then
    Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
  }

  @Test
  public void tsysProxyBatchMethod_ShouldWriteMessageOnKafkaWhenSubscriptionKeyNotFound()
      throws IOException, URISyntaxException {
    //given
    WireMockStubs.stubDebitCardManagerResponse(WIREMOCK_SERVER);
    WireMockStubs.stubTransactionManagerResponse(WIREMOCK_SERVER);
    String url = "http://localhost:" + randomServerPort + "/v1/settlement-records";
    //when
    HttpEntity<SettlementRequestDto> entity = buildRequest(TSYS_PROXY_BATCH_REQUEST_INVALID_FILE);
    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST,
        entity, Void.class);
    //then
    verifyKafkaContainsMessage(Constants.TSYS_PROXY_BATCH_REPAIR_TOPIC,
        REPAIR_TOPIC_MESSAGE_FILE, NO_TRACING);
    Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
  }

  HttpEntity<SettlementRequestDto> buildRequest(String requestBody)
      throws IOException, URISyntaxException {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(singletonList(APPLICATION_JSON));
    SettlementRequestDto requestJson = stringToJson(FileUtility.getFileContent(requestBody),
        SettlementRequestDto.class);
    return new HttpEntity<>(requestJson, headers);
  }


}


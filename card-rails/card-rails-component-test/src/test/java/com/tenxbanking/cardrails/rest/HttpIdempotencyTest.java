package com.tenxbanking.cardrails.rest;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.primary.rest.model.response.ErrorResponse;
import com.tenxbanking.cardrails.adapter.primary.rest.model.response.SchemeMessageResponse;
import com.tenxbanking.cardrails.domain.TestConstant;
import com.tenxbanking.cardrails.domain.service.handler.CardAdviceHandler;
import com.tenxbanking.cardrails.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "redis.idempotent.expire-time=20s")
public class HttpIdempotencyTest extends BaseComponentTest {

  private static final String ENDPOINT = "/api/v1/transactions/authorisation_advice";
  private static final String HEALTH_ENDPOINT = "/actuator/health";
  private static final String IDEMPOTENT_ADVICE_JSON = "/json/idempotent-advice.json";

  @MockBean
  private CardAdviceHandler cardAdviceHandler;
  @Autowired
  private TestRestTemplate testRestTemplate;

  @Before
  public void setup() {
    when(cardAdviceHandler.auth(any())).thenReturn(TestConstant.CAIN_OO2);
  }

  @Test
  public void idempotency() {

    HttpEntity<String> entity = buildEntity(FileUtils.readFile(IDEMPOTENT_ADVICE_JSON).replace("113060", "113061"));

    ResponseEntity<SchemeMessageResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, SchemeMessageResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<ErrorResponse> response2 = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, ErrorResponse.class);

    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response2.getBody().getMessage()).isEqualTo("A request with an identical request body already been processed");

    verify(cardAdviceHandler).auth(any());
  }

  @Test
  public void idempotency_allowsReplayIf500Response() {

    when(cardAdviceHandler.auth(any()))
        .thenThrow(new RuntimeException())
        .thenReturn(TestConstant.CAIN_OO2);

    HttpEntity<String> entity = buildEntity(FileUtils.readFile(IDEMPOTENT_ADVICE_JSON));

    ResponseEntity<SchemeMessageResponse> response = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, SchemeMessageResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    ResponseEntity<ErrorResponse> response2 = testRestTemplate
        .postForEntity(serverUrl.apply(ENDPOINT), entity, ErrorResponse.class);

    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(cardAdviceHandler, times(2)).auth(any());
  }

  @Test
  public void idempotency_excludesGetRequests() {

    ResponseEntity<String> response = testRestTemplate
        .getForEntity(serverUrl.apply(HEALTH_ENDPOINT), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<String> response2 = testRestTemplate
        .getForEntity(serverUrl.apply(HEALTH_ENDPOINT), String.class);

    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private HttpEntity<String> buildEntity(String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(singletonList(APPLICATION_JSON));
    headers.setContentType(APPLICATION_JSON);
    return new HttpEntity<>(json, headers);
  }

}

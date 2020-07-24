package com.tenxbanking.cardrails.client;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.cards.DebitCardManagerClient;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardRequest;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.GetCardResponse;
import com.tenxbanking.cardrails.data.DebitCardManagerDataFactory;
import com.tenxbanking.cardrails.stub.DebitCardManagerWiremockStubs;
import java.util.UUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DebitCardFallbackReaderClientTest extends BaseComponentTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();
  private static final String PAN_HASH = "5c9001eeea39ff0d004c8fef4e2979eb666323a8eb4dce75a6";

  @Autowired
  private DebitCardManagerClient unit;

  @Test
  public void testSubscriptionManagerClient() throws Exception {

    GetCardRequest request = DebitCardManagerDataFactory.getCardRequest(PAN_HASH);
    GetCardResponse response = DebitCardManagerDataFactory
        .getCardResponse(SUBSCRIPTION_KEY, PAN_HASH);

    DebitCardManagerWiremockStubs.stubPostCardByPanHashSuccess(request, response);

    ResponseEntity<GetCardResponse> actual = unit.getCardByHash(request);

    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(actual.getBody()).isEqualTo(response);
  }
}

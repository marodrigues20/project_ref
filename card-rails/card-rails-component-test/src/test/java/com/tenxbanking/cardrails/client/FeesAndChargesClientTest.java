package com.tenxbanking.cardrails.client;

import static com.tenxbanking.cardrails.data.FeesAndChargesDataFactory.getFeeResponse;
import static com.tenxbanking.cardrails.data.FeesAndChargesDataFactory.getFeeTransactionRequest;
import static com.tenxbanking.cardrails.stub.FeesAndChargesWiremockStubs.stubPostFeesCharges;
import static org.assertj.core.api.Java6Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.fees.FeesAndChargesClient;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeResponse;
import com.tenxbanking.cardrails.adapter.secondary.fees.model.FeeTransactionRequest;
import java.util.UUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class FeesAndChargesClientTest extends BaseComponentTest {

  private static final UUID SUBSCRIPTION_KEY = UUID.randomUUID();

  @Autowired
  private FeesAndChargesClient unit;

  @Test
  public void testFeesAndChargesClient() throws JsonProcessingException {

    final FeeTransactionRequest request = getFeeTransactionRequest(SUBSCRIPTION_KEY);
    final FeeResponse response = getFeeResponse(SUBSCRIPTION_KEY);
    stubPostFeesCharges(request, response);

    final ResponseEntity<FeeResponse> actual = unit.postTransaction(request);

    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(actual.getBody()).isEqualTo(response);
  }
}

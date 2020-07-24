package com.tenxbanking.cardrails.client;

import static com.tenxbanking.cardrails.data.LedgerManagerDataFactory.getReserveResponse;
import static com.tenxbanking.cardrails.data.LedgerManagerDataFactory.getTransactionMessage;
import static com.tenxbanking.cardrails.stub.LedgerManagerWiremockStubs.stubPostReserve;
import static org.assertj.core.api.Java6Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.BaseComponentTest;
import com.tenxbanking.cardrails.adapter.secondary.ledger.LedgerManagerClient;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.ReserveResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class LedgerManagerClientTest extends BaseComponentTest {

  @Autowired
  private LedgerManagerClient unit;

  @Test
  public void testLedgerManagerClient() throws JsonProcessingException {

    final TransactionMessage request = getTransactionMessage();
    final ReserveResponse response = getReserveResponse();
    stubPostReserve(response);

    final ResponseEntity<ReserveResponse> actual = unit.reserve(request);

    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(actual.getBody()).isEqualTo(response);
  }
}

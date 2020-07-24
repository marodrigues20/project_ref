package com.tenx.universalbanking.transactionmanager.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.util.Date;
import org.junit.Test;
import com.tenx.universalbanking.transactionmanager.model.ApplicationAdjustmentMessageData;

public class ApplicationAdjustmentMessageDataTest {

  @Test
  public void shouldCreateAccrualMessageData() {
    Long transactionId = 1L;
    Long partyKey = 2L;
    Long tenantPartyKey = 20L;
    String productKey = "3";
    String subscriptionKey = "123456";
    BigDecimal amount = BigDecimal.TEN;
    Date transactionDate = new Date();
    Date transactionValueDate = new Date();
    String correlationId = "123";

    ApplicationAdjustmentMessageData data = new ApplicationAdjustmentMessageData(transactionId, partyKey,
        productKey, subscriptionKey, tenantPartyKey, amount, transactionDate, transactionValueDate, correlationId);

    assertThat(data.getTransactionId(), is(transactionId));
    assertThat(data.getPartyKey(), is(partyKey));
    assertThat(data.getProductKey(), is(productKey));
    assertThat(data.getSubscriptionKey(), is(subscriptionKey));
    assertThat(data.getTenantPartyKey(), is(tenantPartyKey));
    assertThat(data.getAmount(), is(amount));
    assertThat(data.getTransactionDate(), is(transactionDate));
    assertThat(data.getTransactionValueDate(), is(transactionValueDate));
    assertThat(data.getCorrelationId(), is(correlationId));
  }

}
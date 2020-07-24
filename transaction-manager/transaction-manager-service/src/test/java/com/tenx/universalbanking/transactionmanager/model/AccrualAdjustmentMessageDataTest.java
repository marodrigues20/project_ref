package com.tenx.universalbanking.transactionmanager.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.util.Date;
import org.junit.Test;
import com.tenx.universalbanking.transactionmanager.model.AccrualAdjustmentMessageData;

public class AccrualAdjustmentMessageDataTest {

  @Test
  public void shouldCreateAccrualMessageData() {
    Long transactionId = 1L;
    Long partyKey = 2L;
    Long tenantPartyKey = 20L;
    String productKey = "3";
    String subscriptionKey = "123456";
    Date accruedDate = new Date();
    BigDecimal amount = BigDecimal.TEN;
    Date interestCompoundDate = new Date();
    Date interestApplicationDate = new Date();
    Boolean isCreateJournal = true;
    Date transactionValueDate = new Date();
    Date transactionDate = new Date();
    String correlationId = "123";

    AccrualAdjustmentMessageData data = new AccrualAdjustmentMessageData(transactionId, partyKey, productKey,
        subscriptionKey, tenantPartyKey, accruedDate, transactionValueDate, transactionDate , amount, interestCompoundDate, interestApplicationDate,
        true,correlationId);

    assertThat(data.getTransactionId(), is(transactionId));
    assertThat(data.getPartyKey(), is(partyKey));
    assertThat(data.getProductKey(), is(productKey));
    assertThat(data.getSubscriptionKey(), is(subscriptionKey));
    assertThat(data.getTenantPartyKey(), is(tenantPartyKey));
    assertThat(data.getAccruedDate(), is(accruedDate));
    assertThat(data.getAmount(), is(amount));
    assertThat(data.getInterestCompoundDate(), is(interestCompoundDate));
    assertThat(data.getInterestApplicationDate(), is(interestApplicationDate));
    assertThat(data.isCreateJournal(), is(true));
    assertThat(data.getTransactionValueDate(), is(transactionValueDate));
    assertThat(data.getTransactionDate(), is(transactionDate));
    assertThat(data.getCorrelationId(), is(correlationId));
  }

}
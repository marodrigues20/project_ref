package com.tenxbanking.cardrails;

import static com.tenxbanking.cardrails.domain.TestConstant.BANKNET_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.CORRELATION_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TRANSACTION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.ONE_SECOND;

import com.tenxbanking.cardrails.adapter.secondary.database.CockroachAuthTransactionStore;
import com.tenxbanking.cardrails.adapter.secondary.database.model.CardTransactionEntity;
import com.tenxbanking.cardrails.adapter.secondary.database.repository.CardTransactionCockroachRepository;
import com.tenxbanking.cardrails.domain.TestConstant;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.CardTransactionType;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CardAuthStoreTest extends BaseComponentTest {

  private static final String AUTH_CODE = "authCode";
  private Instant now;
  @Autowired
  private CardTransactionCockroachRepository cardTransactionCockroachRepository;

  @Autowired
  private CockroachAuthTransactionStore authCockroachCardTransactionStore;

  @Before
  public void before() {
    now = Instant.now(clock);
    cardTransactionCockroachRepository.deleteAll();
  }

  @Test
  public void saveCardAuthInCockroachDB() {

    Cain001 cain001 = TestConstant.CAIN_001;
    Cain002 cain002 = TestConstant.CAIN_OO2;

    authCockroachCardTransactionStore.save(new CardAuth(
        CARD_ID,
        SUBSCRIPTION_KEY,
        PARTY_KEY,
        PRODUCT_KEY,
        TENANT_KEY,
        cain001,
        cain002));

    List<CardTransactionEntity> cardAuthList = cardTransactionCockroachRepository.findAll();

    await().atMost(ONE_SECOND).untilAsserted(() -> assertThat(cardAuthList.size()).isEqualTo(1));

    Optional<CardTransactionEntity> optionalCardAuth = getCardAuth(cardAuthList);

    assertThat(optionalCardAuth).isPresent();
    CardTransactionEntity savedCardAuth = optionalCardAuth.get();
    assertThat(savedCardAuth.getAuthCode()).isEqualTo(cain002.getAuthCode());
    assertThat(savedCardAuth.getTransactionId()).isEqualTo(TRANSACTION_ID.toString());
    assertThat(savedCardAuth.getCorrelationId()).isEqualTo(CORRELATION_ID.toString());
    assertThat(savedCardAuth.getPartyKey()).isEqualTo(PARTY_KEY);
    assertThat(savedCardAuth.getProductKey()).isEqualTo(PRODUCT_KEY);
    assertThat(savedCardAuth.getTenantKey()).isEqualTo(TENANT_KEY);
    assertThat(savedCardAuth.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(savedCardAuth.getTransactionMessages()).hasSize(2);
    assertThat(savedCardAuth.getCreatedDate()).isEqualTo(cain001.getTransactionDate());
    assertThat(savedCardAuth.getCardId()).isEqualTo(CARD_ID);
    assertThat(savedCardAuth.getCardTransactionType()).isEqualTo(CardTransactionType.AUTH);
    assertThat(savedCardAuth.getBanknetReferenceNumber()).isEqualTo(BANKNET_REFERENCE_NUMBER);

  }

  private Optional<CardTransactionEntity> getCardAuth(List<CardTransactionEntity> cardAuthList) {
    return cardAuthList
        .stream()
        .findFirst();
  }

}

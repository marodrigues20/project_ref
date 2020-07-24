package com.tenx.universalbanking.transactionmanager.service.turbine;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenx.universalbanking.transactionmanager.exception.UnmappableCardException;
import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.model.SubscriptionStatus;
import com.tenx.universalbanking.transactionmanager.rest.responses.dcm.GetCardResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class DebitCardMapperTest {

  private static final String PAN_HASH = "5c9001eeea39ff0d004c8fef4e2979eb666323a8eb4dce75a6";
  private static final String CARD_ID = "5c9001eeea39ff0d004c8fef4e2979eb666323a8eb4dce75a7";
  private static final UUID SUBSCRIPTION_KEY = fromString("c6bc5c48-726d-450e-b50e-9293c9e9f077");
  private static final UUID PARTY_KEY = fromString("9933ad98-31cc-48e0-8a2a-ffb45c1811b7");
  private static final UUID PRODUCT_KEY = fromString("9933ad98-31cc-48e0-8a2a-ffb45c1811b8");

  private final DebitCardMapper mapper = new DebitCardMapper();

  @Test
  public void shouldMapResponseToCard() {

    Card card = mapper.toCard(getCardResponse());

    assertThat(card.getTenantKey()).isEqualTo("10000");
    assertThat(card.getCardCountryCode()).isEqualTo("GBR");
    assertThat(card.getCardCurrencyCode()).isEqualTo("GBP");
    assertThat(card.getId()).isNotEqualTo(PAN_HASH);
    assertThat(card.getId()).isEqualTo(CARD_ID);
    assertThat(card.getProcessorAccountId()).isEqualTo("1234");
    assertThat(card.getPartyKey()).isEqualTo(PARTY_KEY);
    assertThat(card.getProductKey()).isEqualTo(PRODUCT_KEY);
    assertThat(card.getSubscriptionKey()).isEqualTo(SUBSCRIPTION_KEY);
    assertThat(card.getSubscriptionStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
  }

  @Test
  public void shouldThrowErrorWhileMapping() {
    GetCardResponse getCardResponse = getCardResponse();
    getCardResponse.setSubscriptionStatus(null);

    assertThrows(UnmappableCardException.class, () -> {
      mapper.toCard(getCardResponse);
    });

  }

  private GetCardResponse getCardResponse() {
    GetCardResponse response = new GetCardResponse();
    response.setPanHash(PAN_HASH);
    response.setCardId(CARD_ID);
    response.setCardCountryCode("GBR");
    response.setCardCurrencyCode("GBP");
    response.setPartyKey("9933ad98-31cc-48e0-8a2a-ffb45c1811b7");
    response.setProductKey("9933ad98-31cc-48e0-8a2a-ffb45c1811b8");
    response.setTenantKey("10000");
    response.setSubscriptionKey(SUBSCRIPTION_KEY.toString());
    response.setSubscriptionStatus("ACTIVE");
    response.setProcessorAccountId("1234");
    return response;
  }

}
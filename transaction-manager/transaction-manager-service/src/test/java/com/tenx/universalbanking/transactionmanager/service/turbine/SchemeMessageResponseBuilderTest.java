package com.tenx.universalbanking.transactionmanager.service.turbine;

import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.AVAILABLE_BALANCE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN002;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tenx.universalbanking.transactionmanager.exception.CAIN002NotReceivedException;
import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.model.SubscriptionStatus;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.SchemeMessageResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;

public class SchemeMessageResponseBuilderTest {

  private final SchemeMessageResponseBuilder messageBuilder = new SchemeMessageResponseBuilder();

  @Test
  public void shouldCreateASuccessfulSchemeMessageResponse() {
    CardAuthResponse cardAuthResponse = create(PaymentStatusEnum.SUCCESS);
    SchemeMessageResponse response = messageBuilder.create(cardAuthResponse, createCard());

    assertThat(response.getReasonCode()).isEqualTo("00");
    assertThat(response.getAuthCode()).isEqualTo("123456");
    assertThat(response.getUpdatedBalance().getAmount()).isEqualTo(BigDecimal.ONE);
    assertThat(response.getUpdatedBalance().getCurrency()).isEqualTo("826");
  }

  @Test
  public void shouldCreateAUnsuccessfulSchemeMessageResponse() {
    CardAuthResponse cardAuthResponse = create(PaymentStatusEnum.FAILED);
    SchemeMessageResponse response = messageBuilder.create(cardAuthResponse, createCard());

    assertThat(response.getReasonCode()).isEqualTo("05");
    assertThat(response.getAuthCode()).isEqualTo("123456");
    assertThat(response.getUpdatedBalance().getAmount()).isEqualTo(BigDecimal.ONE);
    assertThat(response.getUpdatedBalance().getCurrency()).isEqualTo("826");
  }

  @Test
  public void shouldThrowExceptionWhenNoCain002WhenNoPayments() {
    CardAuthResponse response = new CardAuthResponse();
    TransactionMessage transactionMessage = new TransactionMessage();
    response.setCain002Response(transactionMessage);
    assertThatThrownBy(() -> messageBuilder.create(response, createCard())).isInstanceOf(
        CAIN002NotReceivedException.class);
  }

  @Test
  public void shouldCreateSchemeResponseWhenNoAuthCode() {
    CardAuthResponse response = new CardAuthResponse();
    TransactionMessage message = new TransactionMessage();
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN002.name());
    message.setMessages(Collections.singletonList(paymentMessage));
    response.setCain002Response(message);

    SchemeMessageResponse schemeMessageResponse = messageBuilder.create(response, createCard());

    assertThat(schemeMessageResponse.getAuthCode()).isNull();
  }

  @Test
  public void shouldCreateSchemeResponseWhenNoAvailableBalance() {
    CardAuthResponse response = new CardAuthResponse();
    TransactionMessage message = new TransactionMessage();
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN002.name());
    message.setMessages(Collections.singletonList(paymentMessage));
    response.setCain002Response(message);

    SchemeMessageResponse schemeMessageResponse = messageBuilder.create(response, createCard());

    assertThat(schemeMessageResponse.getUpdatedBalance().getCurrency()).isEqualTo("826");
    assertThat(schemeMessageResponse.getUpdatedBalance().getAmount()).isNull();
  }

  private CardAuthResponse create(PaymentStatusEnum paymentStatusEnum) {
    CardAuthResponse response = new CardAuthResponse();
    response.setCardAuthStatus(paymentStatusEnum.name());
    response.setCain002Response(createTransactionMessage());
    return response;
  }

  private TransactionMessage createTransactionMessage() {
    TransactionMessage message = new TransactionMessage();
    message.setAdditionalInfo(ImmutableMap.of(AVAILABLE_BALANCE.name(), "1"));
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN002.name());
    paymentMessage.setAdditionalInfo(ImmutableMap.of(AUTHORISATION_CODE.name(), "123456"));
    message.setMessages(ImmutableList.of(paymentMessage));
    return message;
  }

  private Card createCard() {
    return Card.builder()
        .id("id")
        .subscriptionStatus(SubscriptionStatus.ACTIVE)
        .partyKey(UUID.randomUUID())
        .tenantKey("1000")
        .productKey(UUID.randomUUID())
        .subscriptionKey(UUID.randomUUID())
        .cardCurrencyCode("GBP")
        .build();
  }

}
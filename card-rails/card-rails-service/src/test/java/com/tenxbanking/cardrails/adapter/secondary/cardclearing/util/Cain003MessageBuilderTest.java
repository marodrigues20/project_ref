package com.tenxbanking.cardrails.adapter.secondary.cardclearing.util;

import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BILLING_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BILLING_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.COMMON_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.MERCHANT_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.FEE_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.FEE_DESCRIPTION;
import static com.tenx.universalbanking.transactionmessage.enums.FeesChargesEnum.VALUE_DATE_TIME;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.DEBIT_CREDIT_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.FEE_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SETTLEMENT_DATE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_003;
import static com.tenxbanking.cardrails.domain.TestConstant.buildFee;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.CLEARING;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TaxEnum;
import com.tenxbanking.cardrails.domain.TestConstant;
import com.tenxbanking.cardrails.domain.model.Cain003;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import com.tenxbanking.cardrails.domain.model.transaction.CardClearing;
import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Cain003MessageBuilderTest {

  private static final String CARD_ID = "132321432";
  private static final UUID SUBSCRIPTION_KEY_VALUE = UUID.randomUUID();

  @InjectMocks
  private Cain003MessageBuilder unit;

  @Test
  @DisplayName("Test responsible to return a complete Transaction Message.")
  void setTransactionMessage_ShouldReturnTransactionMessage() {
    //Given
    Cain003 cain003 = CAIN_003
        .toBuilder()
        .cardTransactionType(CLEARING)
        .paymentMethodType(PaymentMethodType.UNKNOWN)
        .fee(buildFee())
        .build();

    CardClearing cardClearing = buildCardClearing(cain003);

    final Map<String, Object> transactionAdditionalInfo = createTransactionAdditionalInfo();

    //when
    TransactionMessage transactionMessageResponse = unit
        .create(cardClearing);
    //Then
    assertNotNull(transactionMessageResponse);
    assertThat(transactionMessageResponse.getHeader()).isNotNull();
    assertThat(transactionMessageResponse.getHeader().getType()).isEqualTo(CLEARING.name());
    assertEquals(transactionMessageResponse.getMessages().get(0).getType(), "FEES_AND_CHARGES");
    assertEquals(transactionMessageResponse.getMessages().get(1).getType(), "TAX");
    assertEquals(transactionMessageResponse.getMessages().get(2).getType(), "CAIN003");
    assertThat(transactionMessageResponse.getAdditionalInfo()).isEqualTo(transactionAdditionalInfo);

  }


  @Test
  void setTransactionMessage_WithoutFee() {

    //Given
    Cain003 cain003 = CAIN_003
        .toBuilder()
        .cardTransactionType(CLEARING)
        .paymentMethodType(PaymentMethodType.UNKNOWN)
        .build();
    CardClearing cardClearing = buildCardClearing(cain003);

    final Map<String, Object> transactionAdditionalInfo = createTransactionAdditionalInfo();

    //when
    TransactionMessage transactionMessageResponse = unit
        .create(cardClearing);
    //Then
    assertNotNull(transactionMessageResponse);
    assertThat(transactionMessageResponse.getHeader()).isNotNull();
    assertThat(transactionMessageResponse.getHeader().getType()).isEqualTo(CLEARING.name());
    assertEquals(transactionMessageResponse.getMessages().get(0).getType(), "CAIN003");
    assertThat(transactionMessageResponse.getAdditionalInfo()).isEqualTo(transactionAdditionalInfo);

  }


  @Test
  void setTransactionMessage_ValidateBody() {

    //Given
    Cain003 cain003 = CAIN_003
        .toBuilder()
        .cardTransactionType(CLEARING)
        .paymentMethodType(PaymentMethodType.UNKNOWN)
        .build();
    CardClearing cardClearing = buildCardClearing(cain003);

    final Map<String, Object> createTransactionMessageBody = createTransactionMessageBody();

    //when
    TransactionMessage transactionMessageResponse = unit
        .create(cardClearing);
    //Then
    assertNotNull(transactionMessageResponse);
    assertThat(transactionMessageResponse.getHeader()).isNotNull();
    assertThat(transactionMessageResponse.getHeader().getType()).isEqualTo(CLEARING.name());
    assertEquals(transactionMessageResponse.getMessages().get(0).getType(), "CAIN003");
    assertThat(transactionMessageResponse.getMessages().get(0).getMessage())
        .isEqualTo(createTransactionMessageBody);

  }


  @Test
  void setTransactionMessage_ValidateFee() {

    //Given
    Cain003 cain003 = CAIN_003
        .toBuilder()
        .cardTransactionType(CLEARING)
        .paymentMethodType(PaymentMethodType.UNKNOWN)
        .fee(buildFee())
        .build();
    CardClearing cardClearing = buildCardClearing(cain003);

    final Map<String, Object> createTransactionMessageFeeBody = createTransactionFeeBody();

    //when
    TransactionMessage transactionMessageResponse = unit
        .create(cardClearing);
    //Then
    assertNotNull(transactionMessageResponse);
    assertThat(transactionMessageResponse.getHeader()).isNotNull();
    assertThat(transactionMessageResponse.getHeader().getType()).isEqualTo(CLEARING.name());
    assertEquals(transactionMessageResponse.getMessages().get(0).getType(), "FEES_AND_CHARGES");
    assertThat(transactionMessageResponse.getMessages().get(0).getMessage())
        .isEqualTo(createTransactionMessageFeeBody);

  }


  private Map<String, Object> createTransactionFeeBody() {

    Map<String, Object> map = new HashMap<>();

    map.put(TaxEnum.TRANSACTION_CODE.name(), "TSX");
    map.put(TaxEnum.TRANSACTION_DATE.name(), "22/11/2019");
    map.put(FEE_CURRENCY_CODE.name(), "12312");
    map.put(VALUE_DATE_TIME.name(), "22/11/2019");
    map.put(FEE_AMOUNT.name(), TEN);
    map.put(FEE_DESCRIPTION.name(), "teste");

    return map;
  }


  private Map<String, Object> createTransactionMessageBody() {

    Map<String, Object> map = new HashMap<>();

    map.put(COMMON_COUNTRY_CODE.name(), "test 123");
    map.put(BILLING_AMOUNT.name(), ONE);
    map.put(CARD_ACCEPTOR_ID.name(), TestConstant.BANKNET_REFERENCE_NUMBER);
    map.put(TRANSACTION_DATE.name(), Instant.parse("2019-11-23T10:12:35Z"));
    map.put(MERCHANT_CATEGORY_CODE.name(), "Category Code Test");
    map.put(MERCHANT_NUMBER.name(), "Test Id");
    map.put(BILLING_CURRENCY_CODE.name(), Currency.getInstance(Locale.UK)); //
    map.put(TRANSACTION_AMOUNT.name(), TEN);
    map.put(BANKNET_REFERENCE_NUMBER.name(), TestConstant.BANKNET_REFERENCE_NUMBER);

    return map;
  }

  private Map<String, Object> createTransactionAdditionalInfo() {
    Map<String, Object> map = new HashMap<>();
    map.put(DEBIT_CREDIT_INDICATOR.name(), "DEBIT");
    map.put(PAYMENT_METHOD_TYPE.name(), "UNKNOWN");
    map.put(SETTLEMENT_DATE.name(), Instant.parse("2019-11-23T10:12:35Z"));
    map.put(AUTHORISATION_CODE.name(), "Auth Code");
    map.put(SUBSCRIPTION_KEY.name(), SUBSCRIPTION_KEY_VALUE);

    return map;
  }

  private CardClearing buildCardClearing(Cain003 cain003) {
    return CardClearing.builder()
        .cardId(CARD_ID)
        .subscriptionKey(SUBSCRIPTION_KEY_VALUE)
        .partyKey(UUID.randomUUID())
        .productKey(UUID.randomUUID())
        .tenantKey(UUID.randomUUID().toString())
        .cain003(cain003)
        .build();
  }


}
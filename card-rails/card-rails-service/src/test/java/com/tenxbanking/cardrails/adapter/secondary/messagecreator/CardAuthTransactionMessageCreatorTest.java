package com.tenxbanking.cardrails.adapter.secondary.messagecreator;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH_VIA_ADVICE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.APPROVED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.RESERVE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.REVERSE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.CLEARING_CUSTOMER_CARD_CROSS_BORDER_CASH_WITHDRAWAL;
import static com.tenxbanking.cardrails.domain.TestConstant.BANKNET_REFERENCE_NUMBER;
import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_001;
import static com.tenxbanking.cardrails.domain.TestConstant.CAIN_OO2;
import static com.tenxbanking.cardrails.domain.TestConstant.CARD_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.CORRELATION_ID;
import static com.tenxbanking.cardrails.domain.TestConstant.PARTY_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.SUBSCRIPTION_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TENANT_KEY;
import static com.tenxbanking.cardrails.domain.TestConstant.TRANSACTION_ID;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.ADVICE;
import static com.tenxbanking.cardrails.domain.model.CardTransactionType.REVERSAL;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.PaymentMethodType;
import com.tenxbanking.cardrails.domain.model.transaction.AuthTransaction;
import com.tenxbanking.cardrails.domain.model.transaction.CardAdvice;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardAuthTransactionMessageCreatorTest {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  @Mock
  private GeneratorUtil generatorUtil;
  @Mock
  private PaymentTransactionCodeMapper paymentTransactionCodeMapper;
  @InjectMocks
  private CardAuthTransactionMessageCreator messageBuilder;
  private static final String REQUEST_ID = "123456";

  @BeforeEach
  void setupGeneratorUtil() {
    when(generatorUtil.generateRandomKey()).thenReturn(REQUEST_ID);
    when(paymentTransactionCodeMapper.map(any()))
        .thenReturn(CLEARING_CUSTOMER_CARD_CROSS_BORDER_CASH_WITHDRAWAL);
  }

  @Test
  void shouldCreateMessageWhenAdviceTrue() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .paymentMethodType(PaymentMethodType.UNKNOWN)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(createPaymentMessageAdditionalInfo());
    assertThat(paymentMessage.getMessage()).isEqualTo(createPaymentMessage(cain001));
  }

  @Test
  void whenReversalCardTransactionType_thenSetsTransactionStatusAsReverse() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(REVERSAL)
        .paymentMethodType(PaymentMethodType.UNKNOWN)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH.name());

    Map<String, Object> additionalInfo = createTransactionAdditionalInfo(
        REVERSE);

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(additionalInfo);
    assertThat(transactionMessage.getMessages()).size().isOne();

    Map<String, Object> paymentMessageAdditionalInfo = createPaymentMessageAdditionalInfo();
    paymentMessageAdditionalInfo.put("DEBIT_CREDIT_INDICATOR", "CREDIT");

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(paymentMessageAdditionalInfo);
    assertThat(paymentMessage.getMessage()).isEqualTo(createPaymentMessage(cain001));
  }

  @Test
  void shouldCreateUseTransactionStatusFailedWhenAuthResponseCode05AndIsAdvice() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .authResponseCode(AuthResponseCode._05)
        .paymentMethodType(PaymentMethodType.UNKNOWN)
        .build();

    AuthTransaction authTransaction = cardAdvice(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    Map<String, Object> transactionAdditionalInfo = createTransactionAdditionalInfo(
        APPROVED);

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualTo(transactionAdditionalInfo);
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(createPaymentMessageAdditionalInfo());
    assertThat(paymentMessage.getMessage()).isEqualTo(createPaymentMessage(cain001));
  }

  @Test
  void shouldCreateMessageWhenTransactionTypeTrue() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .paymentMethodType(PaymentMethodType.UNKNOWN)
        .processingCode("01")
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(createPaymentMessageAdditionalInfo());
    Map<String, Object> map = createPaymentMessage(cain001);
    map.put("TRANSACTION_TYPE", "01");

    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  void shouldCreateMessageWhenTransactionCardDataEntryModeTrue() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .processingCode("01")
        .pointOfServiceEntryMode("051")
        .paymentMethodType(PaymentMethodType.INTERNATIONAL_CASH_WITHDRAWAL)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "INTERNATIONAL_CASH_WITHDRAWAL");
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);
    Map<String, Object> map = createPaymentMessage(cain001);
    map.put("TRANSACTION_TYPE", "01");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  void shouldCreateMessageWhenIsPurchaseTrue() {

    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .processingCode("00")
        .pointOfServiceEntryMode("051")
        .pointOfServiceConditionCode("00")
        .paymentMethodType(PaymentMethodType.INTERNATIONAL_POS_CHIP_AND_PIN)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "INTERNATIONAL_POS_CHIP_AND_PIN");
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage(cain001);
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "00");
    map.put("TRANSACTION_TYPE", "00");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  void shouldCreateMessageWhenIsPosMagStripeTrue() {

    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .processingCode("00")
        .pointOfServiceEntryMode("02")
        .pointOfServiceConditionCode("00")
        .paymentMethodType(PaymentMethodType.POS_MAG_STRIPE)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "POS_MAG_STRIPE");
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage(cain001);
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "00");
    map.put("TRANSACTION_TYPE", "00");
    map.put("CARD_DATA_ENTRY_MODE", "MGST");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  void shouldCreateMessageWhenIsAtmMagStripeTrue() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .processingCode("01")
        .pointOfServiceEntryMode("02")
        .pointOfServiceConditionCode("00")
        .paymentMethodType(PaymentMethodType.ATM_MAG_STRIPE)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "ATM_MAG_STRIPE");
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage(cain001);
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "00");
    map.put("TRANSACTION_TYPE", "01");
    map.put("CARD_DATA_ENTRY_MODE", "MGST");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  void shouldCreateMessageWhenIsMailTelephoneOrderTrue() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .pointOfServiceEntryMode("01")
        .pointOfServiceConditionCode("01")
        .paymentMethodType(PaymentMethodType.MAIL_TELEPHONE_ORDER)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "MAIL_TELEPHONE_ORDER");

    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage(cain001);
    map.put("CARD_DATA_ENTRY_MODE", "PHYS");
    map.put("CARDHOLDER_PRESENT", false);
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  void shouldCreateMessageWhenIsOnlineTrue() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .pointOfServiceEntryMode("81")
        .pointOfServiceConditionCode("08")
        .paymentMethodType(PaymentMethodType.ONLINE)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "ONLINE");

    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage(cain001);
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "08");
    map.put("CARD_DATA_ENTRY_MODE", "PHYS");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  void shouldCreateMessageWhenIsContactlessTrue() {
    Cain001 cain001 = CAIN_001
        .toBuilder()
        .cardTransactionType(ADVICE)
        .processingCode("00")
        .pointOfServiceEntryMode("07")
        .paymentMethodType(PaymentMethodType.CONTACTLESS)
        .build();

    AuthTransaction authTransaction = cardAuth(cain001);
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH_VIA_ADVICE.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualToComparingFieldByField(createTransactionAdditionalInfo(APPROVED));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());

    Map<String, Object> additionalInfo = createPaymentMessageAdditionalInfo();
    additionalInfo.put("PAYMENT_METHOD_TYPE", "CONTACTLESS");

    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(additionalInfo);

    Map<String, Object> map = createPaymentMessage(cain001);
    map.put("CARDHOLDER_PRESENT", true);
    map.put("TRANSACTION_TYPE", "00");
    map.put("CARD_DATA_ENTRY_MODE", "CICC");
    assertThat(paymentMessage.getMessage()).isEqualTo(map);
  }

  @Test
  void shouldCreateMessageWhenAdviceFalse() {

    AuthTransaction authTransaction = cardAuth(
        CAIN_001.toBuilder().paymentMethodType(PaymentMethodType.UNKNOWN).build());
    TransactionMessage transactionMessage = messageBuilder.create(authTransaction);

    assertThat(transactionMessage.getHeader()).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(CARD_AUTH.name());

    assertThat(transactionMessage.getAdditionalInfo())
        .isEqualTo(createTransactionAdditionalInfo(RESERVE));
    assertThat(transactionMessage.getMessages()).size().isOne();

    PaymentMessage paymentMessage = transactionMessage.getMessages().get(0);
    assertThat(paymentMessage.getType()).isEqualTo(CAIN001.name());
    assertThat(paymentMessage.getAdditionalInfo()).isEqualTo(createPaymentMessageAdditionalInfo());
    assertThat(paymentMessage.getMessage()).isEqualTo(createPaymentMessage(CAIN_001));
  }


  private AuthTransaction cardAdvice(Cain001 cain001) {
    return CardAdvice
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .cain002(CAIN_OO2)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .productKey(PRODUCT_KEY)
        .partyKey(PARTY_KEY)
        .tenantKey(TENANT_KEY)
        .build();
  }

  private AuthTransaction cardAuth(Cain001 cain001) {
    return CardAuth
        .builder()
        .cardId(CARD_ID)
        .cain001(cain001)
        .cain002(CAIN_OO2)
        .subscriptionKey(SUBSCRIPTION_KEY)
        .productKey(PRODUCT_KEY)
        .partyKey(PARTY_KEY)
        .tenantKey(TENANT_KEY)
        .build();
  }

  private Map<String, Object> createPaymentMessage(Cain001 cain001) {
    Map<String, Object> map = new HashMap<>();
    map.put("AMOUNT", "10");
    map.put("BANKNET_REFERENCE_NUMBER", BANKNET_REFERENCE_NUMBER);
    map.put("CARDHOLDER_PRESENT", true);
    map.put("CARD_ACCEPTOR_CONDITION_CODE", "01");
    map.put("CARD_DATA_ENTRY_MODE", null);
    map.put("CARD_TOKEN", "aCardId");
    map.put("EXCHANGE_RATE", ONE);
    map.put("INITIATOR_PARTY_ID", "networkId");
    map.put("MERCHANT_CATEGORY_CODE", "merchantCatCode");
    map.put("TOTAL_AMOUNT", "1");
    map.put("TRANSACTION_AMOUNT_QUALIFIER", "ACTL");
    map.put("TRANSACTION_CURRENCY_CODE", "GBP");
    map.put("TRANSACTION_DATE",
        DATE_TIME_FORMATTER.format(cain001.getTransactionDate().atZone(ZoneId.of("Z"))));
    map.put("TRANSACTION_TIME",
        cain001.getTransactionDate().atZone(ZoneId.of("Z")).toLocalTime().toString());
    map.put("TRANSACTION_TYPE", "processingCode");
    map.put("VALUE_DATE",
        DATE_TIME_FORMATTER.format(cain001.getTransactionDate().atZone(ZoneId.of("Z"))));
    return map;
  }

  private Map<String, Object> createPaymentMessageAdditionalInfo() {
    Map<String, Object> map = new HashMap<>();
    map.put("DEBIT_CREDIT_INDICATOR", "DEBIT");
    map.put("PARTY_KEY", PARTY_KEY.toString());
    map.put("PAYMENT_METHOD_TYPE", "UNKNOWN");
    map.put("PRODUCT_KEY", PRODUCT_KEY.toString());
    map.put("SUBSCRIPTION_KEY", SUBSCRIPTION_KEY.toString());
    map.put("TRANSACTION_CODE", "PMNT.CCRD.XBCL");
    map.put("TRANSACTION_ID", TRANSACTION_ID.toString());
    return map;
  }

  private Map<String, Object> createTransactionAdditionalInfo(
      TransactionStatusValueEnum valueEnum) {
    Map<String, Object> map = new HashMap<>();
    map.put("REQUEST_ID", "123456");
    map.put("TENANT_PARTY_KEY", "10000");
    map.put("TRANSACTION_STATUS", valueEnum.name());
    map.put("TRANSACTION_CORRELATION_ID", CORRELATION_ID.toString());
    return map;
  }

}
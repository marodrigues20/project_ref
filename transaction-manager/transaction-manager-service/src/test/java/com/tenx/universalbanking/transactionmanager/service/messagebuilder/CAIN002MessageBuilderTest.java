package com.tenx.universalbanking.transactionmanager.service.messagebuilder;

import static com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes.INSUFFICIENT_BALANCE_REASON;
import static com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes.NO_CUSTOMER_INFO_IN_SM2;
import static com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes.PAYMENT_RESERVE_FAILED_CODE;
import static com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes.RULES_FAILED_CODE;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.CARD_ACCEPTOR_CITY;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.CARD_ACCEPTOR_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.CARD_ACCEPTOR_POST_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.INITIATOR_PARTY_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TOTAL_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_RESPONSE_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_RESPONSE_REASON_CODE;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Cain002Enum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import java.io.IOException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CAIN002MessageBuilderTest {

  @InjectMocks
  private CAIN002MessageBuilder cain002MessageBuilder;

  @Mock
  private GeneratorUtil generatorUtil;

  private static final String TRANSACTION_TYPE = "CARD_AUTH";
  private static final String PAYMENT_TYPE = "CAIN002";
  private static final String SUCCESS = "SUCCESS";
  private static final String FAILURE = "FAILURE";
  private static final String TEST_AMOUNT = "123";
  private static final String TEST_CURRENCY_CODE = "GBP";
  private static final String TEST_INITIATOR_PARTY_ID = "101";
  private static final String TEST_MERCHANT_CATEGORY_CODE = "5411";
  private static final String TEST_TRANSACTION_DATE = "2018-05-11T13:14:30.893Z";
  private static final String TRANSACTION_RESPONSE_CODE_SUCCESS = "APPR";
  private static final String PM_TRANSACTION_TYPE = "CRDP";
  private static final String TRANSACTION_AMOUNT_QUALIFIER = "ACTL";
  private static final String CARD_DATA_ENTRY_MODE = "CICC";
  private static final Boolean CARDHOLDER_PRESENT = true;
  private static final String CARD_ACCEPTOR_NAME = "ASDA";
  private static final int AUTH_CODE = 123456;

  private static final String ACCEPTOR_CITY = "HOLLY SPRINGS";
  private static final String ACCEPTOR_POST_CODE = "HA02TN";
  private static final String ACCEPTOR_COUNTRY_CODE = "GBP";


  private FileReaderUtil fileReader;
  private TransactionMessage message;

  @Before
  public void init() throws IOException {
    final String CAIN001_FILE = "message/cain001TransactionMesage.json";
    fileReader = new FileReaderUtil();
    message = createTransactionMessage();
    when(generatorUtil.generate6DigitAuthCode()).thenReturn(AUTH_CODE);
  }

  @Test
  public void shouldBuildHeaderType() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    assertThat(response.getHeader().getType()).isEqualTo(TRANSACTION_TYPE);
  }

  @Test
  public void shouldSetTransactionStatus() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    assertThat(response.getAdditionalInfo().get(TRANSACTION_STATUS.name()))
        .isEqualTo(TransactionStatusValueEnum.SUCCESS);
  }

  @Test
  public void shouldCreateCAIN002() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    assertThat(response.getMessages().get(0).getType()).isEqualTo(PAYMENT_TYPE);
  }

  @Test
  public void shouldCreateCAIN002ForDuplicateRequest() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message);
    assertThat(response.getMessages().get(0).getType()).isEqualTo(PAYMENT_TYPE);
  }

  @Test
  public void shouldSetProperty_AMOUNT() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(AMOUNT.name())).isEqualTo(TEST_AMOUNT);

  }

  @Test
  public void shouldSetProperty_MERCHANT_CATEGORY_CODE() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(MERCHANT_CATEGORY_CODE.name())).isEqualTo(TEST_MERCHANT_CATEGORY_CODE);

  }

  @Test
  public void shouldSetProperty_CARD_ACCEPTOR_COUNTRY_CODE() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(CARD_ACCEPTOR_COUNTRY_CODE.name())).isEqualTo(ACCEPTOR_COUNTRY_CODE);

  }

  @Test
  public void shouldSetProperty_CARD_ACCEPTOR_POST_CODE() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(CARD_ACCEPTOR_POST_CODE.name())).isEqualTo(ACCEPTOR_POST_CODE);

  }

  @Test
  public void shouldSetProperty_CARD_ACCEPTOR_CITY() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(CARD_ACCEPTOR_CITY.name())).isEqualTo(ACCEPTOR_CITY);

  }


  @Test
  public void shouldSetProperty_CURRENCY_CODE() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(TRANSACTION_CURRENCY_CODE.name())).isEqualTo(TEST_CURRENCY_CODE);

  }



  @Test
  public void shouldSetProperty_TRANSACTION_DATE() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(TRANSACTION_DATE.name())).isEqualTo(TEST_TRANSACTION_DATE);

  }

  @Test
  public void shouldSetProperty_INITIATOR_PARTY_ID() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(INITIATOR_PARTY_ID.name())).isEqualTo(TEST_INITIATOR_PARTY_ID);

  }

  @Test
  public void shouldSetProperty_TRANSACTION_RESPONSE_CODE() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(TRANSACTION_RESPONSE_CODE.name())).
        isEqualTo(TRANSACTION_RESPONSE_CODE_SUCCESS);

  }

  @Test
  public void shouldSetProperty_TOTAL_AMOUNT() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(TOTAL_AMOUNT.name())).isEqualTo(TEST_AMOUNT);

  }

  @Test
  public void shouldSetProperty_TRANSACTION_TYPE() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(Cain002Enum.TRANSACTION_TYPE.name())).isEqualTo(PM_TRANSACTION_TYPE);

  }

  @Test
  public void shouldSetProperty_TRANSACTION_AMOUNT_QUALIFIER() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(Cain002Enum.TRANSACTION_AMOUNT_QUALIFIER.name()))
        .isEqualTo(TRANSACTION_AMOUNT_QUALIFIER);

  }

  @Test
  public void shouldSetProperty_CARD_DATA_ENTRY_MODE() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(Cain002Enum.CARD_DATA_ENTRY_MODE.name())).isEqualTo(CARD_DATA_ENTRY_MODE);

  }

  @Test
  public void shouldSetProperty_CARDHOLDER_PRESENT() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(Cain002Enum.CARDHOLDER_PRESENT.name())).isEqualTo(CARDHOLDER_PRESENT);
  }

  @Test
  public void shouldSetProperty_CARD_ACCEPTOR_NAME() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(Cain002Enum.CARD_ACCEPTOR_NAME.name())).isEqualTo(CARD_ACCEPTOR_NAME);
  }


  @Test
  public void shouldSetAdditionalInfoForPaymentMessage() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> paymentMap = response.getMessages().get(0).getAdditionalInfo();
    assertThat(paymentMap).containsAllEntriesOf(message.getMessages().get(0).getAdditionalInfo());
  }

  @Test
  public void shouldSetAdditionalInfoForTransactionMessage() {
    com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage pdfResponse
        = new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage();
    pdfResponse.setAdditionalInfo(message.getAdditionalInfo());
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            pdfResponse));
    Map<String, Object> paymentMap = response.getAdditionalInfo();
    assertThat(paymentMap).containsAllEntriesOf(message.getAdditionalInfo());
  }

  @Test
  public void shouldSetTransactionStatusForTransactionMessage() {
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(),
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> paymentMap = response.getAdditionalInfo();
    assertThat(paymentMap).containsKey(TRANSACTION_STATUS.name());
  }

  @Test
  public void shouldSetProperty_TRANSACTION_RESPONSE_REASON_CODE_05() {
    PaymentDecisionReasonDTO pdfReasonDTO = new PaymentDecisionReasonDTO();
    pdfReasonDTO.setCode(RULES_FAILED_CODE);
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(FAILURE, pdfReasonDTO,
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(TRANSACTION_RESPONSE_REASON_CODE.name())).isEqualTo(NO_CUSTOMER_INFO_IN_SM2);
  }

  @Test
  public void shouldSetProperty_TRANSACTION_RESPONSE_REASON_CODE_FDNI() {
    PaymentDecisionReasonDTO pdfReasonDTO = new PaymentDecisionReasonDTO();
    pdfReasonDTO.setCode(PAYMENT_RESERVE_FAILED_CODE);
    TransactionMessage response = cain002MessageBuilder
        .getCain002Response(message, createPDFResponse(FAILURE, pdfReasonDTO,
            new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage()));
    Map<String, Object> map = response.getMessages().get(0).getMessage();
    assertThat(map.get(TRANSACTION_RESPONSE_REASON_CODE.name()))
        .isEqualTo(INSUFFICIENT_BALANCE_REASON);
  }

  @Test
  public void shouldSetPropertyPostedBalanceFromSubTransactionMessage() {
    com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage subTransactionMessage
        = new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage();
    subTransactionMessage.setAdditionalInfo(singletonMap("POSTED_BALANCE", 100));
    TransactionMessage response = cain002MessageBuilder.getCain002Response(message,
        createPDFResponse(SUCCESS, new PaymentDecisionReasonDTO(), subTransactionMessage));
    assertEquals(100, response.getAdditionalInfo().get("POSTED_BALANCE"));
  }

  private PaymentDecisionTransactionResponse createPDFResponse(String status,
      PaymentDecisionReasonDTO reasonDTo,
      com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage transactionMessage) {
    PaymentDecisionTransactionResponse response = new PaymentDecisionTransactionResponse();
    response.setDecisionResponse(status);
    response.setDecisionReason(reasonDTo);
    response.setTransactionMessage(transactionMessage);
    return response;
  }

  private TransactionMessage createTransactionMessage() throws IOException {
    return stringToJson(
        fileReader.getFileContent("message/cain001TransactionMesage.json"),
        TransactionMessage.class);
  }

}

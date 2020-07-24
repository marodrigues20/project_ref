package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.BILLING_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.COMMON_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.AUTH_MATCHING_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CLEARING;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.POSTED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN003;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN005;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.CLEARING_CUSTOMER_CARD_CROSS_BORDER_CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.CLEARING_CUSTOMER_CARD_DOMESTIC_PURCHASE_TRANSACTION;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.CLEARING_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.entity.AuthorisationId;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.factory.PaymentMessageServiceFactory;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.rest.responses.SettlementResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.validation.MessageValidator;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Cain001Enum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BatchSettlementProcessorTest {

  @Mock
  private PaymentMessageServiceFactory paymentMessageServiceFactory;

  @Mock
  private MessageValidator messageValidator;

  @Mock
  private MessageServiceProcessorHelper processorHelper;

  @Mock
  private MessageSender messageSender;

  @Mock
  private CAIN003Processor cain003Processor;

  @Mock
  private PaymentAuthorisations paymentAuthorisationsRepository;

  @Mock
  private HttpServletRequest request;

  @Mock
  private GeneratorUtil generatorUtil;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @InjectMocks
  private BatchSettlementProcessor batchSettlementProcessor;

  private TransactionMessage message;
  private SettlementResponse settlementResponse;
  private final String TRANSACTIONID = "12345";
  private final String AUTHORISATIONCODE = "12345";
  private final String CORRELATIONID = "6789";
  private final String SUBSCRIPTIONKEY = "12345";
  private Authorisations authorisations;
  private Authorisations authorisations1;
  private static final String REQUEST_ID = "ecfc6c00-3cab-11e8-98d3-7f99b9a03939";

  @Before
  public void setUp() {
    message = createTransactionMessage("6010", "GBR");
    settlementResponse = new SettlementResponse(SUCCESS.name());
    authorisations = new Authorisations();
    authorisations.setCorrelationId(CORRELATIONID);
    authorisations.setTransactionId(TRANSACTIONID);
    AuthorisationId id = new AuthorisationId();
    id.setAuthorisationCode(AUTHORISATIONCODE);
    authorisations.setId(id);
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
    when(generatorUtil.generateRandomKey()).thenReturn(REQUEST_ID);
  }

  @Test
  public void shouldValidateMessage() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    verify(messageValidator).validateAnyMessage(message, CAIN003, CAIN005);
  }

  @Test
  public void shouldGetAppropriatePaymentMessage() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    verify(paymentMessageServiceFactory).getPaymentMessageService(message.getMessages().get(0));
  }

  @Test
  public void shouldGetTransactionIds() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    verify(cain003Processor).getAuthorisations(message);
  }

  @Test
  public void shouldGenerateTransactionIdCorrelationId() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    batchSettlementProcessor.process(message, request);
    verify(processorHelper).generateTransactionAndCorrelationIds(message);
  }

  @Test
  public void shouldSetAuthMatchFlagToFalse() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    batchSettlementProcessor.process(message, request);
    assertThat(message.getAdditionalInfo().get(AUTH_MATCHING_STATUS.name())).isEqualTo(false);
  }

  @Test
  public void shouldSetAuthMatchFlagTrue() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    assertThat(message.getAdditionalInfo().get(AUTH_MATCHING_STATUS.name())).isEqualTo(true);
  }

  @Test
  public void shouldSendMessageToKafka() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    verify(messageSender).sendPaymentMessage(SUBSCRIPTIONKEY, message);
  }

  @Test
  public void shouldReturnSuccessResponse() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    SettlementResponse response = batchSettlementProcessor.process(message, request);
    assertThat(response.getSettlementStatus()).isEqualTo(SUCCESS.name());
  }

  @Test
  public void shouldUpdateAuthorisationsWhenMatchFound() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    authorisations.setMatched(true);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    verify(paymentAuthorisationsRepository).save(authorisations);
  }

  @Test
  public void shouldSetAuthorisationCode() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    authorisations.setMatched(true);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    assertThat(AUTHORISATIONCODE)
        .isEqualTo(message.getMessages().get(0).getMessage().get(AUTHORISATION_CODE.name()));
  }

  @Test
  public void shouldSetTransactionId() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    authorisations.setMatched(true);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    assertThat(TRANSACTIONID)
        .isEqualTo(message.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_ID.name()));
  }

  @Test
  public void shouldSetCorrelationId() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    authorisations.setMatched(true);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations);
    batchSettlementProcessor.process(message, request);
    assertThat(CORRELATIONID)
        .isEqualTo(message.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()));
  }

  @Test
  public void shouldNotUpdateAuthorisationsWhenMatchFound() {
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    batchSettlementProcessor.process(message, request);
    verify(paymentAuthorisationsRepository, times(0)).save(authorisations);
  }

  @Test
  public void shouldSetTransactionCode() {
    message = createTransactionMessage("6010", "GBR");
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations1);
    batchSettlementProcessor.process(message, request);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
    verify(messageSender).sendPaymentMessage(captorStr.capture(), captor.capture());
    TransactionMessage result = captor.getValue();
    assertThat(result.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name()))
        .isEqualTo(CLEARING_CUSTOMER_CARD_TRANSACTION_CASH_WITHDRAWAL.getValue());
  }

  @Test
  public void shouldSetTransactionCodeCrossBorder() {
    message = createTransactionMessage("6011", "IND");
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations1);
    batchSettlementProcessor.process(message, request);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
    verify(messageSender).sendPaymentMessage(captorStr.capture(), captor.capture());
    TransactionMessage result = captor.getValue();
    assertThat(result.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name()))
        .isEqualTo(CLEARING_CUSTOMER_CARD_CROSS_BORDER_CASH_WITHDRAWAL.getValue());
  }

  @Test
  public void shouldSetTransactionPurchase() {
    message = createTransactionMessage("5411", "IND");
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations1);
    batchSettlementProcessor.process(message, request);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
    verify(messageSender).sendPaymentMessage(captorStr.capture(), captor.capture());
    TransactionMessage result = captor.getValue();
    assertThat(result.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_CODE.name()))
        .isEqualTo(CLEARING_CUSTOMER_CARD_DOMESTIC_PURCHASE_TRANSACTION.getValue());
  }

  @Test
  public void shouldSetTransactionStatus() {
    message = createTransactionMessage("5411", "IND");
    when(paymentMessageServiceFactory.getPaymentMessageService(message.getMessages().get(0)))
        .thenReturn(cain003Processor);
    when(cain003Processor.getAuthorisations(message))
        .thenReturn(authorisations1);
    batchSettlementProcessor.process(message, request);
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
    verify(messageSender).sendPaymentMessage(captorStr.capture(), captor.capture());
    TransactionMessage result = captor.getValue();
    assertThat(result.getAdditionalInfo().get(TRANSACTION_STATUS.name()))
        .isEqualTo(POSTED.name());
  }

  private TransactionMessage createTransactionMessage(String mCC,
      String commonCountry) {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.getHeader().setType(CLEARING.name());
    Map<String, Object> transactionAddnlInfo = new HashMap<>();
    transactionAddnlInfo.put(TRANSACTION_CORRELATION_ID.name(), "788-troqpt-000888");
    PaymentMessage paymentMessage = new PaymentMessage();
    Map<String, Object> paymentAddnlInfo = new HashMap<>();
    paymentMessage.setType(PaymentMessageTypeEnum.CAIN003.name());
    Map<String, Object> message = new HashMap();
    message.put(Cain001Enum.TRANSACTION_DATE.name(), "2018-08-20");
    message.put(BANKNET_REFERENCE_NUMBER.name(), "1000");
    message.put(MERCHANT_CATEGORY_CODE.name(), mCC);
    message.put(TRANSACTION_AMOUNT.name(), BigDecimal.valueOf(1000L));
    message.put(CARD_ACCEPTOR_COUNTRY_CODE.name(), "GBR");
    message.put(COMMON_COUNTRY_CODE.name(), commonCountry);
    message.put(BILLING_CURRENCY_CODE.name(), "000");
    paymentAddnlInfo.put(SUBSCRIPTION_KEY.name(), "12345");
    paymentAddnlInfo.put(AUTHORISATION_CODE.name(), "12345");
    paymentMessage.setMessage(message);
    paymentMessage.setAdditionalInfo(paymentAddnlInfo);
    transactionMessage.setMessages(Collections.singletonList(paymentMessage));
    transactionMessage.setAdditionalInfo(transactionAddnlInfo);
    return transactionMessage;
  }
}
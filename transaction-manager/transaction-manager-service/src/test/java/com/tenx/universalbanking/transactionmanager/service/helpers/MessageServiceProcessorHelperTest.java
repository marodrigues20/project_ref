package com.tenx.universalbanking.transactionmanager.service.helpers;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.SUBSCRIPTION_KEY_NOTFOUND;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.SUBSCRIPTION_STATUS_NOTFOUND;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.FPS_IN;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.PAYMENT_METHOD_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PACS008;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PAIN001;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.api.PaymentDecisionControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes;
import com.tenx.universalbanking.transactionmanager.entity.AuthorisationId;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.enums.TransactionReason;
import com.tenx.universalbanking.transactionmanager.exception.TransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageCorrelationIdGenerator;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageTransactionIdGenerator;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class MessageServiceProcessorHelperTest {

  @Mock
  private TransactionMessageCorrelationIdGenerator correlationIdGenerator;

  @Mock
  private TransactionMessageTransactionIdGenerator transactionIdGenerator;

  @Mock
  private PaymentDecisionControllerApi paymentDecisionControllerApi;

  @Mock
  private PDFTransactionMessageMapper pdfMapper;

  @Mock
  private TMExceptionBuilder exceptionbuilder;

  @Mock
  private HttpServletRequest request;

  @Mock
  private MessageSender messageSender;

  @Mock
  private TracingHeadersFilter tracingHeadersFilter;

  @InjectMocks
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  private static final String requestHeaderKey = "x-b3-parentspanid";
  private static final String requestHeaderValue = "xb3123";

  private static final TransactionMessage ORIGINAL_MESSAGE = new TransactionMessage();

  private static final com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage
      CLIENT_MESSAGE =
      new com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage();

  @Test
  public void shouldAddGeneratedTransactionAndCorrelationIdsToTransactionMessageForNonReversal() {
    //given
    TransactionMessage transactionMessage = createPac008TransactionMessageWithSubscriptionkey();

    //when
    messageServiceProcessorHelper.generateTransactionAndCorrelationIds(transactionMessage);
    //then
    verify(correlationIdGenerator).addCorrelationId(transactionMessage);
    verify(transactionIdGenerator).addTransactionId(transactionMessage.getMessages().get(0));
  }

  @Test
  public void shouldAddGeneratedTransactionAndCorrelationIdsToTransactionMessageForReversal() {
    //given
    TransactionMessage transactionMessage = createPac008TransactionMessageWithSubscriptionkey();
    Authorisations authorisations = buildAuthorisation();

    //when
    messageServiceProcessorHelper
        .generateTransactionAndCorrelationIds(transactionMessage, true, authorisations);
    //then
    verify(correlationIdGenerator).addCorrelationId(transactionMessage, "1234");
    verify(transactionIdGenerator)
        .addTransactionId(transactionMessage.getMessages().get(0), "1234");
  }

  @Test
  public void shouldAddCorrelationIdsToTransactionMessage() {
    //given
    TransactionMessage transactionMessage = createPac008TransactionMessageWithSubscriptionkey();

    //when
    messageServiceProcessorHelper.generateCorrelationId(transactionMessage);
    //then
    verify(correlationIdGenerator).addCorrelationId(transactionMessage);
  }


  @Test
  public void shouldAddTransactionStatusToTransactionMessage() {
    //given
    TransactionMessage transactionMessage = createCain001TransactionMessage();

    //when
    messageServiceProcessorHelper
        .addTransactionStatus(transactionMessage, TransactionStatusValueEnum.RESERVE);
    //then
    assertTrue(transactionMessage.getAdditionalInfo().containsKey("TRANSACTION_STATUS"));
  }


  @Test
  public void shouldGetPaymentDecisionTransactionResponseWhenCallPaymentDecisionService() {
    TransactionMessage transactionMesssgae = createTransactionMessage();
    com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage
        clientTransactionMessage = createClientTransactionMessage();
    givenPaymentDecisionIs(PaymentDecisionResponse.SUCCESS.name());
    mapToClientTransactionMessage(transactionMesssgae);
    messageServiceProcessorHelper.callPaymentDecisionService(transactionMesssgae);
    verify(paymentDecisionControllerApi).makePaymentDecision(clientTransactionMessage);
  }

  @Test
  public void shouldGetTrueWhenCallIsPaymentDecisionSuccessWithPaymentDecisionSuccessResponse() {
    PaymentDecisionTransactionResponse response =
        getPaymentDecisionTransactionSuccessResponse(PaymentDecisionResponse.SUCCESS.name());
    boolean status = messageServiceProcessorHelper.isPaymentDecisionSuccess(response);
    assertTrue(status);
  }

  @Test
  public void shouldGetFalseWhenCallIsPaymentDecisionSuccessWithPaymentDecisionFailedResponse() {
    PaymentDecisionTransactionResponse response =
        getPaymentDecisionTransactionFalseResponse(PaymentDecisionResponse.FAILED.name());
    boolean status = messageServiceProcessorHelper.isPaymentDecisionSuccess(response);
    assertFalse(status);
  }

  @Test
  public void shouldNotGetNullWhenCallGetPayeeSubscriptionKey() {
    TransactionMessage transactionMessage = createPac008TransactionMessageWithSubscriptionkey();
    String subscriptionKey = messageServiceProcessorHelper
        .getSubscriptionKey(transactionMessage, PACS008);
    assertNotNull(subscriptionKey);
  }

  @Test
  public void shouldThrowLionExceptionWhenCallGetGetPayeeSubscriptionKeyWithoutSubscriptionkey() {
    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionManagerException transactionManagerException = new TransactionManagerException();
    transactionManagerException.setErrorCode(SUBSCRIPTION_KEY_NOTFOUND.getStatusCode());
    transactionManagerException.setErrorMessage(SUBSCRIPTION_KEY_NOTFOUND.getMessage());
    transactionManagerException.setFieldName(null);
    when(exceptionbuilder.buildTransactionException(any()))
        .thenReturn(transactionManagerException);
    TransactionManagerException actual = assertThrows(TransactionManagerException.class, () -> {
      messageServiceProcessorHelper
          .getSubscriptionKey(transactionMessage, PACS008);
    });
    assertEquals(transactionManagerException, actual);
  }

  @Test
  public void shouldThrowExceptionWhenErrorOccurredWhileFetchingSubscriptionKeyException() {
    TransactionMessage transactionMessage = createPac008TransactionMessageWithoutSubscriptionkey();
    TransactionManagerException transactionManagerException = new TransactionManagerException();
    transactionManagerException.setErrorCode(SUBSCRIPTION_KEY_NOTFOUND.getStatusCode());
    transactionManagerException.setErrorMessage(SUBSCRIPTION_KEY_NOTFOUND.getMessage());
    transactionManagerException.setFieldName(null);
    when(exceptionbuilder.buildTransactionException(any()))
        .thenReturn(transactionManagerException);
    TransactionManagerException actual = assertThrows(TransactionManagerException.class, () -> {
      messageServiceProcessorHelper
          .getSubscriptionKey(transactionMessage, PACS008);
    });
    assertEquals(transactionManagerException, actual);
  }

  @Test
  public void shouldThrowLionExceptionWhenCallGetGetPayeeSubscriptionKeyWithInvalidPac008Message() {
    TransactionMessage transactionMessage =
        createPac008TransactionMessageWithInvalidPac008Message();
    TransactionManagerException transactionManagerException = new TransactionManagerException();
    transactionManagerException.setErrorCode(SUBSCRIPTION_KEY_NOTFOUND.getStatusCode());
    transactionManagerException.setErrorMessage(SUBSCRIPTION_KEY_NOTFOUND.getMessage());
    transactionManagerException.setFieldName(null);
    when(exceptionbuilder.buildTransactionException(any()))
        .thenReturn(transactionManagerException);
    TransactionManagerException actual = assertThrows(TransactionManagerException.class, () -> {
      messageServiceProcessorHelper
          .getSubscriptionKey(transactionMessage, PACS008);
    });
    assertEquals(transactionManagerException, actual);
  }

  @Test
  public void testGetSubscriptionStatus_shouldNotGetNull_WhenValidSubscriptionStatusProvided() {
    TransactionMessage transactionMessage = createPac008TransactionMessageWithSubscriptionStatus();
    String subscriptionKey = messageServiceProcessorHelper
        .getSubscriptionStatus(transactionMessage, PACS008);
    assertNotNull(subscriptionKey);
  }

  @Test
  public void testGetSubscriptionStatus_shouldThrowException_WhenSubscriptionStatusNotProvided() {
    TransactionMessage transactionMessage = createPac008TransactionMessageWithoutSubscriptionkey();
    TransactionManagerException transactionManagerException = new TransactionManagerException();
    transactionManagerException.setErrorCode(SUBSCRIPTION_STATUS_NOTFOUND.getStatusCode());
    transactionManagerException.setErrorMessage(SUBSCRIPTION_STATUS_NOTFOUND.getMessage());
    transactionManagerException.setFieldName(null);
    when(exceptionbuilder.buildTransactionException(any()))
        .thenThrow(transactionManagerException);
    TransactionManagerException actual = assertThrows(TransactionManagerException.class, () -> {
      messageServiceProcessorHelper.getSubscriptionStatus(transactionMessage, PACS008);
    });
    assertEquals(transactionManagerException, actual);
  }

  @Test
  public void testGetSubscriptionStatus_shouldThrowException_WhenInvalidPACMessageProvided() {
    TransactionMessage transactionMessage = createPac008TransactionMessageWithInvalidPac008Message();
    TransactionManagerException transactionManagerException = new TransactionManagerException();
    transactionManagerException.setErrorCode(SUBSCRIPTION_STATUS_NOTFOUND.getStatusCode());
    transactionManagerException.setErrorMessage(SUBSCRIPTION_STATUS_NOTFOUND.getMessage());
    transactionManagerException.setFieldName(null);
    when(exceptionbuilder.buildTransactionException(any()))
        .thenThrow(transactionManagerException);
    TransactionManagerException actual = assertThrows(TransactionManagerException.class, () -> {
      messageServiceProcessorHelper.getSubscriptionStatus(transactionMessage, PACS008);
    });
    assertEquals(transactionManagerException, actual);
  }

  @Test
  public void shouldNotGenerateNullReasonPropertiesWhenCallGeneratePaymentDecisionErrorResponse() {
    ReasonDto reason = messageServiceProcessorHelper.getGenericFailureReason();
    assertNotNull(reason.getCode());
    assertNotNull(reason.getMessage());
  }

  @Test
  public void getSpecificFailureReasonTest() {
    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = new PaymentDecisionTransactionResponse();
    PaymentDecisionReasonDTO reasonDTO = new PaymentDecisionReasonDTO();
    reasonDTO.setCode(8002);
    paymentDecisionTransactionResponse.setDecisionReason(reasonDTO);
    ReasonDto reason = messageServiceProcessorHelper
        .getFailureReason(paymentDecisionTransactionResponse);
    assertEquals((Integer) TransactionManagerExceptionCodes.DECISION_INSUFFICIENT_FUNDS,
        reason.getCode());
  }

  @Test
  public void getGenericFailureReasonTest() {
    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = new PaymentDecisionTransactionResponse();
    PaymentDecisionReasonDTO reasonDTO = new PaymentDecisionReasonDTO();
    reasonDTO.setCode(8);
    paymentDecisionTransactionResponse.setDecisionReason(reasonDTO);
    ReasonDto reason = messageServiceProcessorHelper
        .getFailureReason(paymentDecisionTransactionResponse);
    assertEquals((Integer) TransactionReason.GENERIC_FAILURE.getFailureCode(), reason.getCode());
    assertEquals(TransactionReason.GENERIC_FAILURE.getFailureMessage(), reason.getMessage());
  }

  @Test
  public void sendToKafkaTest() {
    TransactionMessage transactionMessage = createPac008TransactionMessageWithSubscriptionStatus();
    doNothing().when(messageSender).sendPaymentMessage("", transactionMessage);
    messageServiceProcessorHelper.sendtoKafka(transactionMessage, PACS008);
    verify(messageSender).sendPaymentMessage("", transactionMessage);
  }

  @Test
  public void shouldAddPaymentMethodType() {
    TransactionMessage transactionMessage = getTransactionMessage(FPS_IN);
    messageServiceProcessorHelper.addPaymentMethodType(transactionMessage);

    Map<String, Object> additionalInfo = transactionMessage.getMessages().get(0)
        .getAdditionalInfo();
    assertTrue(additionalInfo.size() > 0);
    assertEquals(FPS_IN.name(), additionalInfo.get(PAYMENT_METHOD_TYPE.name()).toString());
  }

  @Test
  public void addTracingHeadersTest() {
    when(tracingHeadersFilter.filter(request))
        .thenReturn(Collections.singletonMap(requestHeaderKey, requestHeaderValue));

    TransactionMessage transactionMessage = getTransactionMessage(FPS_IN);

    messageServiceProcessorHelper.addTracingHeaders(transactionMessage, request);
    assertEquals(requestHeaderValue, transactionMessage.getAdditionalInfo().get(requestHeaderKey));
  }

  private Authorisations buildAuthorisation() {
    Authorisations auth = new Authorisations();

    AuthorisationId authid = new AuthorisationId();
    authid.setTransactionDate(LocalDate.now());
    authid.setAuthorisationCode("code");
    authid.setBankNetReferenceNumber("ref_number");

    auth.setId(authid);
    auth.setMcc("mcc");
    auth.setTransactionId("1234");
    auth.setCorrelationId("1234");
    auth.setTsysAccountId("tsyAccountId");
    auth.setMatched(true);
    auth.setExpired(false);
    auth.setTransactionType("REVERSAL");
    auth.setTransactionAmount(new BigDecimal("20"));
    return auth;
  }

  private TransactionMessage getTransactionMessage(TransactionMessageTypeEnum transactionType) {
    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(transactionType.name());
    PaymentMessage paymentMessage = new PaymentMessage();
    Map<String, Object> additionalInfoMap = new HashMap<>();
    paymentMessage.setAdditionalInfo(additionalInfoMap);
    transactionMessage.setHeader(header);
    transactionMessage.setMessages(singletonList(paymentMessage));
    return transactionMessage;
  }

  private PaymentDecisionTransactionResponse getPaymentDecisionTransactionSuccessResponse(
      String status) {
    PaymentDecisionTransactionResponse response = new PaymentDecisionTransactionResponse();
    response.setDecisionResponse(status);
    return response;
  }

  private PaymentDecisionTransactionResponse getPaymentDecisionTransactionFalseResponse(
      String status) {
    PaymentDecisionTransactionResponse response = new PaymentDecisionTransactionResponse();
    response.setDecisionResponse(status);
    return response;
  }

  private void givenPaymentDecisionIs(String status) {
    PaymentDecisionTransactionResponse response =
        getPaymentDecisionTransactionSuccessResponse(status);
    when(paymentDecisionControllerApi.makePaymentDecision(any())).thenReturn(response);
  }

  private void mapToClientTransactionMessage(TransactionMessage transactionMessage) {
    when(pdfMapper.toClientTransactionMessage(transactionMessage)).thenReturn(CLIENT_MESSAGE);
  }

  private TransactionMessage createTransactionMessage() {
    return ORIGINAL_MESSAGE;
  }

  private com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage createClientTransactionMessage() {
    return CLIENT_MESSAGE;
  }

  private static TransactionMessage createPac008TransactionMessageWithSubscriptionkey() {

    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put(
        PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY.name(),
        "27c5b516-5aea-540d-adec-59cb56c6f637");

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(PACS008.name());
    paymentMessage.setAdditionalInfo(additionalInfo);

    List<PaymentMessage> paymentMessageList = new ArrayList<>();
    paymentMessageList.add(paymentMessage);

    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setMessages(paymentMessageList);

    return transactionMessage;
  }


  private static TransactionMessage createPac008TransactionMessageWithoutSubscriptionkey() {

    Map<String, Object> additionalInfo = new HashMap<>();

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(PACS008.name());
    paymentMessage.setAdditionalInfo(additionalInfo);

    List<PaymentMessage> paymentMessageList = new ArrayList<>();
    paymentMessageList.add(paymentMessage);

    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setMessages(paymentMessageList);

    return transactionMessage;
  }

  private static TransactionMessage createPac008TransactionMessageWithSubscriptionStatus() {

    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put(
        PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_STATUS.name(),
        "ACTIVE");
    additionalInfo.put(
        PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY.name(),
        "");

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(PACS008.name());
    paymentMessage.setAdditionalInfo(additionalInfo);

    List<PaymentMessage> paymentMessageList = new ArrayList<>();
    paymentMessageList.add(paymentMessage);

    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setMessages(paymentMessageList);

    return transactionMessage;
  }

  private static TransactionMessage createPac008TransactionMessageWithInvalidPac008Message() {

    Map<String, Object> additionalInfo = new HashMap<>();

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(PAIN001.name());
    paymentMessage.setAdditionalInfo(additionalInfo);

    List<PaymentMessage> paymentMessageList = new ArrayList<>();
    paymentMessageList.add(paymentMessage);

    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setMessages(paymentMessageList);

    return transactionMessage;
  }

  private static TransactionMessage createCain001TransactionMessage() {

    Map<String, Object> additionalInfo = new HashMap<>();

    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN001.name());
    paymentMessage.setAdditionalInfo(additionalInfo);

    List<PaymentMessage> paymentMessageList = new ArrayList<>();
    paymentMessageList.add(paymentMessage);

    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setMessages(paymentMessageList);

    return transactionMessage;


  }
}

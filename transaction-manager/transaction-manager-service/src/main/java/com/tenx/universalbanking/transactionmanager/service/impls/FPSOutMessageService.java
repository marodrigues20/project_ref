package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionReason.GENERIC_FAILURE;
import static com.tenx.universalbanking.transactionmessage.enums.Pacs002Enum.OUTCOME;
import static com.tenx.universalbanking.transactionmessage.enums.Pain001AckEnum.RESPONSE_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Pain001AckEnum.RESPONSE_DESCRIPTION;
import static com.tenx.universalbanking.transactionmessage.enums.Pain002Enum.IS_RETURN;
import static com.tenx.universalbanking.transactionmessage.enums.Pain002Enum.PAYMENT_TYPE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.COMPLETE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.RETURN;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PAIN001_ACK;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PAIN002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_DEBIT;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.api.DomesticControllerApi;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.enums.PaymentType;
import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.FPSOutPain001RequestHandler;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.mapper.PPMTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@Service
public class FPSOutMessageService implements TransactionMessageService {

  private final Logger log = getLogger(getClass());

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private PaymentProcessResponseConverter responseConverter;

  @Autowired
  private LedgerManagerClient lmClient;

  @Autowired
  private FPSOutPain001RequestHandler handler;

  @Autowired
  private DomesticControllerApi domesticControllerApi;

  @Autowired
  private PPMTransactionMessageMapper ppmTransactionMessageMapper;

  @Autowired
  private TMExceptionBuilder exceptionBuilder;

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Autowired
  GeneratorUtil generatorUtil;

  @Override
  public TransactionMessageTypeEnum getType() {
    return TransactionMessageTypeEnum.FPS_OUT;
  }

  @Override
  public PaymentProcessResponse process(TransactionMessage transactionMessage,
      HttpServletRequest request) {
    PaymentProcessResponse processResponse = new PaymentProcessResponse();
    Map<String, PaymentMessage> paymentMessages = getPaymentInitiationMessageType(
        transactionMessage);

    if (paymentMessages.size() != 1) {
      log.error("UnExpected message received count: {}", paymentMessages.size());
      return getGenericFailureExceptionResponse();
    }

    if (paymentMessages.containsKey(PAIN001.name())) {
      try {
        TransactionMessage pain001ACkTxMsg = handler.process(transactionMessage,
            paymentMessages.get(PAIN001.name()));
        processResponse = processPaymentResponse(pain001ACkTxMsg, SUCCESS, request);

      } catch (HttpServerErrorException restException) {
        if (HttpStatus.GATEWAY_TIMEOUT.equals(restException.getStatusCode())) {
          log.error("Http Server error exception {}", restException);
          throw exceptionBuilder.buildFPSOutTransactionManagerException(
              INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getStatusCode(),
              INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } catch (RestClientException | InvalidTransactionMessageException ex) {
        log.error("Exception exception {}", ex);
        processResponse = getGenericFailureExceptionResponse();
      }

    } else if (paymentMessages.containsKey(PAIN002.name())) {
      try {
        if (isReturn(transactionMessage)) {
          processResponse = processPaymentResponse(transactionMessage, RETURN, request);
        } else {
          processResponse = processPaymentResponse(transactionMessage, COMPLETE, request);
        }
        if (!isFpsSipRequest(transactionMessage)) {
          domesticControllerApi.acceptPaymentConfirmation(
              ppmTransactionMessageMapper.toClientTransactionMessage(transactionMessage));
        }
      } catch (HttpServerErrorException restException) {
        if (HttpStatus.GATEWAY_TIMEOUT.equals(restException.getStatusCode())) {
          log.error("Http Server error exception {}", restException);
          throw exceptionBuilder.buildFPSOutTransactionManagerException(
              INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE.getStatusCode(),
              INTERNAL_SERVER_ERROR_PAIN002_LM_FAILURE.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } catch (RestClientException ex) {
        log.error("Rest Client Exception exception {}", ex);
        processResponse = getGenericFailureExceptionResponse();
      }
    }
    return processResponse;
  }

  private boolean isReturn(final TransactionMessage transactionMessage) {
    Map<String, Object> pain002Message = transactionMessage.getMessages().get(0).getMessage();
    return nonNull(pain002Message.get(IS_RETURN.name()))
        && Boolean.valueOf(pain002Message.get(IS_RETURN.name()).toString());
  }

  /**
   * Check whether transaction is SIP or not
   *
   * @return boolean
   */
  private boolean isFpsSipRequest(TransactionMessage transactionMessage) {
    return transactionMessage.getMessages().get(0).getMessage().get(PAYMENT_TYPE.name()).equals(
        PaymentType.SINGLE_IMMEDIATE_PAYMENT.getPaymentType());
  }

  /**
   * Handles PAIN001 ACK and PAIN002 Response
   */
  @SuppressWarnings("unchecked")
  private PaymentProcessResponse processPaymentResponse(TransactionMessage transactionMessage,
      TransactionStatusValueEnum txSuccessState, HttpServletRequest request) {

    PaymentMessage validPaymentMessage = transactionMessage.getMessages().get(0);
    Map<String, Object> additionalInfo = transactionMessage.getAdditionalInfo();
    PaymentProcessResponse processResponse = new PaymentProcessResponse();

    if (validPaymentMessage.getMessage().containsKey(OUTCOME.name())
        && validPaymentMessage.getMessage().get(OUTCOME.name()).equals(SUCCESS.name())
        && additionalInfo.containsKey(REQUEST_ID.name())
        && additionalInfo.containsKey(TRANSACTION_CORRELATION_ID.name())) {
      log.debug("acknowledgement outcome success");

      addTransactionStatus(transactionMessage, txSuccessState);
      processResponse.setPaymentStatus(PaymentDecisionResponse.SUCCESS.name());
    } else {
      Integer errorCode = validPaymentMessage.getMessage().containsKey(RESPONSE_CODE.name())
          ? Integer.parseInt(validPaymentMessage.getMessage().get(RESPONSE_CODE.name()).toString())
          : 0;
      String messageDescription =
          (String) validPaymentMessage.getMessage().get(RESPONSE_DESCRIPTION.name());
      processResponse = new PaymentProcessResponse(PaymentDecisionResponse.FAILED.name(),
          createReasonDto(errorCode, messageDescription));
      log.error(
          "acknowledgement outcome failure, Transaction Correlation Id : {}, Request Correlation Id: {}, Tenant Party Key: {}",
          transactionMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()),
          transactionMessage.getAdditionalInfo().get(REQUEST_CORRELATION_ID.name()),
          transactionMessage.getAdditionalInfo().get(TENANT_PARTY_KEY.name()));
      if (PAIN001_ACK.name().equals(transactionMessage.getMessages().get(0).getType())) {

        reconciliationHelper.saveReconciliationMessage(
            buildReconciliationMessage(transactionMessage, Event.EXT_REJECT_PSP));
      } else {
        reconciliationHelper.saveReconciliationMessage(
            buildReconciliationMessage(transactionMessage, Event.EXT_REJECT));
      }
      addTransactionStatus(transactionMessage, FAILED);
    }

    validPaymentMessage.getAdditionalInfo().put(TRANSACTION_CODE.name(),
        PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_DEBIT.getValue());

    String key = transactionMessage.getMessages().get(0).getAdditionalInfo()
        .get(PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY.name()).toString();

    messageServiceProcessorHelper.addTracingHeaders(transactionMessage, request);
    LedgerPostingResponse lmPostingResponse = lmClient.postTransactionToLedger(transactionMessage);

    if (lmPostingResponse.isPostingSuccess()) {
      messageSender.sendPaymentMessage(key, transactionMessage);
      if (!transactionMessage.getMessages().isEmpty()
          && transactionMessage.getMessages().get(0).getType() != null
          && PAIN002.name().equals(transactionMessage.getMessages().get(0).getType())) {
        reconciliationHelper.saveReconciliationMessage(
            buildReconciliationMessage(transactionMessage, Event.SUCCESS));
      }
    } else {
      log.error(
          "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
      processResponse = responseConverter
          .buildPaymentProcessResponse(lmPostingResponse, transactionMessage);
    }
    processResponse.setTransactionMessage(transactionMessage);
    return processResponse;
  }

  private Map<String, PaymentMessage> getPaymentInitiationMessageType(
      TransactionMessage transactionMessage) {
    Predicate<PaymentMessage> paymentTypeIsPain001OrPain002 = paymentMessage ->
        PAIN002.name().equals(paymentMessage.getType()) || PAIN001.name()
            .equals(paymentMessage.getType());

    return transactionMessage.getMessages()
        .stream()
        .filter(paymentTypeIsPain001OrPain002)
        .collect(Collectors.toMap(PaymentMessage::getType, paymentMsg -> paymentMsg));
  }


  private PaymentProcessResponse getGenericFailureExceptionResponse() {
    PaymentProcessResponse processResponse;
    processResponse = new PaymentProcessResponse(PaymentDecisionResponse.FAILED.name(),
        createReasonDto(GENERIC_FAILURE.getFailureCode(), GENERIC_FAILURE.getFailureMessage()));
    return processResponse;
  }

  private void addTransactionStatus(TransactionMessage transactionMessage,
      TransactionStatusValueEnum status) {
    transactionMessage.getAdditionalInfo().put(TRANSACTION_STATUS.name(), status.name());
  }

  private ReasonDto createReasonDto(Integer errorCode, String messageDescription) {
    return new ReasonDto(errorCode, messageDescription);
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage,
      Event event) {
    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(event).
        scope(TransactionMessageTypeEnum.FPS_OUT).
        serviceName(ServiceNames.TRANSACTION_MANAGER).build();
  }

}
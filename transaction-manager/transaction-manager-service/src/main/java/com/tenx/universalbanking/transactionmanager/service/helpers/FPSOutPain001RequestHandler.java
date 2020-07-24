package com.tenx.universalbanking.transactionmanager.service.helpers;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentType.FUTURE_DATED_PAYMENT;
import static com.tenx.universalbanking.transactionmanager.enums.PaymentType.SINGLE_IMMEDIATE_PAYMENT;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PDF_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVICE_UNAVAILABLE;
import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.Pain001Enum.CREDITOR_CLEARING_SYSTEM_IDENTIFICATION;
import static com.tenx.universalbanking.transactionmessage.enums.Pain001Enum.DEBTOR_CLEARING_SYSTEM_IDENTIFICATION;
import static com.tenx.universalbanking.transactionmessage.enums.Pain001Enum.PAYMENT_TYPE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_DEBIT;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.api.PaymentDecisionControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.api.DomesticPaymentControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.model.FpsOutPaymentResponse;
import com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageCorrelationIdGenerator;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageTransactionIdGenerator;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.service.mapper.PPTransactionMessageMapper;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.validationlib.response.Errors;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@Component
public class FPSOutPain001RequestHandler {

  private final Logger log = getLogger(getClass());

  private static final String GBDSC = "GBDSC";

  private final PaymentDecisionControllerApi paymentDecisionControllerApi;

  private final DomesticPaymentControllerApi domesticPaymentControllerApi;

  private final PDFTransactionMessageMapper pdfMapper;

  private final PPTransactionMessageMapper ppMapper;

  private final TransactionMessageCorrelationIdGenerator correlationIdGenerator;

  private final TransactionMessageTransactionIdGenerator transactionIdGenerator;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final TMExceptionBuilder exceptionBuilder;

  private final ReconciliationHelper reconciliationHelper;

  @Autowired
  public FPSOutPain001RequestHandler(PaymentDecisionControllerApi paymentDecisionControllerApi,
      DomesticPaymentControllerApi domesticPaymentControllerApi,
      PDFTransactionMessageMapper pdfMapper,
      PPTransactionMessageMapper ppMapper,
      TransactionMessageCorrelationIdGenerator correlationIdGenerator,
      TransactionMessageTransactionIdGenerator transactionIdGenerator,
      TMExceptionBuilder exceptionBuilder,
      ReconciliationHelper reconciliationHelper) {

    this.correlationIdGenerator=correlationIdGenerator;
    this.domesticPaymentControllerApi=domesticPaymentControllerApi;
    this.paymentDecisionControllerApi=paymentDecisionControllerApi;
    this.transactionIdGenerator=transactionIdGenerator;
    this.pdfMapper=pdfMapper;
    this.ppMapper=ppMapper;
    this.exceptionBuilder = exceptionBuilder;
    this.reconciliationHelper = reconciliationHelper;
  }

  public TransactionMessage process(TransactionMessage transactionMessage,
      PaymentMessage pain001Msg) {

    TransactionMessage fpsResponse;

    correlationIdGenerator.addCorrelationId(transactionMessage);
    transactionMessage.getMessages().forEach(transactionIdGenerator::addTransactionId);

    addTransactionStatus(transactionMessage);

    if (isPAIN001CriteriaMet(pain001Msg)) {
      pain001Msg.getAdditionalInfo().put(
          TRANSACTION_CODE.name(),
          PAYMENTS_ISSUED_REAL_TIME_CREDIT_TRANSFERS_FPS_DEBIT.getValue());
    } else {

      String msg = "Criteria not met for PAIN001 msg";
      log.error(msg+" with TRANSACTION_CORRELATION_ID: {}",
              transactionMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()));
      throw new InvalidTransactionMessageException(msg);
    }

    PaymentDecisionTransactionResponse paymentDecisionTxResponse =
                                                makePaymentDecision(transactionMessage);

    if (PaymentDecisionResponse.SUCCESS.equals(PaymentDecisionResponse
        .valueOf(paymentDecisionTxResponse.getDecisionResponse()))) {

      FpsOutPaymentResponse fpsOutPaymentResponse = makeFPSOutPayment(transactionMessage);

      fpsResponse = ppMapper.toTMTransactionMessage(fpsOutPaymentResponse.getTransactionMessage());

    } else {

      log.error("Downstream Service Name :{} , API Name : {}", "payment decision",
                                                                "payment-decision/v1/payment-decision");
      log.error("Input Transaction Message : {} ",appendTransactionMessageRequest(transactionMessage));
      if(null != paymentDecisionTxResponse.getDecisionReason()){
        log.error(" Payment Decision Response Code {}"
                + " Payment Decision Response Message {} :",
            paymentDecisionTxResponse.getDecisionReason().getCode(),
            paymentDecisionTxResponse.getDecisionReason().getMessage());
      }

      String msg = String.format("Payment decision is failed, Transaction Correlation Id : {}, Request Correlation Id: {}, Tenant Party Key: {}",
          transactionMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()),
          transactionMessage.getAdditionalInfo().get(REQUEST_CORRELATION_ID.name()),
          transactionMessage.getAdditionalInfo().get(TENANT_PARTY_KEY.name()));
      log.error(msg);
      reconciliationHelper.saveReconciliationMessage(
          buildReconciliationMessage(transactionMessage));
      throw exceptionBuilder.buildFromPdfResponse(paymentDecisionTxResponse);
    }
    return fpsResponse;
  }


  private PaymentDecisionTransactionResponse makePaymentDecision(
      TransactionMessage transactionMessage) {

    PaymentDecisionTransactionResponse paymentDecisionTransactionResponse = null;

    try {
      paymentDecisionTransactionResponse = paymentDecisionControllerApi
          .makePaymentDecision(pdfMapper.toClientTransactionMessage(transactionMessage));

    } catch (HttpServerErrorException restException) {
      if (restException.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
        throw exceptionBuilder
            .buildFPSOutTransactionManagerException(INTERNAL_SERVER_ERROR_PDF_FAILURE.getStatusCode(),
                INTERNAL_SERVER_ERROR_PDF_FAILURE.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (RestClientException e) {
      log.error("Payment decision is failed due to Exception {}", e);
      log.error("Downstream Service Name :{} , API Name : {}", "Payment Decision Framework",
          "payment-decision");
      log.error("Input Transaction Message : {} ",
          appendTransactionMessageRequest(transactionMessage));
      throw e;
    }
    return paymentDecisionTransactionResponse;
  }

  private FpsOutPaymentResponse makeFPSOutPayment(TransactionMessage transactionMessage) {

    String tenantKey = transactionMessage.getAdditionalInfo()
        .get(TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY.name()).toString();

    FpsOutPaymentResponse fpsOutPaymentResponse = null;
    try {
      return domesticPaymentControllerApi.processFpsOutPayment(ppMapper.toPPTransactionMessage(transactionMessage),tenantKey);

    } catch (HttpServerErrorException restException) {

      log.error("FPSout downstream failure: {}", restException);

      if (!restException.getResponseBodyAsString().isEmpty()) {
        try {
          Errors errors = objectMapper
              .readValue(restException.getResponseBodyAsString(), Errors.class);

          FPSOutTransactionManagerException fpsOutException = exceptionBuilder
              .buildFromDownstreamError(errors);

          if (!Objects.isNull(fpsOutException)) {
            throw fpsOutException;
          }
        } catch (IOException e) {
          //ignoring this exception as this is not a generated exception from Form3 Adapter
        }
      }

      if (restException.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
        log.error("Payment proxy call is failed due to Exception {}", restException);
        log.error("Downstream Service Name :{} , API Name : {}", "Payment Proxy",
            "Domestic payment FPSOut");
        log.error("Input Transaction Message : {} ",
            appendTransactionMessageRequest(transactionMessage));
        throw exceptionBuilder
            .buildFPSOutTransactionManagerException(INTERNAL_SERVICE_UNAVAILABLE.getStatusCode(),
                INTERNAL_SERVICE_UNAVAILABLE.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
      }
      throw restException;
    }
    catch (RestClientException ex) {

      log.error("Payment proxy call is failed due to Exception {}", ex);
      log.error("Downstream Service Name :{} , API Name : {}", "Payment Proxy",
          "Domestic payment FPSOut");
      log.error("Input Transaction Message : {} ",
          appendTransactionMessageRequest(transactionMessage));
      throw ex;
    }
  }

  private boolean isPAIN001CriteriaMet(PaymentMessage paymentInitMsg) {
    return (paymentInitMsg.getMessage()
        .get(PAYMENT_TYPE.name()).equals(SINGLE_IMMEDIATE_PAYMENT.getPaymentType()) || paymentInitMsg.getMessage()
        .get(PAYMENT_TYPE.name()).equals(FUTURE_DATED_PAYMENT.getPaymentType()))
        && paymentInitMsg.getMessage()
        .get(CREDITOR_CLEARING_SYSTEM_IDENTIFICATION.name()).equals(GBDSC) && paymentInitMsg.getMessage()
        .get(DEBTOR_CLEARING_SYSTEM_IDENTIFICATION.name()).equals(GBDSC);
  }


  private void addTransactionStatus(TransactionMessage transactionMessage) {
    transactionMessage.getAdditionalInfo().put(TRANSACTION_STATUS.name(), TransactionStatusValueEnum.RESERVE
        .name());
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage) {
    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TRANSACTION_CORRELATION_ID.name()).toString();
    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(Event.INT_REJECT).
        scope(TransactionMessageTypeEnum.FPS_OUT).
        serviceName(ServiceNames.TRANSACTION_MANAGER).build();
  }
}
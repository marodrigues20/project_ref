package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.RESERVE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.REVERSE;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes;
import com.tenx.universalbanking.transactionmanager.converter.CardAuthResponseBuilder;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.client.PaymentDecisionClient;
import com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.CardAuthService;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.messagebuilder.CAIN002MessageBuilder;
import com.tenx.universalbanking.transactionmanager.service.validation.MessageValidator;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class CardAuthMessageService extends AbstractCardAuthMessageService implements
    TransactionMessageService,
    CardAuthService {

  private final Logger logger = getLogger(CardAuthMessageService.class);

  @Autowired
  private CAIN002MessageBuilder cain002MessageBuilder;

  @Autowired
  private PaymentDecisionClient paymentDecisionClient;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private PaymentProcessResponseConverter responseConverter;

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private MessageValidator messageValidator;

  @Autowired
  private CardAuthResponseBuilder cardAuthResponseBuilder;

  @Autowired
  private PaymentAuthorisations paymentAuthorisations;

  @Autowired
  private AuthorisationFinder authorisationFinder;

  @Autowired
  private LedgerManagerClient lmClient;

  @Autowired
  private ReconciliationHelper reconciliationHelper;

  @Override
  public TransactionMessageTypeEnum getType() {
    return CARD_AUTH;
  }

  @Override
  public PaymentProcessResponse process(TransactionMessage message, HttpServletRequest request) {

    PaymentMessageTypeEnum paymentMessageType = PaymentMessageTypeEnum.CAIN001;
    messageValidator.validateMessage(message, paymentMessageType);

    messageServiceProcessorHelper.generateTransactionAndCorrelationIds(message);

    messageServiceProcessorHelper.addTransactionStatus(message, RESERVE);

    generateTransactionCode(message);
    PaymentDecisionTransactionResponse paymentResponse = paymentDecisionClient
        .getPaymentDecision(message);
    TransactionMessage cain002Response = cain002MessageBuilder
        .getCain002Response(message, paymentResponse);

    String subscriptionKey = messageServiceProcessorHelper
        .getSubscriptionKey(message, paymentMessageType);

    LedgerPostingResponse lmPostingResponse = lmClient.postTransactionToLedger(cain002Response);

    if (lmPostingResponse.isPostingSuccess()) {
      messageSender.sendPaymentMessage(subscriptionKey, cain002Response);
    } else {
      logger.debug(
          "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
      return responseConverter.buildPaymentProcessResponse(lmPostingResponse, message);
    }

    return responseConverter.buildPaymentProcessResponse(paymentResponse, cain002Response);
  }

  public CardAuthResponse processCardAuth(TransactionMessage message) {
    return processCardAuth(message, null);
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public CardAuthResponse processCardAuth(TransactionMessage cardAuthMessage,
      HttpServletRequest request) {
    PaymentMessageTypeEnum paymentMessageType = PaymentMessageTypeEnum.CAIN001;
    messageValidator.validateMessage(cardAuthMessage, paymentMessageType);

    boolean isReversal = isReversal(cardAuthMessage);
    //the below call will help us decide if its a duplicate entry
    Authorisations matchingAuth = getMatchingAuth(cardAuthMessage, isReversal, false);
    Authorisations matchingAuthForReversal = null;
    if (isReversal) {
      //Below call will help us to populate auth info for reversal process.
      matchingAuthForReversal = getMatchingAuth(cardAuthMessage, false, true);
      messageServiceProcessorHelper
          .generateTransactionAndCorrelationIds(cardAuthMessage, true,
              matchingAuthForReversal);
      messageServiceProcessorHelper.addTransactionStatus(cardAuthMessage, REVERSE);
    } else {
      messageServiceProcessorHelper.generateTransactionAndCorrelationIds(cardAuthMessage);
      messageServiceProcessorHelper.addTransactionStatus(cardAuthMessage, RESERVE);
    }

    TransactionMessage cain002Response;
    CardAuthResponse cardAuthResponse;

    if (checkDuplicateTransaction(matchingAuth, isReversal)) {
      cain002Response = cain002MessageBuilder.getCain002Response(cardAuthMessage);
      cardAuthResponse = cardAuthResponseBuilder.buildCardAuthResponse(cain002Response);
    } else {
      generateTransactionCode(cardAuthMessage);
      PaymentDecisionTransactionResponse paymentResponse = paymentDecisionClient
          .getPaymentDecision(cardAuthMessage);

      cain002Response = cain002MessageBuilder.getCain002Response(cardAuthMessage, paymentResponse);

      if (SUCCESS.name().equals(paymentResponse.getDecisionResponse())) {
        if (isReversal) {
          updateAuthStatusWhileReversal(matchingAuthForReversal);
        }
        saveCardAuthMessage(cain002Response, isReversal);
      }
      BigDecimal totalAmount = cardAuthMessage.getMessages().stream()
          .filter(message -> "CAIN001".equalsIgnoreCase(message.getType()) && !CollectionUtils
              .isEmpty(message.getMessage()))
          .map(message -> getBigDecimal(message.getMessage().getOrDefault("TOTAL_AMOUNT", "0")))
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      if (totalAmount.compareTo(BigDecimal.ZERO) == -1) {
        CardAuthResponse response = cardAuthResponseBuilder
            .buildCardAuthResponse(paymentResponse, cain002Response);
        if (!Objects.isNull(response.getReason())) {
          response.getReason().setCode(TransactionManagerExceptionCodes.NEGATIVE_AMOUNT_ERROR_CODE);
        }
        return response;
      }

      if (request != null) {
        messageServiceProcessorHelper.addTracingHeaders(cain002Response, request);
      }

      LedgerPostingResponse lmPostingResponse = lmClient
          .postTransactionToLedger(cain002Response, isReversal);

      if (lmPostingResponse.isPostingSuccess()) {
        messageServiceProcessorHelper.sendtoKafka(cain002Response, PaymentMessageTypeEnum.CAIN002);
        if (paymentResponse.getDecisionResponse().equals(SUCCESS.name())) {
          reconciliationHelper.saveReconciliationMessage(
              buildReconciliationMessage(cain002Response, Event.SUCCESS));
        } else {
          if(paymentResponse.getDecisionReason().getCode()== TransactionResponseReasonCodes.PAYMENT_RESERVE_FAILED_CODE){
            reconciliationHelper.saveReconciliationMessage(
                buildReconciliationMessage(cain002Response, Event.INT_REJECT_FUNDS));
          }
          else{
            reconciliationHelper.saveReconciliationMessage(
                buildReconciliationMessage(cain002Response, Event.INT_REJECT));
          }
        }
      } else {
        logger.debug(
            "Posting transaction on LM returned failure. Transaction message not posted to Topic.");
        return cardAuthResponseBuilder.buildCardAuthResponse(lmPostingResponse, cardAuthMessage);
      }

      cardAuthResponse = cardAuthResponseBuilder
          .buildCardAuthResponse(paymentResponse, cain002Response);
    }
    return cardAuthResponse;
  }

  private boolean checkDuplicateTransaction(Authorisations matchingAuth, boolean isReversal) {
    return isReversal && (matchingAuth != null);
  }

  private static BigDecimal getBigDecimal(Object value) {
    BigDecimal ret = null;
    if (value != null) {
      if (value instanceof BigDecimal) {
        ret = (BigDecimal) value;
      } else if (value instanceof String) {
        ret = new BigDecimal((String) value);
      } else if (value instanceof BigInteger) {
        ret = new BigDecimal((BigInteger) value);
      } else if (value instanceof Number) {
        ret = new BigDecimal(((Number) value).doubleValue());
      } else {
        throw new ClassCastException(
            "Not possible to coerce [" + value + "] from class " + value.getClass()
                + " into a BigDecimal.");
      }
    }
    return ret;
  }

  private ReconciliationMessageDto buildReconciliationMessage(TransactionMessage transactionMessage,
      Event event) {
    String transactionCorrelationId = transactionMessage.getAdditionalInfo().get(
        TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID.name()).toString();

    return ReconciliationMessageDto.builder().
        transactionCorrelationId(transactionCorrelationId).
        event(event).
        scope(TransactionMessageTypeEnum.CARD_AUTH).
        serviceName(ServiceNames.TRANSACTION_MANAGER).
        build();
  }
}

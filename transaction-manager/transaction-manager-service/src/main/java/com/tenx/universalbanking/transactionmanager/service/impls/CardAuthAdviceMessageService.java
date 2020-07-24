package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.FAILED;
import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.SUCCESS;
import static com.tenx.universalbanking.transactionmanager.utils.LoggerUtils.appendTransactionMessageRequest;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.AVAILABLE_BALANCE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TENANT_PARTY_KEY;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH_VIA_ADVICE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.APPROVED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.REVERSE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.converter.CardAuthResponseBuilder;
import com.tenx.universalbanking.transactionmanager.converter.PaymentProcessResponseConverter;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.client.PaymentDecisionClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.CardAuthService;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.messagebuilder.CAIN002MessageBuilder;
import com.tenx.universalbanking.transactionmanager.service.validation.MessageValidator;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.math.BigDecimal;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
public class CardAuthAdviceMessageService extends AbstractCardAuthMessageService implements
    TransactionMessageService,
    CardAuthService {

  private final Logger log = getLogger(CardAuthAdviceMessageService.class);

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

  @Override
  public TransactionMessageTypeEnum getType() {
    return CARD_AUTH_VIA_ADVICE;
  }

  @Autowired
  private LedgerManagerClient lmClient;

  @Override
  public PaymentProcessResponse process(TransactionMessage message, HttpServletRequest request) {

    PaymentMessageTypeEnum paymentMessageType = PaymentMessageTypeEnum.CAIN001;
    messageValidator.validateMessage(message, paymentMessageType);

    messageServiceProcessorHelper.generateTransactionAndCorrelationIds(message);

    messageServiceProcessorHelper.addTransactionStatus(message, APPROVED);

    generateTransactionCode(message);
    PaymentDecisionTransactionResponse paymentResponse = paymentDecisionClient
        .getPaymentDecision(message);
    TransactionMessage cain002Response = cain002MessageBuilder
        .getCain002Response(message, paymentResponse);

    String subscriptionKey = messageServiceProcessorHelper
        .getSubscriptionKey(message, paymentMessageType);
    messageServiceProcessorHelper.addTracingHeaders(cain002Response, request);

    messageSender.sendPaymentMessage(subscriptionKey, cain002Response);
    return responseConverter.buildPaymentProcessResponse(paymentResponse, cain002Response);
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public CardAuthResponse processCardAuth(TransactionMessage cardAuthMessage,
      HttpServletRequest request) {
    PaymentMessageTypeEnum paymentMessageType = PaymentMessageTypeEnum.CAIN001;
    messageValidator.validateMessage(cardAuthMessage, paymentMessageType);

    boolean isReversal = isReversal(cardAuthMessage);
    boolean isFailed = isFailed(cardAuthMessage);

    //the below call will help us decide if its a duplicate entry
    Authorisations matchingAuth = getMatchingAuth(cardAuthMessage, isReversal, false);
    Authorisations matchingAuthForReversal = null;
    if (isReversal) {
      //Below call will help us to populate auth info for reversal process.
      matchingAuthForReversal = getMatchingAuth(cardAuthMessage, false, true);
      messageServiceProcessorHelper
          .generateTransactionAndCorrelationIds(cardAuthMessage, true, matchingAuthForReversal);
      messageServiceProcessorHelper.addTransactionStatus(cardAuthMessage, REVERSE);
    } else if (!isFailed) {
      messageServiceProcessorHelper.generateTransactionAndCorrelationIds(cardAuthMessage);
      messageServiceProcessorHelper.addTransactionStatus(cardAuthMessage, APPROVED);
    }

    CardAuthResponse cardAuthResponse;

    if (checkDuplicateTransaction(matchingAuth) && !isFailed) {
      cardAuthResponse = processDuplicateCardAdviceRequest(cardAuthMessage);
    } else if (isFailed) {
      cardAuthResponse = processFailedCardAdviceRequest(cardAuthMessage, request);
    } else {
      cardAuthResponse = processCardAdviceRequest(cardAuthMessage, request, isReversal,
          matchingAuthForReversal);
    }
    return cardAuthResponse;
  }

  public CardAuthResponse processCardAuth(TransactionMessage cardAuthMessage) {
    return processCardAuth(cardAuthMessage, null);
  }

  private CardAuthResponse processCardAdviceRequest(TransactionMessage cardAuthMessage,
      HttpServletRequest request, boolean isReversal, Authorisations matchingAuthForReversal) {

    TransactionMessage cain002Response;
    CardAuthResponse cardAuthResponse;
    generateTransactionCode(cardAuthMessage);
    try {

      PaymentDecisionTransactionResponse paymentResponse = paymentDecisionClient
          .getPaymentDecision(cardAuthMessage);
      cain002Response = cain002MessageBuilder.getCain002Response(cardAuthMessage, paymentResponse);

      if (SUCCESS.name().equals(paymentResponse.getDecisionResponse())) {
        if (isReversal) {
          updateAuthStatusWhileReversal(matchingAuthForReversal);
        }
        saveCardAuthMessage(cain002Response, isReversal);
      }

      if (request != null) {
        messageServiceProcessorHelper.addTracingHeaders(cain002Response, request);
      }

      messageServiceProcessorHelper.sendtoKafka(cain002Response, PaymentMessageTypeEnum.CAIN002);
      cardAuthResponse = cardAuthResponseBuilder
          .buildCardAuthResponse(paymentResponse, cain002Response);
    } catch (RestClientException ex) {

      log.error("Rest Client Exception exception {}", ex);
      log.error("Downstream Service Name :{} , API Name : {}", "payment decision",
          "payment-decision/v1/payment-decision");
      log.error("Input Transaction Message : {} ",
          appendTransactionMessageRequest(cardAuthMessage));

      String msg = String.format(
          "Payment decision is failed, Transaction Correlation Id : {}, Request Correlation Id: {}, Tenant Party Key: {}",
          cardAuthMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()),
          cardAuthMessage.getAdditionalInfo().get(REQUEST_CORRELATION_ID.name()),
          cardAuthMessage.getAdditionalInfo().get(TENANT_PARTY_KEY.name()));
      log.error(msg);
      cain002Response = cain002MessageBuilder.getCain002Response(cardAuthMessage);
      cardAuthResponse = cardAuthResponseBuilder.buildCardAuthResponse(cain002Response);
    }

    return cardAuthResponse;
  }

  private CardAuthResponse processDuplicateCardAdviceRequest(
      TransactionMessage cardAuthMessage) {
    TransactionMessage cain002Response;
    CardAuthResponse cardAuthResponse;
    cain002Response = cain002MessageBuilder.getCain002Response(cardAuthMessage);
    cardAuthResponse = cardAuthResponseBuilder.buildCardAuthResponse(cain002Response);
    return cardAuthResponse;
  }

  private CardAuthResponse processFailedCardAdviceRequest(TransactionMessage cardAuthMessage,
      HttpServletRequest request) {
    TransactionMessage cain002Response;
    CardAuthResponse cardAuthResponse;
    updateAvailableBalance(cardAuthMessage);
    cain002Response = cain002MessageBuilder.getCain002Response(cardAuthMessage);
    if (request != null) {
      messageServiceProcessorHelper.addTracingHeaders(cain002Response, request);
    }
    messageServiceProcessorHelper.sendtoKafka(cain002Response, PaymentMessageTypeEnum.CAIN002);
    cardAuthResponse = cardAuthResponseBuilder.buildCardAuthResponse(cain002Response);
    return cardAuthResponse;
  }

  private void updateAvailableBalance(TransactionMessage cardAuthMessage) {

    BigDecimal availableBalance;
    try {
      availableBalance = lmClient.getAvailableBalanceFromLedger(
          cardAuthMessage.getMessages().get(0).getAdditionalInfo().get(SUBSCRIPTION_KEY.name())
              .toString());
      cardAuthMessage.getAdditionalInfo().put(AVAILABLE_BALANCE.name(), availableBalance);
    } catch (Exception e) {
      log.error("Unable to fetch balance from Ledger Manager");
    }
  }

  private boolean isFailed(TransactionMessage cardAuthMessage) {
    return cardAuthMessage.getAdditionalInfo().containsKey(TRANSACTION_STATUS.name())
        && cardAuthMessage.getAdditionalInfo().get(TRANSACTION_STATUS.name()).equals(FAILED.name());
  }

  private boolean checkDuplicateTransaction(Authorisations matchingAuth) {
    return !Objects.isNull(matchingAuth);
  }

}

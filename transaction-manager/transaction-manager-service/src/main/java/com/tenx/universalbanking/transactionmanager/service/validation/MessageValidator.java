package com.tenx.universalbanking.transactionmanager.service.validation;

import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.*;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class MessageValidator {

  private static final String SAFE_STRING = "SafeString";

  private final Logger logger = getLogger(getClass());

  public boolean validateMessage(TransactionMessage message,
      PaymentMessageTypeEnum... paymentMessageTypes) {

    if (message.getMessages().size() != paymentMessageTypes.length) {
        logger.debug("Transaction message is not valid: it contains an invalid number of payment messages! {}"
                ,appendMessageAdditionalInfo(message));
      throw new InvalidTransactionMessageException(
          "Transaction message is not valid: it contains an invalid number of payment messages!");
    }

    Set<PaymentMessageTypeEnum> allowedPaymentMessageTypes = new HashSet<>(
        asList(paymentMessageTypes));

    message.getMessages().forEach((PaymentMessage paymentMessage) -> {
      PaymentMessageTypeEnum paymentMessageType = PaymentMessageTypeEnum
          .valueOf(paymentMessage.getType());
      if (!allowedPaymentMessageTypes.contains(paymentMessageType)) {
        logger.debug("Transaction message is not valid: it contains an invalid payment message! {}"
                ,appendMessageAdditionalInfo(message));
        throw new InvalidTransactionMessageException(
            "Transaction message is not valid: it contains an invalid payment message!");
      }
    });

    return true;
  }

  private String appendMessageAdditionalInfo(TransactionMessage message) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(" Subscription key: ");
    stringBuilder.append(validInput("SUBSCRIPTION_KEY", message.getAdditionalInfo().get(SUBSCRIPTION_KEY.name()).toString()));
    stringBuilder.append(" Tenant Party Key: ");
    stringBuilder.append(validInput("TENANT_PARTY_KEY", message.getAdditionalInfo().get(TENANT_PARTY_KEY.name()).toString()));
    return stringBuilder.toString();
  }

  private String validInput(String context, String value) {
    String result = null;
    try {
        result = ESAPI.validator().getValidInput(context, value, SAFE_STRING, 200, true);
    } catch (ValidationException e) {
        logger.debug("Exception in ESAPI Valition: {}", e.getMessage());
    }
    return result;
  }

  public boolean validateAnyMessage(TransactionMessage message,
      PaymentMessageTypeEnum... paymentMessageTypes) {

    if (message.getMessages().size() != 1) {
      throw new InvalidTransactionMessageException(
          "Transaction message is not valid: it contains an invalid number of payment messages!");
    }
    Set<PaymentMessageTypeEnum> allowedPaymentMessageTypes = new HashSet<>(
        asList(paymentMessageTypes));
    return allowedPaymentMessageTypes.contains(PaymentMessageTypeEnum.valueOf(message.getMessages().get(0).getType())) ? true
        : false;
  }

}

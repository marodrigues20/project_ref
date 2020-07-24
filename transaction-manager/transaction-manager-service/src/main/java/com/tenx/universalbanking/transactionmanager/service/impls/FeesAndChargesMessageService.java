package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.SUCCESS;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.validation.MessageValidator;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;

@Service
public class FeesAndChargesMessageService implements TransactionMessageService {

  @Autowired
  private MessageServiceProcessorHelper messageServiceProcessorHelper;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private MessageValidator messageValidator;

  @Override
  public TransactionMessageTypeEnum getType() {
    return TransactionMessageTypeEnum.FEES_AND_CHARGES;
  }

  @Override
  public PaymentProcessResponse process(TransactionMessage message, HttpServletRequest request) {

    messageValidator.validateMessage(message, PaymentMessageTypeEnum.FEES_AND_CHARGES);

    messageServiceProcessorHelper.generateTransactionAndCorrelationIds(message);

    String subscriptionKey = messageServiceProcessorHelper.getSubscriptionKey(message, PaymentMessageTypeEnum.FEES_AND_CHARGES);

    messageServiceProcessorHelper.addTracingHeaders(message, request);
    messageSender.sendPaymentMessage(subscriptionKey, message);

    return new PaymentProcessResponse(SUCCESS.name());
  }
}

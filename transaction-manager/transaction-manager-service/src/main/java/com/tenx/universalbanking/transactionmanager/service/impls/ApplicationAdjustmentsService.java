package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.APPLICATION_ADJUSTMENTS;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationAdjustmentsService implements TransactionMessageService {

  private final Logger logger = getLogger(getClass());

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private LedgerManagerClient lmClient;

  @Override
  public TransactionMessageTypeEnum getType() {
    return APPLICATION_ADJUSTMENTS;
  }

  @Override
  public PaymentProcessResponse process(TransactionMessage message, HttpServletRequest request) {
    logger.debug("Received a Application Adjustments message");
    PaymentProcessResponse response = new PaymentProcessResponse();
    String key = message.getMessages().get(0).getAdditionalInfo()
        .get(PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY.name()).toString();
    LedgerPostingResponse lmPostingResponse = lmClient.postTransactionToLedger(message);
    if(lmPostingResponse.isPostingSuccess()){
      //Message sending is done in the TransactionMessageHandlerBase.java
      response.setPaymentStatus("SUCCESS");
      logger.debug("Posting transaction on LM is a success. Application adjustments message will be posted to the topic.");
    } else{
      response.setPaymentStatus("FAILED");
      logger.error("Posting transaction on LM returned failure. Application adjustments message not posted to Topic.");
    }
    logger.debug("Application Adjustments message processed successfully.");
    return response;
  }
}

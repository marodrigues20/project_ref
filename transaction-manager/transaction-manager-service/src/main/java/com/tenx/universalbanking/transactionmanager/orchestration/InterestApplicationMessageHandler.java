package com.tenx.universalbanking.transactionmanager.orchestration;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.INTEREST_APPLICATION;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageSender;
import com.tenx.universalbanking.transactionmanager.service.impls.InterestApplicationService;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class InterestApplicationMessageHandler extends
    TransactionMessageHandlerBase implements
    TransactionMessageHandler {

  private static final Logger logger = getLogger(InterestApplicationMessageHandler.class);

  private static final String SUCCESS = "SUCCESS";

  private final InterestApplicationService service;

  InterestApplicationMessageHandler(GeneratorUtil generatorUtil,
      TransactionMessageSender transactionMessageSender, InterestApplicationService service) {
    super(generatorUtil, transactionMessageSender);
    this.service = service;
  }

  @Override
  public TransactionMessageTypeEnum handlesMessageOfType() {
    return INTEREST_APPLICATION;
  }

  @Override
  public void handleMessage(TransactionMessage transactionMessage) {
    super.addCorrelationTransactionIDs(transactionMessage);
    if(SUCCESS.equalsIgnoreCase(service.process(transactionMessage, null).getPaymentStatus())){
      super.handleMessage(transactionMessage);
      logger.debug("Post interest application message to LM has succeeded. Message was sent to topic.");
    } else{
      logger.error("Post interest application message to LM has failed. Message was not be sent to topic.");
    }
  }

}

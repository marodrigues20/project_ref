package com.tenx.universalbanking.transactionmanager.orchestration;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.APPLICATION_ADJUSTMENTS;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageSender;
import com.tenx.universalbanking.transactionmanager.service.impls.ApplicationAdjustmentsService;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ApplicationAdjustmentsMessageHandler extends
    TransactionMessageHandlerBase implements
    TransactionMessageHandler {

  private static final Logger logger = getLogger(InterestApplicationMessageHandler.class);

  private static final String SUCCESS = "SUCCESS";

  private final ApplicationAdjustmentsService service;

  ApplicationAdjustmentsMessageHandler(GeneratorUtil generatorUtil,
      TransactionMessageSender transactionMessageSender, ApplicationAdjustmentsService service) {
    super(generatorUtil, transactionMessageSender);
    this.service = service;
  }

  @Override
  public TransactionMessageTypeEnum handlesMessageOfType() {
    return APPLICATION_ADJUSTMENTS;
  }

  @Override
  public void handleMessage(TransactionMessage transactionMessage) {
    super.addCorrelationTransactionIDs(transactionMessage);
    if (SUCCESS.equalsIgnoreCase(service.process(transactionMessage, null).getPaymentStatus())) {
      super.handleMessage(transactionMessage);
      logger.debug(
          "Post interest application message to LM has succeeded. Message was sent to topic.");
    } else {
      logger.error(
          "Post interest application message to LM has failed. Message was not be sent to topic.");
    }
  }

}

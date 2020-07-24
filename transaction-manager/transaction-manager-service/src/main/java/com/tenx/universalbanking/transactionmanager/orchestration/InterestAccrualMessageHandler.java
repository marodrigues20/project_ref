package com.tenx.universalbanking.transactionmanager.orchestration;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.INTEREST_ACCRUAL;

import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageSender;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import org.springframework.stereotype.Component;


@Component
public final class InterestAccrualMessageHandler extends TransactionMessageHandlerBase implements
    TransactionMessageHandler {
	
  InterestAccrualMessageHandler(GeneratorUtil generatorUtil,
      TransactionMessageSender transactionMessageSender) {
    super(generatorUtil, transactionMessageSender);
  }

  @Override
  public TransactionMessageTypeEnum handlesMessageOfType() {
    return INTEREST_ACCRUAL;
  }
}

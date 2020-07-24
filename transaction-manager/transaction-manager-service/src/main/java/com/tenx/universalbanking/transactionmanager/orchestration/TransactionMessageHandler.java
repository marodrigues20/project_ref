package com.tenx.universalbanking.transactionmanager.orchestration;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;

interface TransactionMessageHandler {

  TransactionMessageTypeEnum handlesMessageOfType();

  void handleMessage(TransactionMessage message);
}

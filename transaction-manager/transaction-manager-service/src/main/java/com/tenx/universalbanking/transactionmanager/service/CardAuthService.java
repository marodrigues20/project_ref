package com.tenx.universalbanking.transactionmanager.service;

import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import javax.servlet.http.HttpServletRequest;

public interface CardAuthService {

  TransactionMessageTypeEnum getType();

  CardAuthResponse processCardAuth(TransactionMessage message, HttpServletRequest request);
}

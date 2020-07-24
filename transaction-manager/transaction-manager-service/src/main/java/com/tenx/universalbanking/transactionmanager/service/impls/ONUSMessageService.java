package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_MISCELLANEOUS_CREDIT_OPERATION_OTHER_INTERNAL_CREDIT;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentsTransactionCodeEnum.PAYMENTS_MISCELLANEOUS_DEBIT_OPERATION_OTHER_INTERNAL;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ONUSMessageService extends InternalMessageService {

  private final Logger logger = getLogger(getClass());

  @Override
  public TransactionMessageTypeEnum getType() {
    return TransactionMessageTypeEnum.ON_US;
  }

  @Override
  public void setTransactionCode(TransactionMessage transactionMessage) {
    transactionMessage.getMessages().forEach(item -> {
      if (item.getType().equals(PaymentMessageTypeEnum.PAIN001.name())) {
        item.getAdditionalInfo().put(
            TRANSACTION_CODE.name(),
            PAYMENTS_MISCELLANEOUS_DEBIT_OPERATION_OTHER_INTERNAL.getValue());

        logger.debug(
            "Setting TranscationCode Value:{}",
            PAYMENTS_MISCELLANEOUS_DEBIT_OPERATION_OTHER_INTERNAL
                .getValue());
      } else {
        item.getAdditionalInfo().put(
            TRANSACTION_CODE.name(),
            PAYMENTS_MISCELLANEOUS_CREDIT_OPERATION_OTHER_INTERNAL_CREDIT.getValue());

        logger.debug(
            "Setting TranscationCode Value:{}",
            PAYMENTS_MISCELLANEOUS_CREDIT_OPERATION_OTHER_INTERNAL_CREDIT
                .getValue());
      }
    });
  }
}

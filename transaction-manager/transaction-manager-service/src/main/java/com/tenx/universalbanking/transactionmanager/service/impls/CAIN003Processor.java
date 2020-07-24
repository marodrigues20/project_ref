package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionType.CASH_WITHDRAWAL;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionType.PURCHASE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionType.PURCHASE_CASH_BACK;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionType.QUASI_CASH;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_TYPE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN003;
import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.service.PaymentMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CAIN003Processor implements PaymentMessageService {

  private final Logger LOGGER = getLogger(CAIN003Processor.class);

  @Autowired
  private AuthorisationFinder authorisationFinder;

  @Override
  public PaymentMessageTypeEnum getType() {
    return CAIN003;
  }

  @Override
  public Authorisations getAuthorisations(TransactionMessage message) {
    PaymentMessage paymentMessage = message.getMessages().get(0);
    Map<String, Object> messageMap = paymentMessage.getMessage();
    setTransactionType(paymentMessage);
    String tsysAccountId = paymentMessage.getAdditionalInfo().get(CARD_PROCESSOR_ACCOUNT_ID.name())
        .toString();
    Date transactionDate = new DateTime( messageMap.get(TRANSACTION_DATE.name())).toDate();
    BigDecimal transactionAmount = new BigDecimal(
        messageMap.get(TRANSACTION_AMOUNT.name()).toString());
    String authorisationCode = (String) paymentMessage.getAdditionalInfo().get(AUTHORISATION_CODE.name());
    String bankNetReferenceNumber = (String) messageMap.get(BANKNET_REFERENCE_NUMBER.name());
    return authorisationFinder.getAuthorisation(tsysAccountId,
        transactionDate, transactionAmount, authorisationCode, bankNetReferenceNumber);
  }

  @Override
  public void setTransactionType(PaymentMessage paymentMessage) {
    String mCC = paymentMessage.getMessage().get(MERCHANT_CATEGORY_CODE.name()).toString();
    String transactionType;
    switch (mCC) {
      case "6010":
        transactionType = PURCHASE_CASH_BACK.getTransactionCode();
        break;
      case "6011":
        transactionType = CASH_WITHDRAWAL.getTransactionCode();
        break;
      case "6012":
        transactionType = QUASI_CASH.getTransactionCode();
        break;
      default:
        transactionType = PURCHASE.getTransactionCode();
        break;
    }
    paymentMessage.getMessage().put(TRANSACTION_TYPE.name(), transactionType);
  }
}

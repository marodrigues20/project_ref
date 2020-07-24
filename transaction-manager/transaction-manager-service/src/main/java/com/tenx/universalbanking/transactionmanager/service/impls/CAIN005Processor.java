package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionType.CASH_DEPOSIT;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionType.REFUND;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.TRANSACTION_TYPE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN005;

import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.service.PaymentMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Cain003Enum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CAIN005Processor implements PaymentMessageService {

  @Autowired
  private AuthorisationFinder authorisationFinder;

  @Override
  public PaymentMessageTypeEnum getType() {
    return CAIN005;
  }

  @Override
  public Authorisations getAuthorisations(TransactionMessage message) {
    PaymentMessage paymentMessage = message.getMessages().get(0);
    Map<String, Object> messageMap = paymentMessage.getMessage();
    setTransactionType(paymentMessage);
    String tsysAccountId = paymentMessage.getAdditionalInfo().get(CARD_PROCESSOR_ACCOUNT_ID.name())
        .toString();
    Date transactionDate = new DateTime( messageMap.get(Cain003Enum.TRANSACTION_DATE.name())).toDate();
    BigDecimal transactionAmount = new BigDecimal(messageMap.get(TRANSACTION_AMOUNT.name()).toString());
    String authorisationCode = (String) paymentMessage.getAdditionalInfo().get(AUTHORISATION_CODE.name());
    String bankNetReferenceNumber = (String) messageMap.get(BANKNET_REFERENCE_NUMBER.name());
    return authorisationFinder.getAuthorisation(tsysAccountId,
        transactionDate, transactionAmount, authorisationCode, bankNetReferenceNumber);
  }

  @Override
  public void setTransactionType(PaymentMessage paymentMessage) {
    String mCC = paymentMessage.getMessage().get(MERCHANT_CATEGORY_CODE.name()).toString();
    String transactionType;
    if ("6010".equals(mCC)) {
      transactionType = CASH_DEPOSIT.getTransactionCode();
    } else {
      transactionType = REFUND.getTransactionCode();
    }
    paymentMessage.getMessage().put(TRANSACTION_TYPE.name(), transactionType);
  }
}

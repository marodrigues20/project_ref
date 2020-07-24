package com.tenx.tsys.proxybatch.service.cain003.helper;

import static com.tenx.tsys.proxybatch.service.CainIsoMapBuilder.DataType.BOOLEAN;
import static com.tenx.tsys.proxybatch.service.CainIsoMapBuilder.DataType.DATE;
import static com.tenx.tsys.proxybatch.service.CainIsoMapBuilder.DataType.DECIMAL;
import static com.tenx.tsys.proxybatch.service.CainIsoMapBuilder.DataType.STRING;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BILLING_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.BILLING_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARDHOLDER_PRESENT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARDHOLER_PRESENT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_CITY;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_NAME;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_STATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_ACCEPTOR_TERMINAL_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CARD_DATA_ENTRY_MODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CASH_BACK_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.CASH_BACK_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.COMMON_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.EXCHANGE_RATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.MERCHANT_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.RECURRING_PAYMENT_INDICATOR;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TOKENISED_PAN;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_AMOUNT_QUALIFIER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_POSTED_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_POSTED_TIME;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.DEBIT_CREDIT_INDICATOR;

import com.tenx.tsys.proxybatch.client.debitcardmanager.model.DebitCardResponse;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.PaymentMessage;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessage;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessageHeader;
import com.tenx.tsys.proxybatch.service.CainIsoMapBuilder;
import com.tenx.tsys.proxybatch.service.PaymentMethodProvider;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Cain003MessageBuilder {

  private static final String DEBIT = "DEBIT";
  private static final String ACTL = "ACTL";
  @Autowired
  private CainIsoMapBuilder cain003IsoMapBuilder;
  @Autowired
  private PaymentMethodProvider paymentMethodProvider;
  private List<String> messageDataPositionList = new ArrayList<String>();
  private List<String> additionalInfoDataPositionList = new ArrayList<String>();

  public Cain003MessageBuilder() {

    messageDataPositionList.add(TRANSACTION_POSTED_DATE.name() + ",031,037," + DATE.name());
    messageDataPositionList.add(TRANSACTION_POSTED_TIME.name() + ",038,044," + DATE.name());
    messageDataPositionList.add(TOKENISED_PAN.name() + ",045,063," + STRING.name());
    messageDataPositionList.add(CARD_ACCEPTOR_NAME.name() + ",120,144," + STRING.name());
    messageDataPositionList.add(CARD_ACCEPTOR_CITY.name() + ",145,157," + STRING.name());
    messageDataPositionList.add(CARD_ACCEPTOR_STATE.name() + ",158,160," + STRING.name());
    messageDataPositionList.add(CARD_ACCEPTOR_COUNTRY_CODE.name() + ",161,163," + STRING.name());
    messageDataPositionList.add(MERCHANT_NUMBER.name() + ",164,179," + STRING.name());
    messageDataPositionList.add(CARD_DATA_ENTRY_MODE.name() + ",194,195," + STRING.name());
    messageDataPositionList.add(CARDHOLER_PRESENT.name() + ",196,196," + BOOLEAN.name());
    messageDataPositionList.add(CARDHOLDER_PRESENT.name() + ",196,196," + BOOLEAN.name());
    messageDataPositionList.add(CARD_ACCEPTOR_ID.name() + ",197,211," + STRING.name());
    messageDataPositionList.add(CARD_ACCEPTOR_TERMINAL_ID.name() + ",228,235," + STRING.name());
    messageDataPositionList.add(BANKNET_REFERENCE_NUMBER.name() + ",238,246," + STRING.name());
    messageDataPositionList.add(MERCHANT_CATEGORY_CODE.name() + ",324,327," + STRING.name());
    messageDataPositionList.add(BILLING_CURRENCY_CODE.name() + ",365,367," + STRING.name());
    messageDataPositionList.add(TRANSACTION_DATE.name() + ",435,441," + DATE.name());
    messageDataPositionList.add(BILLING_AMOUNT.name() + ",456,470," + DECIMAL.name());
    messageDataPositionList.add(TRANSACTION_AMOUNT.name() + ",442,455," + DECIMAL.name());
    messageDataPositionList.add(CASH_BACK_AMOUNT.name() + ",471,485," + DECIMAL.name());
    messageDataPositionList.add(COMMON_COUNTRY_CODE.name() + ",607,609," + STRING.name());
    messageDataPositionList.add(RECURRING_PAYMENT_INDICATOR.name() + ",658,658," + BOOLEAN.name());
    messageDataPositionList.add(EXCHANGE_RATE.name() + ",662,680," + DECIMAL.name());
    messageDataPositionList.add(CASH_BACK_INDICATOR.name() + ",743,743," + BOOLEAN.name());

    additionalInfoDataPositionList.add(
        PaymentMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID.name() + ",002,012," + STRING
            .name());
    additionalInfoDataPositionList
        .add(PaymentMessageAdditionalInfoEnum.AUTHORISATION_CODE.name() + ",186,191," + STRING
            .name());
    additionalInfoDataPositionList
        .add(PaymentMessageAdditionalInfoEnum.SETTLEMENT_DATE.name() + ",402,408," + DATE.name());

    Collections.unmodifiableList(getMessageDataPositionList());
    Collections.unmodifiableList(getAdditionalInfoDataPositionList());
  }

  public List<String> getMessageDataPositionList() {
    return messageDataPositionList;
  }

  public List<String> getAdditionalInfoDataPositionList() {
    return additionalInfoDataPositionList;
  }

  public TransactionMessage setTransactionMessageHeader(TransactionMessage transactionMessage) {
    TransactionMessageHeader messageHeader = new TransactionMessageHeader();
    messageHeader.setType(TransactionMessageTypeEnum.CLEARING.name());
    transactionMessage.setHeader(messageHeader);
    return transactionMessage;
  }

  public TransactionMessage setTransactionMessage(TransactionMessage transactionMessage,
      String rawMessage, DebitCardResponse debitCardResponse) throws ParseException {
    List<PaymentMessage> paymentMessageList = new ArrayList<PaymentMessage>();
    PaymentMessage message = new PaymentMessage();
    message.setType(PaymentMessageTypeEnum.CAIN003.name());
    Map<String, Object> messageMap = generateDebitCardPaymentCain003Message(rawMessage,
        getMessageDataPositionList());
    messageMap.put(TRANSACTION_AMOUNT_QUALIFIER.name(), ACTL);
    message.setMessage(messageMap);
    Map<String, Object> messageAdditionalMap = generateDebitCardPaymentCain003Message(rawMessage,
        getAdditionalInfoDataPositionList());
    messageAdditionalMap.put(DEBIT_CREDIT_INDICATOR.name(), DEBIT);
    paymentMethodProvider.setPaymentMethodType(messageMap, messageAdditionalMap);
    message.setAdditionalInfo(messageAdditionalMap);
    setSubscriptionKey(debitCardResponse, message);
    paymentMessageList.add(message);
    transactionMessage.setMessages(paymentMessageList);
    return transactionMessage;
  }

  private void setSubscriptionKey(DebitCardResponse debitCardResponse, PaymentMessage message) {
    HashMap<String, String> additionalInfoMap = (HashMap<String, String>) message
        .getAdditionalInfo();
    if (additionalInfoMap != null) {
      additionalInfoMap
          .put(PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY.name(),
              debitCardResponse.getSubscriptionKey());
      additionalInfoMap
          .put(PaymentMessageAdditionalInfoEnum.PRODUCT_KEY.name(),
              debitCardResponse.getProductKey());

    }
  }

  public Map generateDebitCardPaymentCain003Message(String message,
      List<String> dataPositionList) throws ParseException {
    Map<String, Object> messageDataMap = new HashMap<>();
    int lastIndex = dataPositionList.size() - 1;
    String lastItemOfList = dataPositionList.get(lastIndex);
    if (message.length() >= Integer.parseInt(lastItemOfList.split(",")[2])) {
      messageDataMap = cain003IsoMapBuilder.buildMessageMap(message, dataPositionList);
    }
    return messageDataMap;
  }

}

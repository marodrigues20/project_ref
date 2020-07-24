package com.tenx.universalbanking.transactionmanager.service.messagebuilder;

import static com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum.AUTHORISATION_RESULT_RESPONSE;
import static com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum.BALANCE_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum.MASKED_PAN;
import static com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum.PDF_ERROR_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Caaa002Enum.PDF_ERROR_DESC;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.PAN_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TOTAL_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.REQUEST_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.TOP_UP_BY_CARD;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAAA002;

import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CAAA002MessageBuilder {

  @Autowired
  private GeneratorUtil generatorUtil;

  public TransactionMessage caaa002MessageBuilder(TransactionMessage transactionMessage,
      PaymentProcessResponse paymentProcessResponse) {

    TransactionMessage txnMessage = new TransactionMessage();
    txnMessage.setHeader(buildHeader());
    txnMessage.setAdditionalInfo(buildAdditionalInfo());

    List<PaymentMessage> paymentMessageList = new ArrayList<>();

    paymentMessageList.add(buildCaaa002Message(transactionMessage, paymentProcessResponse));

    txnMessage.setMessages(paymentMessageList);

    return txnMessage;
  }

  public TransactionMessageHeader buildHeader() {
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(TOP_UP_BY_CARD.name());
    header.setUrl("");
    return header;
  }

  public Map<String, Object> buildAdditionalInfo() {
    Map<String, Object> additionalInfoMap = new HashMap<>();
    additionalInfoMap.put(REQUEST_ID.name(), generatorUtil.generateRandomKey());
    return additionalInfoMap;
  }

  public PaymentMessage buildCaaa002Message(TransactionMessage transactionMessage,
      PaymentProcessResponse paymentProcessResponse) {

    PaymentMessage caaa002Message = new PaymentMessage();

    PaymentMessage caaa0001PaymentMessage = transactionMessage.getMessages().get(0);
    caaa002Message.setType(CAAA002.name());

    String panNumber = caaa0001PaymentMessage.getMessage().get(PAN_NUMBER.name()).toString();
    panNumber = StringUtils.overlay(
        panNumber, StringUtils.repeat("X", panNumber.length() - 10), 6, panNumber.length() - 4);

    caaa002Message.setAdditionalInfo(caaa0001PaymentMessage.getAdditionalInfo());

    Map<String, Object> paymentMessageMap = new HashMap<>();
    paymentMessageMap.put(MASKED_PAN.name(), panNumber);
    paymentMessageMap
        .put(BALANCE_AMOUNT.name(), caaa0001PaymentMessage.getMessage().get(TOTAL_AMOUNT.name()));
    paymentMessageMap
        .put(AUTHORISATION_RESULT_RESPONSE.name(), paymentProcessResponse.getPaymentStatus());
    paymentMessageMap.put(PDF_ERROR_CODE.name(), paymentProcessResponse.getReason().getCode());
    paymentMessageMap.put(PDF_ERROR_DESC.name(), paymentProcessResponse.getReason().getMessage());
    caaa002Message.setMessage(paymentMessageMap);

    return caaa002Message;
  }

}

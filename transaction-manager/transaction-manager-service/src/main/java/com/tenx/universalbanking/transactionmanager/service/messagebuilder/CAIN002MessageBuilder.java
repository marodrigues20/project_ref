package com.tenx.universalbanking.transactionmanager.service.messagebuilder;

import static com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes.INSUFFICIENT_BALANCE_REASON;
import static com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes.NO_CUSTOMER_INFO_IN_SM2;
import static com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes.PAYMENT_RESERVE_FAILED_CODE;
import static com.tenx.universalbanking.transactionmanager.rest.constant.TransactionResponseReasonCodes.RULES_FAILED_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.CARD_ACCEPTOR_NAME;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.CARDHOLDER_PRESENT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.CARD_ACCEPTOR_CITY;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.CARD_ACCEPTOR_COUNTRY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.CARD_ACCEPTOR_POST_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.CARD_DATA_ENTRY_MODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.INITIATOR_PARTY_ID;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TOTAL_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_AMOUNT_QUALIFIER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_CURRENCY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_RESPONSE_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_RESPONSE_REASON_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.TRANSACTION_TYPE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.enums.CainResponseCode;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.Cain001Enum;
import com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CAIN002MessageBuilder {

  @Autowired
  private GeneratorUtil generatorUtil;

  public TransactionMessage getCain002Response(TransactionMessage cain001TransactionMessage) {

    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(cain001TransactionMessage.getHeader().getType());
    transactionMessage.setHeader(header);
    transactionMessage.setAdditionalInfo(cain001TransactionMessage.getAdditionalInfo());
    transactionMessage.setMessages(
        singletonList(
            buildCain002PaymentMessage(cain001TransactionMessage.getMessages().get(0))));

    return transactionMessage;
  }

  public TransactionMessage getCain002Response(TransactionMessage cain001TransactionMessage,
      PaymentDecisionTransactionResponse paymentDecisionResponse) {

    String decisionResponse = paymentDecisionResponse.getDecisionResponse();
    PaymentDecisionReasonDTO decisionReason = paymentDecisionResponse.getDecisionReason();

    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(cain001TransactionMessage.getHeader().getType());
    transactionMessage.setHeader(header);
    transactionMessage.setMessages(
        singletonList(
            buildCain002PaymentMessage(cain001TransactionMessage.getMessages().get(0),
                decisionResponse, decisionReason)));
    transactionMessage.setAdditionalInfo(
        buildTransactionMessageAdditionalInfo( decisionResponse,
            paymentDecisionResponse.getTransactionMessage()));
    return transactionMessage;
  }

  private Map<String, Object> buildTransactionMessageAdditionalInfo(
      String responseStatus,
      com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage pdfResponseMessage) {

    Map<String, Object> responseAdditionalInfo = new HashMap<>();
    if (pdfResponseMessage != null) {
      responseAdditionalInfo.putAll(toAdditionalInfoMap(pdfResponseMessage.getAdditionalInfo()));
    }
    responseAdditionalInfo.put(TRANSACTION_STATUS.name(), responseStatus.equals(SUCCESS.name()) ?
        TransactionStatusValueEnum.SUCCESS : FAILED);
    return responseAdditionalInfo;
  }

  private Map<String, Object> toAdditionalInfoMap(Object additionalInfoObject) {
    return isNull(additionalInfoObject) ? EMPTY_MAP : (Map<String, Object>) additionalInfoObject;
  }

  private PaymentMessage buildCain002PaymentMessage(PaymentMessage cain001TransactionMessage) {
    return buildCain002PaymentMessage(cain001TransactionMessage, null, null);
  }

  private PaymentMessage buildCain002PaymentMessage(PaymentMessage cain001TransactionMessage,
      String responseStatus, PaymentDecisionReasonDTO paymentDecisionReasonDTO) {

    PaymentMessage paymentMessage = new PaymentMessage();

    Map<String, Object> message = new HashMap<>();
    Map<String, Object> cain001MessageMap = cain001TransactionMessage.getMessage();

    message.put(AMOUNT.name(), cain001MessageMap.get(Cain001Enum.AMOUNT.name()));
    message.put(TOTAL_AMOUNT.name(), cain001MessageMap.get(Cain001Enum.TOTAL_AMOUNT.name()));
    message.put(TRANSACTION_CURRENCY_CODE.name(),
        cain001MessageMap.get(Cain001Enum.TRANSACTION_CURRENCY_CODE.name()));
    message.put(INITIATOR_PARTY_ID.name(),
        cain001MessageMap.get(Cain001Enum.INITIATOR_PARTY_ID.name()));
    message.put(MERCHANT_CATEGORY_CODE.name(),
        cain001MessageMap.get(Cain001Enum.MERCHANT_CATEGORY_CODE.name()));
    message
        .put(TRANSACTION_DATE.name(), cain001MessageMap.get(Cain001Enum.TRANSACTION_DATE.name()));
    message
        .put(TRANSACTION_TYPE.name(), cain001MessageMap.get(Cain001Enum.TRANSACTION_TYPE.name()));
    message.put(TRANSACTION_AMOUNT_QUALIFIER.name(),
        cain001MessageMap.get(Cain001Enum.TRANSACTION_AMOUNT_QUALIFIER.name()));
    message.put(CARD_DATA_ENTRY_MODE.name(),
        cain001MessageMap.get(Cain001Enum.CARD_DATA_ENTRY_MODE.name()));
    message.put(CARDHOLDER_PRESENT.name(),
        cain001MessageMap.get(Cain001Enum.CARDHOLDER_PRESENT.name()));
    message.put(CARD_ACCEPTOR_NAME.name(),
        cain001MessageMap.get(Cain001Enum.CARD_ACCEPTOR_NAME.name()));

    message.put(CARD_ACCEPTOR_POST_CODE.name(),
        cain001MessageMap.get(Cain001Enum.CARD_ACCEPTOR_POST_CODE.name()));
    message.put(CARD_ACCEPTOR_CITY.name(),
        cain001MessageMap.get(Cain001Enum.CARD_ACCEPTOR_CITY.name()));
    message.put(CARD_ACCEPTOR_COUNTRY_CODE.name(),
        cain001MessageMap.get(Cain001Enum.CARD_ACCEPTOR_COUNTRY_CODE.name()));

    if (!isEmpty(responseStatus)) {
      message.put(TRANSACTION_RESPONSE_CODE.name(),
          responseStatus.equals(SUCCESS.name()) ? CainResponseCode.APPR.value() :
              CainResponseCode.DECL.value());
      if (responseStatus.equals(SUCCESS.name())) {
        message
            .put(AUTHORISATION_CODE.name(),
                Integer.toString(generatorUtil.generate6DigitAuthCode()));
      } else {
        if (PAYMENT_RESERVE_FAILED_CODE == paymentDecisionReasonDTO.getCode()) {
          message.put(TRANSACTION_RESPONSE_REASON_CODE.name(), INSUFFICIENT_BALANCE_REASON);
        } else if (RULES_FAILED_CODE == paymentDecisionReasonDTO.getCode()) {
          message.put(TRANSACTION_RESPONSE_REASON_CODE.name(), NO_CUSTOMER_INFO_IN_SM2);
        }
      }
    }
    message.put(BANKNET_REFERENCE_NUMBER.name(),
        cain001MessageMap.get(BANKNET_REFERENCE_NUMBER.name()));
    message.put(CARD_PROCESSOR_ACCOUNT_ID.name(), cain001TransactionMessage.getAdditionalInfo().get(
        CARD_PROCESSOR_ACCOUNT_ID
            .name())); // can be tested once sm2 gets tsys accountid story played
    paymentMessage.setType(CAIN002.name());
    paymentMessage.setMessage(message);
    paymentMessage.setAdditionalInfo(cain001TransactionMessage.getAdditionalInfo());
    return paymentMessage;
  }
}
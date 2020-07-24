package com.tenx.universalbanking.transactionmanager.service.turbine;

import static com.tenx.universalbanking.transactionmessage.enums.Cain002Enum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.AVAILABLE_BALANCE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN002;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;

import com.tenx.universalbanking.transactionmanager.exception.CAIN002NotReceivedException;
import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.rest.request.turbine.Money;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.ReasonCodeEnum;
import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.SchemeMessageResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.math.BigDecimal;
import java.util.Currency;
import org.springframework.stereotype.Component;

@Component
class SchemeMessageResponseBuilder {

  SchemeMessageResponse create(CardAuthResponse cardAuthResponse,
      Card card) {
    return new SchemeMessageResponse(getAuthCode(cardAuthResponse.getCain002Response()),
        getUpdatedBalance(cardAuthResponse.getCain002Response(), card.getCardCurrencyCode()),
        getReasonCode(cardAuthResponse.getCardAuthStatus()));
  }

  private Money getUpdatedBalance(TransactionMessage cain002Response, String currency) {
    return Money.builder()
        .amount(fromString(cain002Response.getAdditionalInfo().get(AVAILABLE_BALANCE.name())))
        .currency(String.valueOf(Currency.getInstance(currency).getNumericCode())).build();
  }

  private String getAuthCode(TransactionMessage cain002Response) {
    PaymentMessage cain002 = cain002Response.getMessages().stream()
        .filter(x -> x.getType().equals(CAIN002.name()))
        .findFirst()
        .orElseThrow(() -> new CAIN002NotReceivedException("A CAIN002 was not received"));
    Object authCode = cain002.getAdditionalInfo().get(AUTHORISATION_CODE.name());
    return authCode == null ? null : authCode.toString();
  }

  private String getReasonCode(String cardAuthStatus) {
    return SUCCESS.name().equals(cardAuthStatus) ? ReasonCodeEnum._00.toString()
        : ReasonCodeEnum._05.toString();
  }

  private static BigDecimal fromString(Object value) {
    return value == null ? null : new BigDecimal(value.toString());
  }

}

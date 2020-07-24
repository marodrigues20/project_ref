package com.tenx.universalbanking.transactionmanager.converter;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import org.springframework.stereotype.Component;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;

@Component
public class CardAuthResponseBuilder {

  public CardAuthResponse buildCardAuthResponse(
      PaymentDecisionTransactionResponse paymentResponse) {

    CardAuthResponse cardAuthResponse = new CardAuthResponse();

    if (SUCCESS.name().equals(paymentResponse.getDecisionResponse())) {
      cardAuthResponse.setCardAuthStatus(SUCCESS.name());
      return cardAuthResponse;
    }

    ReasonDto reason = new ReasonDto();
    reason.setCode(paymentResponse.getDecisionReason().getCode());
    reason.setMessage(paymentResponse.getDecisionReason().getMessage());
    cardAuthResponse.setCardAuthStatus(FAILED.name());
    cardAuthResponse.setReason(reason);

    return cardAuthResponse;
  }

  public CardAuthResponse buildCardAuthResponse(
      PaymentDecisionTransactionResponse paymentResponse,
      TransactionMessage responseMessage) {
    CardAuthResponse cardAuthResponse = buildCardAuthResponse(paymentResponse);
    cardAuthResponse.setCain002Response(responseMessage);
    return cardAuthResponse;
  }

  public CardAuthResponse buildCardAuthResponse(
      TransactionMessage responseMessage) {
    CardAuthResponse cardAuthResponse = new CardAuthResponse();
    cardAuthResponse.setCain002Response(responseMessage);
    return cardAuthResponse;
  }

  public CardAuthResponse buildCardAuthResponse(LedgerPostingResponse postingResponse,
      TransactionMessage responseMessage) {
    CardAuthResponse processResponse = new CardAuthResponse();
    processResponse.setCardAuthStatus(FAILED.name());
    processResponse.setReason(postingResponse.getReason());
    processResponse.setCain002Response(responseMessage);
    return processResponse;
  }

}

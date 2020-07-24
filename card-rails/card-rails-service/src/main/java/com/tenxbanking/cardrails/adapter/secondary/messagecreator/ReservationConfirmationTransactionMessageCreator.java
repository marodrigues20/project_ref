package com.tenxbanking.cardrails.adapter.secondary.messagecreator;

import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_RESPONSE_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.AUTHORISATION_CODE;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuth;
import com.tenxbanking.cardrails.domain.model.transaction.CardTransaction;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReservationConfirmationTransactionMessageCreator {

  private static final String APPROVED = "APPR";
  private static final String DECLINED = "DECL";

  public TransactionMessage create(@NonNull final TransactionMessage cain001, @NonNull final Cain002 cain002) {
    TransactionMessage reservationMessage = new TransactionMessage();
    reservationMessage.setHeader(cain001.getHeader());

    Map<String, Object> additionalInfo = cain001.getAdditionalInfo();
    additionalInfo.put(TRANSACTION_STATUS.name(), cain002.isSuccess() ? SUCCESS.name() : FAILED.name());
    reservationMessage.setAdditionalInfo(additionalInfo);

    cain001.getMessages()
        .forEach(paymentMessage -> {
          if (paymentMessage.getType().equals(PaymentMessageTypeEnum.CAIN001.name())) {
            convertIntoCain002(paymentMessage, cain002);
          }
        });

    reservationMessage.setMessages(cain001.getMessages());

    return reservationMessage;
  }

  private PaymentMessage convertIntoCain002(PaymentMessage paymentMessage, Cain002 cain002) {
    paymentMessage.setType(PaymentMessageTypeEnum.CAIN002.name());
    Map<String, Object> message = paymentMessage.getMessage();

    message.put(TRANSACTION_RESPONSE_CODE.name(), cain002.isSuccess() ? APPROVED : DECLINED);

    if (cain002.isSuccess()) {
      message.put(AUTHORISATION_CODE.name(), cain002.getAuthCode());
    }
    return paymentMessage;
  }


}



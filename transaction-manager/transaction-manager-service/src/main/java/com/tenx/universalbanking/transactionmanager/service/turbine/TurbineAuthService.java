package com.tenx.universalbanking.transactionmanager.service.turbine;

import com.tenx.universalbanking.transactionmanager.exception.CardNotFoundException;
import com.tenx.universalbanking.transactionmanager.model.Card;
import com.tenx.universalbanking.transactionmanager.model.CardAuth;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.turbine.SchemeMessageResponse;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthAdviceMessageService;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthMessageService;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TurbineAuthService {

  private CardAuthAdviceMessageService cardAuthAdviceMessageService;
  private DebitCardProvider debitCardProvider;
  private CardAuthTransactionMessageBuilder messageBuilder;
  private CardAuthMessageService cardAuthMessageService;
  private SchemeMessageResponseBuilder schemeMessageResponseBuilder;

  public SchemeMessageResponse authorise(CardAuth cardAuth) {
    Card card = getCard(cardAuth.getCardId());
    TransactionMessage transactionMessage = messageBuilder.create(cardAuth, card, false);
    CardAuthResponse cardAuthResponse = cardAuthMessageService.processCardAuth(transactionMessage);
    return schemeMessageResponseBuilder.create(cardAuthResponse, card);
  }

  public SchemeMessageResponse advice(CardAuth cardAuth) {
    Card card = getCard(cardAuth.getCardId());
    TransactionMessage transactionMessage = messageBuilder.create(cardAuth, card, true);
    CardAuthResponse cardAuthResponse = cardAuthAdviceMessageService.processCardAuth(transactionMessage);
    return schemeMessageResponseBuilder.create(cardAuthResponse , card);
  }

  public SchemeMessageResponse reversal(CardAuth cardAuth) {
    return advice(cardAuth);
  }

  private Card getCard(String cardId) {
    return debitCardProvider.getCard(cardId)
        .orElseThrow(() -> new CardNotFoundException(String.format(
            "Debit card with Id %s is not found and returning error", cardId)));
  }

}

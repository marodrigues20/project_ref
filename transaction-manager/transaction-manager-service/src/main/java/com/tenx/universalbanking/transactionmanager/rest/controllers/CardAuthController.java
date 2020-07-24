package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.tenx.idempotency.request.annotation.Idempotent;
import com.tenx.universalbanking.transactionmanager.factory.CardAuthServiceFactory;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CardAuthController {

  private final Logger LOGGER = getLogger(CardAuthController.class);

  @Autowired
  private CardAuthServiceFactory cardAuthServiceFactory;

  @Autowired
  private HttpServletRequest request;

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    String[] allowedFields = new String[]{"TransactionMessage.header",
        "TransactionMessage.additionalInfo",
        "TransactionMessage.messages"};

    binder.setAllowedFields(allowedFields);
  }

  @Idempotent(expiry = 5)
  @RequestMapping(value = "/transaction-manager/card-auth", method = POST)
  public CardAuthResponse processPayment(@RequestBody TransactionMessage transactionMessage) {
    LOGGER.debug(
        "In CardAuthController: Processing payment with request");
    return cardAuthServiceFactory
        .getCardAuthService(transactionMessage)
        .processCardAuth(transactionMessage, request);
  }
}

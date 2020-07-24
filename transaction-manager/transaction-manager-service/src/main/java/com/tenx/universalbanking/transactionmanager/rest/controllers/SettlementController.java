package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.idempotency.request.annotation.Idempotent;
import com.tenx.universalbanking.transactionmanager.rest.responses.SettlementResponse;
import com.tenx.universalbanking.transactionmanager.service.impls.BatchSettlementProcessor;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SettlementController {

  private final Logger LOGGER = getLogger(SettlementController.class);

  @Autowired
  private BatchSettlementProcessor settlementProcessor;

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
  @PostMapping(value = "/transaction-manager/settlement")
  public SettlementResponse processSettlement(@RequestBody TransactionMessage transactionMessage) {
    LOGGER.debug(
        "In SettlementController: Processing settlement request");
    return settlementProcessor.process(transactionMessage, request);
  }
}

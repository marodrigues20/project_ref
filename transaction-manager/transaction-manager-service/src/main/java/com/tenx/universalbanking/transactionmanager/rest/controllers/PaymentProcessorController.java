package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.idempotency.request.annotation.Idempotent;
import com.tenx.universalbanking.transactionmanager.aspect.annotations.PaymentCountMetric;
import com.tenx.universalbanking.transactionmanager.constants.RestApiUrls;
import com.tenx.universalbanking.transactionmanager.factory.TransactionServiceFactory;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
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
public class PaymentProcessorController {

  private final Logger LOGGER = getLogger(PaymentProcessorController.class);

  @Autowired
  private TransactionServiceFactory transactionServiceFactory;

  @Autowired
  private HttpServletRequest httpServletRequest;

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    String[] allowedFields = new String[]{"TransactionMessage.header",
        "TransactionMessage.additionalInfo",
        "TransactionMessage.messages"};

    binder.setAllowedFields(allowedFields);
  }

  @PaymentCountMetric
  @Idempotent(expiry = 5)
  @PostMapping(value = RestApiUrls.PROCESS_PAYMENT)
  public PaymentProcessResponse processPayment(@RequestBody TransactionMessage transactionMessage) {

    LOGGER.debug(
        "In PaymentProcessorController: Processing payment with request");
    return transactionServiceFactory
        .getTransactionMessageService(transactionMessage)
        .process(transactionMessage, httpServletRequest);
  }
}

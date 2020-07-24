package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.tenx.universalbanking.transactionmanager.service.impls.AuthRetentionProcessor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ExpiredAuthController {

  private final Logger LOGGER = getLogger(ExpiredAuthController.class);

  @Autowired
  private AuthRetentionProcessor retentionProcessor;

  @RequestMapping(value = "/transaction-manager/v1/expired-auth-dropping", method = POST)
  public void processExpiredAuth() {
    LOGGER.debug(
        "In RestController: Processing expired auth drop request");
    retentionProcessor.authRetention();
  }
}

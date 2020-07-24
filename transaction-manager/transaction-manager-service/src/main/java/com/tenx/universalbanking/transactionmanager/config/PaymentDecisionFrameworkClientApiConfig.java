package com.tenx.universalbanking.transactionmanager.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.ApiClient;

@Configuration
class PaymentDecisionFrameworkClientApiConfig {

  @Value("${downstream.payment-decision.rest.url}")
  private String paymentDecisionBasePath;

  @Autowired
  private ApiClient paymentDecisionClient;

  @PostConstruct
  public void setBasePath() {
    paymentDecisionClient.setBasePath(paymentDecisionBasePath);
  }
}

package com.tenx.universalbanking.transactionmanager.config;

import com.tenx.universalbanking.transactionmanager.client.platformpaymentmanager.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
class PlatformPaymentManagerClientApiConfig {

  @Value("${downstream.platform-payment-manager.rest.url}")
  private String platformPaymentManagerBasePath;

  @Autowired
  private ApiClient platformPaymentManagerClient;

  @PostConstruct
  public void setBasePath() {
    platformPaymentManagerClient.setBasePath(platformPaymentManagerBasePath);
  }
}

package com.tenx.universalbanking.transactionmanager.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.tenx.universalbanking.transactionmanager.client.paymentproxy.ApiClient;

@Configuration
class PaymentProxyClientAPIConfig {

  @Value("${downstream.payment-proxy.rest.url}")
  private String paymentProxyBasePath;

  @Autowired
  private ApiClient paymentProxyClient;

  @PostConstruct
  public void setBasePath() {
    paymentProxyClient.setBasePath(paymentProxyBasePath);
  }
}

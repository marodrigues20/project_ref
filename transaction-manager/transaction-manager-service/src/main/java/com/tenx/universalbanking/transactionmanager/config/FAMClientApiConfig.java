package com.tenx.universalbanking.transactionmanager.config;

import com.tenx.universalbanking.fundaccountmanager.client.ApiClient;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
class FAMClientApiConfig {

  @Value("${downstream.fam.rest.url}")
  private String fundAccountManagerUrl;

  @Autowired
  private ApiClient apiClient;

  @PostConstruct
  public void setBasePath() {
    apiClient.setBasePath(fundAccountManagerUrl);
  }

}

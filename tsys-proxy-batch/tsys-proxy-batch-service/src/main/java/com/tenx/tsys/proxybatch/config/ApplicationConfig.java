package com.tenx.tsys.proxybatch.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Value("${downstream.debitcardmanager.url}")
  private String debitCardManagerClientBasePath;

  @Value("${downstream.transactionmanager.url}")
  private String transactionmanagerClientBasePath;

  @Autowired
  private com.tenx.tsys.proxybatch.client.debitcardmanager.ApiClient debitCardManager;

  @Autowired
  private com.tenx.tsys.proxybatch.client.transactionmanager.ApiClient transactionmanagerApiClient;

  @PostConstruct
  public void setBasePath() {
    debitCardManager.setBasePath(debitCardManagerClientBasePath);
    transactionmanagerApiClient.setBasePath(transactionmanagerClientBasePath);
  }

}

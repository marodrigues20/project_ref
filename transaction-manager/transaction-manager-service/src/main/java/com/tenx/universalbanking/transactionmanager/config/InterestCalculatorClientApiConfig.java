package com.tenx.universalbanking.transactionmanager.config;

import com.tenx.universalbanking.transactionmanager.client.interestcalculator.ApiClient;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
class InterestCalculatorClientApiConfig {

  @Value("${downstream.interest-calculator.rest.url}")
  private String interestCalculatorBasePath;

  @Autowired
  private ApiClient interestCalculatorClient;

  @PostConstruct
  public void setBasePath() {
    interestCalculatorClient.setBasePath(interestCalculatorBasePath);
  }
}

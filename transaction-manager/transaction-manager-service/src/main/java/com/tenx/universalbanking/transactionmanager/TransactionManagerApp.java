package com.tenx.universalbanking.transactionmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.tenx.*", "com.tenx.reconciliation.*"})
@EnableFeignClients
public class TransactionManagerApp {

  public static void main(String[] args) {
    SpringApplication.run(TransactionManagerApp.class, args);
  }

}

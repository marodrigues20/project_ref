package com.tenx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.tenx.*")
@SpringBootApplication
public class TsysProxyBatchApplication {

  public static void main(String[] args) {
    SpringApplication.run(TsysProxyBatchApplication.class, args);
  }
}

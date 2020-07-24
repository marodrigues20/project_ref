package com.tenxbanking.cardrails.adapter.secondary.messagecreator;

import org.springframework.stereotype.Service;

@Service
public class GeneratorUtil {
  private static final String PREFIX = "01";

  public String generateRandomKey() {
    return PREFIX + java.util.UUID.randomUUID().toString() + System.currentTimeMillis();
  }

}

package com.tenx.universalbanking.transactionmanager.utils;

import java.security.SecureRandom;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GeneratorUtil {
  private static final String PRIFIX = "01";
  public String generateRandomKey() {
    return PRIFIX + UUID.randomUUID().toString() + System.currentTimeMillis();
  }

  public int generate6DigitAuthCode() {
    SecureRandom random = new SecureRandom();
    return 100000 + random.nextInt(900000);
  }

}

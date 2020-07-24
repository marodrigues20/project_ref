package com.tenxbanking.cardrails.adapter.primary.idempotency;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequestHashingService {

  private static final String HASHING_ALGORITHM = "MD5";

  private final MessageDigest messageDigest;

  @Autowired
  public RequestHashingService() {
    messageDigest = initDigest();
  }

  public String hash(String object) {
    return new String(messageDigest.digest(object.getBytes()));
  }

  private MessageDigest initDigest() {
    try {
      return MessageDigest.getInstance(HASHING_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}

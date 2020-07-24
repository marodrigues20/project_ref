package com.tenxbanking.cardrails.domain.service;

import com.tenxbanking.cardrails.domain.exception.PanNotHashedException;
import java.security.MessageDigest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PanHashingService {

  private final String saltKey;

  public PanHashingService(@Value("${hashpan.saltKey}") String saltKey) {
    this.saltKey = saltKey;
  }

  public String hashCardId(@NonNull final String cardId) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
      messageDigest.update(saltKey.getBytes());
      byte[] digest = messageDigest.digest(cardId.getBytes());

      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
      }

      return sb.toString();
    } catch (Exception e) {
      log.error("Error occurred while hashing string. Exception:", e);
      throw new PanNotHashedException(e);
    }
  }

}

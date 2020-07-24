package com.tenx.universalbanking.transactionmanager.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class GeneratorUtilTest {

  private final GeneratorUtil generatorUtil = new GeneratorUtil();

  @Test
  public void generate6DigitAuthCode_Test() {
    assertEquals(String.valueOf(generatorUtil.generate6DigitAuthCode()).length(), 6);
  }

  @Test
  public void generateRandomKeyWithPrefixTest() {
    String actual = generatorUtil.generateRandomKey();
    assertTrue(actual.startsWith("01"));
  }
}
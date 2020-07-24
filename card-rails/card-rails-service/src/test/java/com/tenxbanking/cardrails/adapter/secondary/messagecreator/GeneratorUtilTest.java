package com.tenxbanking.cardrails.adapter.secondary.messagecreator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class GeneratorUtilTest {

  private final GeneratorUtil generatorUtil = new GeneratorUtil();

  @Test
  void generateRandomKey() {
    String actual = generatorUtil.generateRandomKey();
    assertTrue(actual.startsWith("01"));
  }
}
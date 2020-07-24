package com.tenxbanking.cardrails.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PanHashingServiceTest {

  private final PanHashingService panHashingService = new PanHashingService("abc");

  @Test
  void shouldReturnHashedPan() {
    String panToHash = "1234567812345678";
    String hashedPan = panHashingService.hashCardId(panToHash);

    assertThat(hashedPan).isEqualTo(
        "d4de1f550b12c130cf051163348f68ede52a06819f23338d701abcb931ee48d8b23f6c354bbb936eca7f3099d8b5793ea4e765747da013096ff823392cba4b3d");
  }

}
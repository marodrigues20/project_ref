package com.tenxbanking.cardrails.adapter.primary.idempotency;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RequestBodyHashingServiceTest {

  @Test
  void hash() {
    String randomString = "aString";
    String hash = new RequestHashingService().hash(randomString);
    String hash2 = new RequestHashingService().hash(randomString);
    assertThat(hash).isEqualTo(hash2);
  }
}
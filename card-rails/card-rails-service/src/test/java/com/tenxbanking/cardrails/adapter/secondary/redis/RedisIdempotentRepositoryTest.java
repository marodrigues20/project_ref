package com.tenxbanking.cardrails.adapter.secondary.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisIdempotentRepositoryTest {

  private static final Duration DURATION = Duration.ofDays(1);
  private static final String KEY = "test123";

  @Mock
  private ValueOperations<String, String> valueOperations;
  @Mock
  private RedisTemplate<String, String> redisTemplate;
  @Mock
  private RedisIdempotentRepository underTest;

  @BeforeEach
  void setup() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    underTest = new RedisIdempotentRepository(DURATION, redisTemplate);
  }

  @Test
  void add() {
    when(valueOperations.setIfAbsent("IDEMPOTENT_KEY:test123", KEY, DURATION)).thenReturn(true);

    boolean returned = underTest.add(KEY);

    assertThat(returned).isTrue();
    verify(valueOperations).setIfAbsent("IDEMPOTENT_KEY:test123", KEY, DURATION);
  }

  @Test
  void remove() {
    underTest.remove(KEY);

    verify(redisTemplate).delete("IDEMPOTENT_KEY:test123");
  }

}
package com.tenxbanking.cardrails.adapter.secondary.redis;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class RedisIdempotentRepository implements IdempotentRepository {

  private static final String KEY_PREFIX = "IDEMPOTENT_KEY:";

  private final Duration duration;
  private final ValueOperations<String, String> valueOperations;
  private final RedisTemplate<String, String> redisTemplate;

  @Autowired
  public RedisIdempotentRepository(
      @Value("${redis.idempotent.expire-time}") Duration duration,
      RedisTemplate<String, String> redisTemplate) {
    this.duration = duration;
    this.valueOperations = redisTemplate.opsForValue();
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean add(String key) {
    return valueOperations.setIfAbsent(createRedisKey(key), key, duration);
  }

  @Override
  public void remove(String key) {
    redisTemplate.delete(createRedisKey(key));
  }

  private String createRedisKey(String key) {
    return KEY_PREFIX + key;
  }

}

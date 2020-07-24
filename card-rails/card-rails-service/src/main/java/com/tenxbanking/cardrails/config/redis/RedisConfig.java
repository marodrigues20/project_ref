package com.tenxbanking.cardrails.config.redis;

import static java.util.Arrays.asList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(repositoryImplementationPostfix = "RedisRepository")
public class RedisConfig {

  @Bean
  public RedisCustomConversions redisCustomConversions(InstantToBytesConverter offsetToBytes,
      BytesToInstantConverter bytesToOffset) {
    return new RedisCustomConversions(asList(offsetToBytes, bytesToOffset));
  }
}

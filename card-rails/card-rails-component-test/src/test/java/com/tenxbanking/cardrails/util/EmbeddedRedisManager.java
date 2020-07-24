package com.tenxbanking.cardrails.util;

public class EmbeddedRedisManager {

  private static final EmbeddedRedisManager INSTANCE = new EmbeddedRedisManager();
  private final redis.embedded.RedisServer redisServer;

  private EmbeddedRedisManager() {
    this.redisServer = new redis.embedded.RedisServer();
    startRedis();
    addShutdownHook();
  }

  public static EmbeddedRedisManager getInstance() {
    return INSTANCE;
  }

  public void startRedis() {
    this.redisServer.start();
  }

  private void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          redisServer.stop();
        } catch (Exception e) {
          exit(e);
        }
      }
    });
  }

  private void exit(Exception e) {
    e.printStackTrace();
    System.exit(1);
  }
}

package com.tenxbanking.cardrails.adapter.secondary.redis;

public interface IdempotentRepository {

  boolean add(String key);

  void remove(String key);

}

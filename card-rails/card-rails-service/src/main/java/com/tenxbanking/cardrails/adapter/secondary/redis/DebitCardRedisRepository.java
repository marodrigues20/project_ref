package com.tenxbanking.cardrails.adapter.secondary.redis;

import com.tenxbanking.cardrails.domain.model.card.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebitCardRedisRepository extends CrudRepository<Card, String> {

}

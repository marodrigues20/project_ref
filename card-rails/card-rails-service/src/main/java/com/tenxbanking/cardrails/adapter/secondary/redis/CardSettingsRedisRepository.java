package com.tenxbanking.cardrails.adapter.secondary.redis;

import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardSettingsRedisRepository extends CrudRepository<CardSettings, String> {

}

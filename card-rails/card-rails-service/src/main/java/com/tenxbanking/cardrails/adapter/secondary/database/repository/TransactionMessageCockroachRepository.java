package com.tenxbanking.cardrails.adapter.secondary.database.repository;

import com.tenxbanking.cardrails.adapter.secondary.database.model.TransactionMessageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionMessageCockroachRepository extends JpaRepository<TransactionMessageEntity, UUID> {

}

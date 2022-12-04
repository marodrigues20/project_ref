package lbg.example.kafkasemantics.repositories;

import lbg.example.kafkasemantics.entities.PaymentBusinessEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentBusinessEventRepository extends JpaRepository<PaymentBusinessEvent, String> {


}

package com.tenxbanking.cardrails.adapter.secondary.clearing;

import com.tenxbanking.cardrails.domain.model.card.Transaction;
import com.tenxbanking.cardrails.domain.port.CardClearingService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CardClearingImpl implements CardClearingService {

  public void process(@NonNull final Transaction transaction){




  }

}

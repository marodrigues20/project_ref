package com.tenx.universalbanking.transactionmanager.reconciliation;

import static org.slf4j.LoggerFactory.getLogger;

import com.tenx.reconciliation.logger.model.ReconciliationMessage;
import com.tenx.reconciliation.logger.service.ReconciliationMessageLogger;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmanager.service.impls.FpsInMessageService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReconciliationHelper {

  private final Logger logger = getLogger(FpsInMessageService.class);

  @Autowired
  private ReconciliationMessageLogger reconciliationMessageLogger;

  public void saveReconciliationMessage(ReconciliationMessageDto reconciliationMessageDto){

    ReconciliationMessage reconciliationMessage = ReconciliationMessage.builder()
        .transactionCorrelationId(reconciliationMessageDto.getTransactionCorrelationId())
        .serviceNames(reconciliationMessageDto.getServiceName())
        .scope(reconciliationMessageDto.getScope().name())
        .event(reconciliationMessageDto.getEvent())
        .build();
    reconciliationMessageLogger.logMessage(reconciliationMessage);
    logger.info("ReconciliationMessage saved successfully In ReconciliationLog DB");
  }

}


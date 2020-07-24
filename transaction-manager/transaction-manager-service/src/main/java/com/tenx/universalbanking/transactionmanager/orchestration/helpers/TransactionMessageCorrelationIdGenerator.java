package com.tenx.universalbanking.transactionmanager.orchestration.helpers;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;

import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionMessageCorrelationIdGenerator {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private GeneratorUtil generatorUtil;

  public void addCorrelationId(TransactionMessage transactionMessage, String correlationId){
    logger.debug("Adding correlation Id: " + correlationId);
    transactionMessage.getAdditionalInfo().put(TRANSACTION_CORRELATION_ID.name(), correlationId);
  }

  public void addCorrelationId(TransactionMessage transactionMessage) {
    String correlationId = generateCorrelationId();
    //this is being done for internal reconciliation testing purpose only
    logger.info("Generated correlationId: " + correlationId);
    addCorrelationId(transactionMessage, correlationId);
  }

  private String generateCorrelationId() {
    return generatorUtil.generateRandomKey();
  }

}

package com.tenx.universalbanking.transactionmanager.model;

import com.tenx.reconciliation.logger.model.Amount;
import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReconciliationMessageDto {
  private String transactionCorrelationId;
  private Amount amount;
  private Event event;
  private TransactionMessageTypeEnum scope;
  private ServiceNames serviceName;
}

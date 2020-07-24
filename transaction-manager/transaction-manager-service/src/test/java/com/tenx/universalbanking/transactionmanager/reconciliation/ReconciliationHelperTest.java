package com.tenx.universalbanking.transactionmanager.reconciliation;

import com.tenx.reconciliation.logger.model.Event;
import com.tenx.reconciliation.logger.model.ServiceNames;
import com.tenx.reconciliation.logger.service.ReconciliationMessageLogger;
import com.tenx.universalbanking.transactionmanager.model.ReconciliationMessageDto;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReconciliationHelperTest {

  @Mock
  private ReconciliationMessageLogger reconciliationMessageLogger;

  @InjectMocks
  private ReconciliationHelper reconciliationHelper;

  @Test
  public void saveReconciliationMessage_success(){
    ReconciliationMessageDto reconciliationMessageDto = buildReconciliationMessageDto();
    reconciliationHelper.saveReconciliationMessage(reconciliationMessageDto);
  }

  @Test
  public void saveReconciliationMessage_successWithoutAmountAndCurrency(){
    ReconciliationMessageDto reconciliationMessageDto = buildReconciliationMessageDto();
    reconciliationHelper.saveReconciliationMessage(reconciliationMessageDto);
  }

  private ReconciliationMessageDto buildReconciliationMessageDto(){
    return ReconciliationMessageDto.builder()
        .event(Event.COMPLETE)
        .scope(TransactionMessageTypeEnum.BACS)
        .serviceName(ServiceNames.DATA_ANALYTICS)
        .build();
  }

}

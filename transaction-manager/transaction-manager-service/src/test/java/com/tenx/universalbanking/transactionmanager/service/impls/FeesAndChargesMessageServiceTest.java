package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.PaymentDecisionResponse.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.FEES_AND_CHARGES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.validation.MessageValidator;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeesAndChargesMessageServiceTest {

  @Mock
  private MessageServiceProcessorHelper processorHelper;

  @Mock
  private MessageSender messageSender;

  @Mock
  private MessageValidator messageValidator;

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private FeesAndChargesMessageService unit = new FeesAndChargesMessageService();

  @Test
  public void shouldHandleFeesAndChargesMessage() {
    assertEquals(FEES_AND_CHARGES, unit.getType());
  }

  @Test
  public void shouldProcessFeesAndChargesMessage() {
    //given
    String subscriptionKey = "subscriptionKey";
    TransactionMessage message = new TransactionMessage();
    when(
        processorHelper.getSubscriptionKey(message, PaymentMessageTypeEnum.FEES_AND_CHARGES))
        .thenReturn(subscriptionKey);
    //when
    PaymentProcessResponse response = unit.process(message, request);
    //then
    verify(messageValidator).validateMessage(message, PaymentMessageTypeEnum.FEES_AND_CHARGES);
    verify(processorHelper).generateTransactionAndCorrelationIds(message);
    verify(messageSender).sendPaymentMessage(subscriptionKey, message);
    //and
    assertEquals(SUCCESS.name(), response.getPaymentStatus());
  }
}
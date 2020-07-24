package com.tenx.universalbanking.transactionmanager.orchestration;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.INTEREST_ACCRUAL;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.factory.TransactionServiceFactory;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageSender;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterestAccrualMessageHandlerTest {

  private static final String TRANSACTION_ID_VALUE = "1234567";
  private static final String CORRELATION_ID_VALUE = "7654321";
  private static final ArgumentMatcher<TransactionMessage> transactionMessageArgumentMatcher = transactionMessage -> {
    assertThat(transactionMessage.getMessages()).hasSize(1);
    assertThat(transactionMessage.getMessages().get(0).getAdditionalInfo().get("TRANSACTION_ID"))
        .isEqualTo(TRANSACTION_ID_VALUE);
    assertThat(transactionMessage.getAdditionalInfo().get("TRANSACTION_CORRELATION_ID"))
        .isEqualTo(CORRELATION_ID_VALUE);
    return true;
  };

  @Mock
  private GeneratorUtil generatorUtil;

  @Mock
  private TransactionMessageSender transactionMessageSender;

  @InjectMocks
  private InterestAccrualMessageHandler interestAccrualMessageHandler;

  @Mock
  private TransactionServiceFactory factory;

  @Before
  public void setUp() {
    when(generatorUtil.generateRandomKey()).thenReturn(TRANSACTION_ID_VALUE, CORRELATION_ID_VALUE);
  }

  @Test
  public void shouldAddTransactionIdToMessage() {
    interestAccrualMessageHandler.handleMessage(getTransactionMessage());

    transactionMessageSender.send(argThat(transactionMessageArgumentMatcher));
    verify(generatorUtil, times(2)).generateRandomKey();
  }

  @Test
  public void shouldInvokeSenderOncePerMessage() {
    TransactionMessage transactionMessage = getTransactionMessage();
    interestAccrualMessageHandler.handleMessage(transactionMessage);

    verify(transactionMessageSender).send(any(TransactionMessage.class));
  }

  @Test
  public void handlesMessageOfCorrectType() {
    assertThat(interestAccrualMessageHandler.handlesMessageOfType()).isEqualTo(INTEREST_ACCRUAL);
  }

  private TransactionMessage getTransactionMessage() {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setMessages(singletonList((new PaymentMessage())));
    return transactionMessage;
  }

}
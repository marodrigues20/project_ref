package com.tenx.universalbanking.transactionmanager.orchestration;


import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.BACS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.INTEREST_ACCRUAL;
import static java.util.Collections.singletonList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tenx.universalbanking.transactionmanager.orchestration.TransactionMessageHandler;
import com.tenx.universalbanking.transactionmanager.orchestration.TransactionProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.tenx.universalbanking.transactionmanager.exception.NoValidTransactionHandlerException;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;

public class TransactionProcessorTest {

  private TransactionProcessor transactionProcessor;
  private final TransactionMessageHandler transactionMessageHandler = Mockito.mock(TransactionMessageHandler.class);

  @Before
  public void setUp() {
    given(transactionMessageHandler.handlesMessageOfType()).willReturn(INTEREST_ACCRUAL);
    transactionProcessor = new TransactionProcessor(singletonList(transactionMessageHandler));
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenAnInvalidTransactionTypeEnumIsReceived() {

    TransactionMessage transactionMessage = getTransactionMessage("invalid type");

    transactionProcessor.handle(transactionMessage);
  }

  @Test(expected = NoValidTransactionHandlerException.class)
  public void whenTransactionTypeEnumIsNotSupported() {

    TransactionMessage transactionMessage = getTransactionMessage(BACS.name());

    transactionProcessor.handle(transactionMessage);
  }

  @Test
  public void shouldSendToHandlerIfMessageTypeIsValid() {

    TransactionMessage transactionMessage = getTransactionMessage(INTEREST_ACCRUAL.name());

    transactionProcessor.handle(transactionMessage);

    verify(transactionMessageHandler).handleMessage(transactionMessage);
  }

  private TransactionMessage getTransactionMessage(String invalid_type) {
    TransactionMessage transactionMessage = new TransactionMessage();
    TransactionMessageHeader transactionMessageHeader = new TransactionMessageHeader();
    transactionMessageHeader.setType(invalid_type);
    transactionMessage.setHeader(transactionMessageHeader);
    return transactionMessage;
  }


}
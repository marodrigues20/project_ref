package com.tenx.universalbanking.transactionmanager.orchestration;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.INTEREST_APPLICATION;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.factory.TransactionServiceFactory;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.TransactionMessageSender;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.impls.InterestApplicationService;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterestApplicationMessageHandlerTest {

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

  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";

  @Mock
  private GeneratorUtil mockLionGeneratorUtils;

  @Mock
  private TransactionMessageSender mockTransactionMessageSender;

  @Mock
  private TransactionServiceFactory factory;

  @Mock
  private InterestApplicationService interestApplicationService;

  @InjectMocks
  private InterestApplicationMessageHandler interestApplicationMessageHandler;

  @Before
  public void setUp() {
    when(mockLionGeneratorUtils.generateRandomKey()).thenReturn(TRANSACTION_ID_VALUE, CORRELATION_ID_VALUE);
  }

  @Test
  public void shouldAddTransactionIdToMessage() throws Exception {
    mockLMPosting("SUCCESS");
    interestApplicationMessageHandler.handleMessage(getTransactionMessage());

    mockTransactionMessageSender.send(argThat(transactionMessageArgumentMatcher));
    verify(mockLionGeneratorUtils, times(2)).generateRandomKey();
  }

  @Test
  public void shouldInvokeSenderOncePerMessageLMSuccess() throws Exception{
    mockLMPosting("SUCCESS");
    interestApplicationMessageHandler.handleMessage(getTransactionMessage());

    verify(mockTransactionMessageSender).send(any(TransactionMessage.class));
  }

  @Test
  public void shouldInvokeSenderOncePerMessageLMFailure() throws Exception{
    mockLMPosting("FAILED");
    interestApplicationMessageHandler.handleMessage(getTransactionMessage());

    verify(mockTransactionMessageSender,times(0)).send(any(TransactionMessage.class));
  }

  @Test
  public void handlesMessageOfCorrectType() {
    assertThat(interestApplicationMessageHandler.handlesMessageOfType()).isEqualTo(INTEREST_APPLICATION);
  }

  private TransactionMessage getTransactionMessage() {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.setMessages(singletonList((new PaymentMessage())));
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(INTEREST_APPLICATION.name());
    transactionMessage.setHeader(header);
    return transactionMessage;
  }

  private void mockLMPosting(String reponse) throws Exception{
    FileReaderUtil fileReader= new FileReaderUtil();
    when(interestApplicationService.process(any(), any())).thenReturn(buildPaymentProcessResponse(reponse));
  }

  private PaymentProcessResponse buildPaymentProcessResponse(String status){
    PaymentProcessResponse response = new PaymentProcessResponse();
    response.setPaymentStatus(status);
    return response;
  }
}
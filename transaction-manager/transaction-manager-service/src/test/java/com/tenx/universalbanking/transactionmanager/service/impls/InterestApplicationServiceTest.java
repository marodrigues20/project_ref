package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.INTEREST_APPLICATION;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterestApplicationServiceTest {

  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";

  @InjectMocks
  private InterestApplicationService service;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private HttpServletRequest request;

  private final FileReaderUtil fileReader = new FileReaderUtil();

  @Test
  public void getType() throws Exception {
    assertEquals(INTEREST_APPLICATION.name(), service.getType().name());
  }

  @Test
  public void processSuccess() throws Exception {
    TransactionMessage message = buildMessage("message/interest/Interest-Application.json", TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = buildMessage(LM_POSTING_SUCCESS, LedgerPostingResponse.class);

    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    PaymentProcessResponse response = service.process(message, request);
    Assert.assertEquals("SUCCESS", response.getPaymentStatus());

  }

  @Test
  public void processFailure() throws Exception {
    TransactionMessage message = buildMessage("message/interest/Interest-Application.json", TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = buildMessage(LM_POSTING_FAILURE, LedgerPostingResponse.class);

    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    PaymentProcessResponse response = service.process(message, request);
    Assert.assertEquals("FAILED", response.getPaymentStatus());
  }

  private <T> T buildMessage(String filename, Class<T> clazz) throws IOException {
    return
        stringToJson(fileReader.getFileContent(filename), clazz);
  }


}
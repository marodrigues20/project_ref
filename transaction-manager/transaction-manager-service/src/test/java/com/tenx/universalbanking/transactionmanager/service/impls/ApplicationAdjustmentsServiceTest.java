package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.APPLICATION_ADJUSTMENTS;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.rest.client.LedgerManagerClient;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationAdjustmentsServiceTest {

  private static final String LM_POSTING_SUCCESS = "message/LM-Posting-Success.json";
  private static final String LM_POSTING_FAILURE = "message/LM-Posting-Failure.json";

  @InjectMocks
  private ApplicationAdjustmentsService service;

  @Mock
  private LedgerManagerClient lmClient;

  @Mock
  private HttpServletRequest request;

  private final FileReaderUtil fileReader = new FileReaderUtil();

  @Test
  public void getType() throws Exception {
    assertEquals(APPLICATION_ADJUSTMENTS.name(), service.getType().name());
  }

  @Test
  public void processSuccess() throws Exception {
    TransactionMessage message = buildMessage("message/interest/Application-Adjustments.json",
        TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = buildMessage(LM_POSTING_SUCCESS,
        LedgerPostingResponse.class);

    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    PaymentProcessResponse response = service.process(message, request);
    assertEquals("SUCCESS", response.getPaymentStatus());

  }

  @Test
  public void processFailure() throws Exception {
    TransactionMessage message = buildMessage("message/interest/Application-Adjustments.json",
        TransactionMessage.class);
    LedgerPostingResponse lmPostingResponse = buildMessage(LM_POSTING_FAILURE,
        LedgerPostingResponse.class);

    when(lmClient.postTransactionToLedger(any())).thenReturn(lmPostingResponse);
    PaymentProcessResponse response = service.process(message, request);
    assertEquals("FAILED", response.getPaymentStatus());
  }

  private <T> T buildMessage(String filename, Class<T> clazz) throws IOException {
    return
        stringToJson(fileReader.getFileContent(filename), clazz);
  }


}
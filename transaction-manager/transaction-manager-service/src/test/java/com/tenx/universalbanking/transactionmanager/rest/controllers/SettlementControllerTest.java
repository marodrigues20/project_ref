package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenx.universalbanking.transactionmanager.rest.responses.SettlementResponse;
import com.tenx.universalbanking.transactionmanager.service.impls.BatchSettlementProcessor;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class SettlementControllerTest {

  private MockMvc mvc;

  @Mock
  private BatchSettlementProcessor batchSettlementProcessor;

  private FileReaderUtil fileReader;

  @InjectMocks
  private SettlementController settlementController;

  @Mock
  private HttpServletRequest request;

  @Before
  public void init() {
    mvc = MockMvcBuilders.standaloneSetup(settlementController)
        .build();
    fileReader = new FileReaderUtil();
  }

  @Test
  public void shouldProcessCain003Message() throws Exception {
    when(batchSettlementProcessor.process(any(TransactionMessage.class), eq(request)))
        .thenReturn(new SettlementResponse(SUCCESS.name()));
    whenMessageProcessed("message/settlement/cain003.json",
        getFileContentAsString("message/settlement/cain003Response.json"));
  }

  @Test
  public void shouldProcessCain005() throws Exception {
    when(batchSettlementProcessor.process(any(TransactionMessage.class), eq(request)))
        .thenReturn(new SettlementResponse(SUCCESS.name()));
    whenMessageProcessed("message/settlement/cain005.json",
        getFileContentAsString("message/settlement/cain005Response.json"));
  }

  private void whenMessageProcessed(String fileName, String jsonResponse) throws Exception {
    String message = getFileContentAsString(fileName);

    mvc.perform(post("/transaction-manager/settlement")
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .content(message))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponse, true));
  }

  private String getFileContentAsString(String requestBody) throws IOException {
    return fileReader.getFileContent(requestBody);
  }

}
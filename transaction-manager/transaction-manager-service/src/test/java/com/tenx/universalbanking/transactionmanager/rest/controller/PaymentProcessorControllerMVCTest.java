package com.tenx.universalbanking.transactionmanager.rest.controller;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.tenx.universalbanking.transactionmanager.factory.TransactionServiceFactory;
import com.tenx.universalbanking.transactionmanager.rest.controllers.PaymentProcessorController;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthMessageService;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class PaymentProcessorControllerMVCTest {

  private MockMvc mvc;

  @Mock
  private TransactionServiceFactory factory;

  @Mock
  private CardAuthMessageService cardAuthMessageService;

  @Mock
  private FileReaderUtil fileReader;

  @InjectMocks
  private PaymentProcessorController paymentProcessorController;

  @Mock
  private HttpServletRequest request;

  @Before
  public void init() {
    mvc = MockMvcBuilders.standaloneSetup(paymentProcessorController)
        .build();
    fileReader = new FileReaderUtil();
  }

  @Test
  public void shouldProcessCain001Message() throws Exception {
    when(cardAuthMessageService.process(any(TransactionMessage.class), eq(request)))
        .thenReturn(new PaymentProcessResponse(SUCCESS.name()));
    whenMessageProcessed(
        "{\"paymentStatus\":\"SUCCESS\"}");
  }

  @Test
  public void shouldProcessCain001MessageFailCase() throws Exception {
    when(cardAuthMessageService.process(any(TransactionMessage.class), eq(request)))
        .thenReturn(new PaymentProcessResponse(FAILED.name()));
    whenMessageProcessed(
        "{\"paymentStatus\":\"FAILED\"}");
  }

  private void whenMessageProcessed(String jsonResponse) throws Exception {
    String message = getFileContentAsString("message/cain001TransactionMesage.json");
    when(factory.getTransactionMessageService(any(TransactionMessage.class)))
        .thenReturn(cardAuthMessageService);

    mvc.perform(post("/transaction-manager/process-payment")
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .content(message))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponse, true));
  }

  private String getFileContentAsString(String requestBody) throws IOException {
    return fileReader.getFileContent(requestBody);
  }
}
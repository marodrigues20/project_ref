package com.tenx.universalbanking.transactionmanager.rest.controllers;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenx.universalbanking.transactionmanager.factory.CardAuthServiceFactory;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthAdviceMessageService;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthMessageService;
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
public class CardAuthControllerTest {

  private MockMvc mvc;

  @Mock
  private CardAuthServiceFactory cardAuthServiceFactory;

  @Mock
  private CardAuthMessageService cardAuthMessageService;

  @Mock
  private CardAuthAdviceMessageService cardAuthAdviceMessageService;

  private FileReaderUtil fileReader;

  @InjectMocks
  private CardAuthController cardAuthController;

  @Mock
  private HttpServletRequest request;

  @Before
  public void init() {
    mvc = MockMvcBuilders.standaloneSetup(cardAuthController)
        .build();
    fileReader = new FileReaderUtil();
  }

  @Test
  public void shouldProcessCain001MessageForCardAuth() throws Exception {
    when(cardAuthServiceFactory.getCardAuthService(any(TransactionMessage.class)))
        .thenReturn(cardAuthMessageService);

    when(cardAuthMessageService.processCardAuth(any(TransactionMessage.class), eq(request)))
        .thenReturn(new CardAuthResponse(SUCCESS.name(), new TransactionMessage()));
    whenMessageProcessed("message/cardauth/cain001TransactionMessageWithToken.json",
        getFileContentAsString("message/cardauth/cardAuthSuccessResponse.json"));
  }

  @Test
  public void shouldProcessCain001MessageFailCaseForCardAuth() throws Exception {
    when(cardAuthServiceFactory.getCardAuthService(any(TransactionMessage.class)))
        .thenReturn(cardAuthMessageService);

    when(cardAuthMessageService.processCardAuth(any(TransactionMessage.class), eq(request)))
        .thenReturn(new CardAuthResponse(FAILED.name(),  new TransactionMessage()));
    whenMessageProcessed("message/cardauth/cain001TransactionMessageWithToken.json",
        getFileContentAsString("message/cardauth/cardAuthFailureResponse.json"));
  }

  @Test
  public void shouldProcessCain001MessageForCardAuthAdviceMessage() throws Exception {
    when(cardAuthServiceFactory.getCardAuthService(any(TransactionMessage.class)))
        .thenReturn(cardAuthMessageService);

    when(cardAuthMessageService.processCardAuth(any(TransactionMessage.class), eq(request)))
        .thenReturn(new CardAuthResponse(SUCCESS.name(), new TransactionMessage()));
    whenMessageProcessed("message/cardauthadvice/cain001TransactionMessageWithToken.json",
        getFileContentAsString("message/cardauthadvice/cardAuthSuccessResponse.json"));
  }

  @Test
  public void shouldProcessCain001MessageFailCaseForCardAuthAdviceMessage() throws Exception {
    when(cardAuthServiceFactory.getCardAuthService(any(TransactionMessage.class)))
        .thenReturn(cardAuthMessageService);

    when(cardAuthMessageService.processCardAuth(any(TransactionMessage.class), eq(request)))
        .thenReturn(new CardAuthResponse(FAILED.name(),  new TransactionMessage()));
    whenMessageProcessed("message/cardauthadvice/cain001TransactionMessageWithToken.json",
        getFileContentAsString("message/cardauthadvice/cardAuthFailureResponse.json"));
  }

  private void whenMessageProcessed(String fileName, String jsonResponse) throws Exception {
    String message = getFileContentAsString(fileName);

    mvc.perform(post("/transaction-manager/card-auth")
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .content(message))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonResponse, true));
  }

  private String getFileContentAsString(String requestBody) throws IOException {
    return fileReader.getFileContent(requestBody);
  }
}
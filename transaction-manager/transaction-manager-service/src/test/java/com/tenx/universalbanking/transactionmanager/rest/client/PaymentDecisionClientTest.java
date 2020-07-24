package com.tenx.universalbanking.transactionmanager.rest.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.api.PaymentDecisionControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.TransactionMessage;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.JsonUtils;
import com.tenx.validationlib.response.Error;
import com.tenx.validationlib.response.Errors;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class PaymentDecisionClientTest {

  @Mock
  private PaymentDecisionControllerApi controllerApi;

  @Mock
  private PDFTransactionMessageMapper mapper;

  @InjectMocks
  PaymentDecisionClient client;

  @Test
  public void shouldGetPaymentDecision() {
    TransactionMessage clientTransactionMessage = new TransactionMessage();
    PaymentDecisionTransactionResponse response = new PaymentDecisionTransactionResponse();
    com.tenx.universalbanking.transactionmessage.TransactionMessage originalMessage = new com.tenx.universalbanking.transactionmessage.TransactionMessage();
    when(mapper.toClientTransactionMessage(originalMessage)).thenReturn(clientTransactionMessage);
    when(controllerApi.makePaymentDecision(clientTransactionMessage))
        .thenReturn(new PaymentDecisionTransactionResponse());
    PaymentDecisionTransactionResponse expected = client.getPaymentDecision(originalMessage);
    assertThat(expected).isEqualTo(response);
  }

  @Test
  public void shouldGetPaymentDecisionShouldThrowHttpServerErrorException() {
    TransactionMessage clientTransactionMessage = new TransactionMessage();
    com.tenx.universalbanking.transactionmessage.TransactionMessage originalMessage = new com.tenx.universalbanking.transactionmessage.TransactionMessage();
    when(mapper.toClientTransactionMessage(originalMessage)).thenReturn(clientTransactionMessage);
    when(controllerApi.makePaymentDecision(clientTransactionMessage))
        .thenThrow(new HttpServerErrorException(
            HttpStatus.BAD_GATEWAY));
    assertThrows(HttpServerErrorException.class, () -> {
      client.getPaymentDecision(originalMessage);
    });
  }

  @Test
  public void shouldGetPaymentDecisionShouldThrowHttpServerErrorExceptionWithBody() {
    Errors errors = new Errors();
    errors.setErrors(Collections.singletonList(new Error()));
    String jsonString = JsonUtils.jsonToString(errors);
    TransactionMessage clientTransactionMessage = new TransactionMessage();
    com.tenx.universalbanking.transactionmessage.TransactionMessage originalMessage = new com.tenx.universalbanking.transactionmessage.TransactionMessage();
    when(mapper.toClientTransactionMessage(originalMessage)).thenReturn(clientTransactionMessage);
    when(controllerApi.makePaymentDecision(clientTransactionMessage))
        .thenThrow(new HttpServerErrorException(
            HttpStatus.BAD_GATEWAY, "BAD_GATEWAY",
            jsonString.getBytes(),null));
    assertThrows(HttpServerErrorException.class, () -> {
      client.getPaymentDecision(originalMessage);
    });
  }

  @Test
  public void shouldGetPaymentDecisionShouldThrowHttpServerErrorExceptionAndEncounterParsingError() {
    Errors errors = new Errors();
    errors.setErrors(Collections.singletonList(new Error()));
    TransactionMessage clientTransactionMessage = new TransactionMessage();
    com.tenx.universalbanking.transactionmessage.TransactionMessage originalMessage = new com.tenx.universalbanking.transactionmessage.TransactionMessage();
    when(mapper.toClientTransactionMessage(originalMessage)).thenReturn(clientTransactionMessage);
    when(controllerApi.makePaymentDecision(clientTransactionMessage))
        .thenThrow(new HttpServerErrorException(
            HttpStatus.BAD_GATEWAY, "BAD_GATEWAY",
            errors.toString().getBytes(),null));
    assertThrows(HttpServerErrorException.class, () -> {
      client.getPaymentDecision(originalMessage);
    });
  }

  @Test
  public void shouldGetPaymentDecisionShouldThorwException() {
    TransactionMessage clientTransactionMessage = new TransactionMessage();
    com.tenx.universalbanking.transactionmessage.TransactionMessage originalMessage = new com.tenx.universalbanking.transactionmessage.TransactionMessage();
    when(mapper.toClientTransactionMessage(originalMessage)).thenReturn(clientTransactionMessage);
    when(controllerApi.makePaymentDecision(clientTransactionMessage))
        .thenThrow(new RuntimeException());
    assertThrows(RuntimeException.class, () -> {
      client.getPaymentDecision(originalMessage);
    });
  }

}
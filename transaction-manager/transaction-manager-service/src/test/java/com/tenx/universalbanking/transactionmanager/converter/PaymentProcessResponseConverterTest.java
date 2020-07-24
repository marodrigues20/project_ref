package com.tenx.universalbanking.transactionmanager.converter;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionReason.GENERIC_FAILURE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class PaymentProcessResponseConverterTest {

  @InjectMocks
  private PaymentProcessResponseConverter responseConverter;

  private PaymentDecisionTransactionResponse paymentResponse = new PaymentDecisionTransactionResponse();
  TransactionMessage transactionMessage = new TransactionMessage();

  @Test
  public void shouldGeneratePaymentResponseWhenStatusSuccess() {
    paymentResponse.setDecisionResponse(SUCCESS.name());
    PaymentProcessResponse response = responseConverter
        .buildPaymentProcessResponse(paymentResponse);
    assertEquals(SUCCESS.name(), response.getPaymentStatus());
  }

  @Test
  public void shouldGeneratePaymentResponseWithResponse() {
    paymentResponse.setDecisionResponse(SUCCESS.name());
    PaymentProcessResponse response = responseConverter
        .buildPaymentProcessResponse(paymentResponse, transactionMessage);
    assertEquals(transactionMessage, response.getTransactionMessage());
  }

  @Test
  public void shouldGeneratePaymentResponseWhenStatusFailure() {
    paymentResponse.setDecisionResponse(FAILED.name());
    PaymentProcessResponse response = responseConverter
        .buildPaymentProcessResponse(paymentResponse);
    assertEquals(FAILED.name(), response.getPaymentStatus());
  }

  @Test
  public void shouldGeneratePaymentFailureResponseWithErrorCode() {
    paymentResponse.setDecisionResponse(FAILED.name());
    PaymentProcessResponse response = responseConverter
        .buildPaymentProcessResponse(paymentResponse);
    assertEquals(GENERIC_FAILURE.getFailureCode(), response.getReason().getCode());
  }

  @Test
  public void shouldGeneratePaymentFailureResponseWithErrorMessage() {
    paymentResponse.setDecisionResponse(FAILED.name());
    PaymentProcessResponse response = responseConverter
        .buildPaymentProcessResponse(paymentResponse);
    assertEquals(GENERIC_FAILURE.getFailureMessage(), response.getReason().getMessage());
  }

  @Test
  public void buildPaymentProcessResponseTest() {
    LedgerPostingResponse ledgerPostingResponse = new LedgerPostingResponse();
    ReasonDto reasonDto = new ReasonDto();
    ledgerPostingResponse.setReason(reasonDto);
    paymentResponse.setDecisionResponse(FAILED.name());
    PaymentProcessResponse response = responseConverter
        .buildPaymentProcessResponse(ledgerPostingResponse, transactionMessage);
    assertEquals(reasonDto,response.getReason());
    assertEquals(FAILED.name(), response.getPaymentStatus());
  }

}
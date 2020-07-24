package com.tenx.universalbanking.transactionmanager.converter;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionReasonDTO;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.CardAuthResponse;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class CardAuthResponseBuilderTest {

  private static final int UNSUPPORTED_SALES_CHANNEL_CODE = 8013;
  private static final String UNSUPPORTED_SALES_CHANNEL_MESSAGE = "Sales Channel not supported";

  @InjectMocks
  private CardAuthResponseBuilder responseConverter;

  private PaymentDecisionTransactionResponse paymentResponse = new PaymentDecisionTransactionResponse();
  private TransactionMessage transactionMessage = new TransactionMessage();

  @Test
  public void shouldGeneratePaymentResponseWhenStatusSuccess() {
    paymentResponse.setDecisionResponse(SUCCESS.name());
    CardAuthResponse response = responseConverter
        .buildCardAuthResponse(paymentResponse);
    assertEquals(SUCCESS.name(), response.getCardAuthStatus());
  }

  @Test
  public void buildCardAuthResponseTestWithTransactionMessage() {
    CardAuthResponse response = responseConverter
        .buildCardAuthResponse(transactionMessage);
    assertEquals(transactionMessage, response.getCain002Response());
  }

  @Test
  public void buildCardAuthResponseTestWithTransactionMessageAndLedgerPostingResponse() {
    LedgerPostingResponse ledgerPostingResponse = new LedgerPostingResponse();
    ReasonDto reasonDto = new ReasonDto();
    ledgerPostingResponse.setReason(reasonDto);
    CardAuthResponse response = responseConverter
        .buildCardAuthResponse(ledgerPostingResponse, transactionMessage);
    assertEquals(transactionMessage, response.getCain002Response());
    assertEquals(FAILED.name(), response.getCardAuthStatus());
    assertEquals(reasonDto, response.getReason());
  }

  @Test
  public void shouldGenerateCardAUthResponseWithToken() {
    TransactionMessage actual = new TransactionMessage();
    paymentResponse.setDecisionResponse(SUCCESS.name());
    CardAuthResponse response = responseConverter
        .buildCardAuthResponse(paymentResponse, actual);
    assertEquals(response.getCain002Response(), actual);
  }

  @Test
  public void shouldGeneratePaymentResponseWhenStatusFailure() {
    paymentResponse.setDecisionReason(getFailedDecisionReason());
    paymentResponse.setDecisionResponse(FAILED.name());
    CardAuthResponse response = responseConverter
        .buildCardAuthResponse(paymentResponse);
    assertEquals(UNSUPPORTED_SALES_CHANNEL_CODE, response.getReason().getCode());
    assertEquals(UNSUPPORTED_SALES_CHANNEL_MESSAGE, response.getReason().getMessage());
    assertEquals(FAILED.name(), response.getCardAuthStatus());
  }

  @Test
  public void shouldGeneratePaymentFailureResponseWithErrorCode() {
    paymentResponse.setDecisionReason(getFailedDecisionReason());
    paymentResponse.setDecisionResponse(FAILED.name());
    CardAuthResponse response = responseConverter
        .buildCardAuthResponse(paymentResponse);
    assertEquals(UNSUPPORTED_SALES_CHANNEL_CODE, response.getReason().getCode());
  }

  @Test
  public void shouldGeneratePaymentFailureResponseWithErrorMessage() {
    paymentResponse.setDecisionReason(getFailedDecisionReason());
    paymentResponse.setDecisionResponse(FAILED.name());
    CardAuthResponse response = responseConverter
        .buildCardAuthResponse(paymentResponse);
    assertEquals(UNSUPPORTED_SALES_CHANNEL_MESSAGE, response.getReason().getMessage());
  }

  private PaymentDecisionReasonDTO getFailedDecisionReason() {
    PaymentDecisionReasonDTO decisionReasonDTO = new PaymentDecisionReasonDTO();
    decisionReasonDTO.setCode(UNSUPPORTED_SALES_CHANNEL_CODE);
    decisionReasonDTO.setMessage(UNSUPPORTED_SALES_CHANNEL_MESSAGE);
    return decisionReasonDTO;
  }

}
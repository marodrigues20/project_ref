package com.tenx.universalbanking.transactionmanager.converter;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentStatusEnum.SUCCESS;

import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import org.springframework.stereotype.Component;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.enums.TransactionReason;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.PaymentProcessResponse;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;

@Component
public class PaymentProcessResponseConverter {

  public PaymentProcessResponse buildPaymentProcessResponse(PaymentDecisionTransactionResponse paymentResponse) {

    PaymentProcessResponse processResponse = new PaymentProcessResponse();

    if (SUCCESS.name().equals(paymentResponse.getDecisionResponse())) {
      processResponse.setPaymentStatus(SUCCESS.name());
      return processResponse;
    }

    ReasonDto reason = new ReasonDto();
    reason.setCode(TransactionReason.GENERIC_FAILURE.getFailureCode());
    reason.setMessage(TransactionReason.GENERIC_FAILURE.getFailureMessage());
    processResponse.setPaymentStatus(FAILED.name());
    processResponse.setReason(reason);

    return processResponse;
  }

  public PaymentProcessResponse buildPaymentProcessResponse(PaymentDecisionTransactionResponse paymentResponse,
      TransactionMessage responseMessage) {

    PaymentProcessResponse processResponse = buildPaymentProcessResponse(paymentResponse);
    processResponse.setTransactionMessage(responseMessage);
    return processResponse;
  }

  public PaymentProcessResponse buildPaymentProcessResponse(LedgerPostingResponse postingResponse,
      TransactionMessage responseMessage) {
    PaymentProcessResponse processResponse = new PaymentProcessResponse();
    processResponse.setPaymentStatus(FAILED.name());
    processResponse.setReason(postingResponse.getReason());
    processResponse.setTransactionMessage(responseMessage);
    return processResponse;
  }

}

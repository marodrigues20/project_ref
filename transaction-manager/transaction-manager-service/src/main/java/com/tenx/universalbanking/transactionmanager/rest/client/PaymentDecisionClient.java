package com.tenx.universalbanking.transactionmanager.rest.client;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.api.PaymentDecisionControllerApi;
import com.tenx.universalbanking.transactionmanager.client.paymentdecisionframework.model.PaymentDecisionTransactionResponse;
import com.tenx.universalbanking.transactionmanager.service.mapper.PDFTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.JsonUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.validationlib.response.Errors;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

@Component
public class PaymentDecisionClient {

  private final Logger logger = getLogger(PaymentDecisionClient.class);

  @Autowired
  private PaymentDecisionControllerApi paymentDecisionControllerApi;

  @Autowired
  private PDFTransactionMessageMapper transactionMessageMapper;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public PaymentDecisionTransactionResponse getPaymentDecision(
      TransactionMessage transactionMessage) {
    try {

      return paymentDecisionControllerApi
          .makePaymentDecision(
              transactionMessageMapper.toClientTransactionMessage(transactionMessage));
    } catch (HttpServerErrorException serverExp) {
      logger.error(
          "Exception occurred while payment decision operation on PDF.", serverExp);
      if (!serverExp.getResponseBodyAsString().isEmpty()) {
        try {
          Errors errors = objectMapper.readValue(serverExp.getResponseBodyAsString(), Errors.class);
          logger.error(
              "PDF error: {}.", JsonUtils.toJson(errors));
        } catch (Exception e) {
          logger.error("Couldn't parse PDF error. ", e);
        }
      }
      throw serverExp;
    } catch (Exception ex) {
      logger.error(
          "Exception occurred while payment decision operation on PDF.", ex);
      throw ex;
    }
  }
}

package com.tenx.tsys.proxybatch.service;

import com.tenx.tsys.proxybatch.client.debitcardmanager.api.DebitCardControllerApi;
import com.tenx.tsys.proxybatch.client.debitcardmanager.model.DebitCardResponse;
import com.tenx.tsys.proxybatch.client.transactionmanager.api.SettlementControllerApi;
import com.tenx.tsys.proxybatch.client.transactionmanager.model.TransactionMessage;
import com.tenx.tsys.proxybatch.dto.request.SettlementRequestDto;
import com.tenx.tsys.proxybatch.service.cain003.Cain003PaymentService;
import com.tenx.tsys.proxybatch.service.cain005.Cain005PaymentService;
import com.tenx.tsys.proxybatch.utils.StringUtils;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class TsysProxyBatchService {

  @Autowired
  private DebitCardControllerApi debitCardControllerApi;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private Cain003PaymentService cain003PaymentService;

  @Autowired
  private Cain005PaymentService cain005PaymentService;

  @Autowired
  private SettlementControllerApi settlementControllerApi;

  private CainPaymentService cainPaymentService;

  @Value("${indicator.debit}")
  private String debitIndicator;

  @Value("${indicator.credit}")
  private String creditIndicator;

  @Value("${indicator.position}")
  private String creditDebitIndicatorPosition;

  @Value("${settlement.request.card.token.start}")
  private String settlementRequestCardTokenStart;

  @Value("${settlement.request.card.token.end}")
  private String settlementRequestCardTokenEnd;


  private Logger logger = LoggerFactory.getLogger(getClass());

  public void findSubscriptionKey(SettlementRequestDto settlementRequestDto)
      throws Exception {

    //Start-end position is less by 1 digit as we start from 0th index
    if (settlementRequestDto.getSettlementRequest().length() > Integer
        .parseInt(settlementRequestCardTokenEnd)) {
      String cardToken = settlementRequestDto.getSettlementRequest()
          .substring(Integer.parseInt(settlementRequestCardTokenStart) - 1,
              Integer.parseInt(settlementRequestCardTokenEnd));
      String cardTokenTrimmed = cardToken.trim();
      String cardTokenTrimmedMasked = StringUtils.maskMessage(cardTokenTrimmed);

      logger.info("settlement request processing for card token: {}", cardTokenTrimmedMasked);

      DebitCardResponse debitCardResponse = new DebitCardResponse();
      try {
        debitCardResponse = debitCardControllerApi
            .getCardByToken(cardTokenTrimmed);
      } catch (HttpClientErrorException e) {
        if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
          messageSender
              .sendErrorMessage("subscription key not found for token: " + cardTokenTrimmed);
          logger.error("subscription key not found for token: {}", cardTokenTrimmedMasked);
        }
      } catch (Exception e) {
        logger.error("error while calling debit card manager {}", e.getMessage());
      }
      if (null != debitCardResponse.getSubscriptionKey()) {
        try {
          makeDebitPaymentProcessWithCainMessage(settlementRequestDto.getSettlementRequest(),
              debitCardResponse);
        } catch (Exception e) {
          logger.error("error while calling transaction manager {}", e.getMessage());
        }
      }

    } else {
      logger.warn(
          "Invalid settlement request record length: %s",
          settlementRequestDto.getSettlementRequest()
              .length());
    }
  }

  public void makeDebitPaymentProcessWithCainMessage(String message,
      DebitCardResponse debitCardResponse) {
    if (message.length() > Integer.parseInt(creditDebitIndicatorPosition)) {
      String creditDebitIndicator = message
          .substring(Integer.parseInt(creditDebitIndicatorPosition) - 1,
              Integer.parseInt(creditDebitIndicatorPosition));
      if (message.length() == 742) {
        if (creditDebitIndicator.equals(debitIndicator)) {
          cainPaymentService = cain003PaymentService;
          message = message + "N";
        } else if (creditDebitIndicator.equals(creditIndicator)) {
          cainPaymentService = cain005PaymentService;
          message = message + "Y";
        }
      } else {
        if (creditDebitIndicator.equals(debitIndicator)) {
          cainPaymentService = cain003PaymentService;
        } else if (creditDebitIndicator.equals(creditIndicator)) {
          cainPaymentService = cain005PaymentService;
        }
      }
      TransactionMessage transactionMessage = null;
      try {
        transactionMessage = cainPaymentService
            .buildMessage(message, debitCardResponse);
      } catch (ParseException e) {
        logger.error("Error while parsing cain message: {}", e.getMessage());
      }
      settlementControllerApi.processSettlementUsingPOST(transactionMessage);
    } else {
      logger.warn(
          "Invalid settlement request record length: {}", message
              .length());
    }
  }
}

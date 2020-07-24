package com.tenx.universalbanking.transactionmanager.rest.client;

import static com.tenx.universalbanking.transactionmanager.constants.TransactionManagerExceptionCodes.LM_POSTING_FAILED;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVICE_UNAVAILABLE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.api.LedgerManagerControllerApi;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.api.ProcessPaymentsControllerApi;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.BalanceDto;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.BalancesListResponse;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.BalancesResponse;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage;
import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.rest.dto.ReasonDto;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.service.mapper.LedgerManagerTransactionMessageMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@Component
public class LedgerManagerClient {

  private final Logger logger = getLogger(LedgerManagerClient.class);

  @Autowired
  private ProcessPaymentsControllerApi ledgerPaymentController;

  @Autowired
  private LedgerManagerControllerApi ledgerManagerController;

  @Autowired
  private LedgerManagerTransactionMessageMapper mapper;

  @Autowired
  private TMExceptionBuilder exceptionBuilder;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private void  processPayments(
      com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage){
    logger.debug("Attempting to post transaction on ledgerPaymentController");
    TransactionMessage lmTxnMsg = mapper.toLMTransactionMessage(transactionMessage);
    logger.info("Sending transaction to LM with TRANSACTION_CORRELATION_ID:{}",
            transactionMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()));
    BalancesResponse balancesResponse = ledgerPaymentController.processPaymentsUsingPOST(lmTxnMsg);
    if(balancesResponse != null) {
      logger.info("Response received from LM:{}", balancesResponse.getData());
    }
  }

  public LedgerPostingResponse postTransactionToLedger(
      com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage,
      boolean reversal) {
    LedgerPostingResponse response = new LedgerPostingResponse();
    try {
      if (!reversal) {
        // For reversal, dont call the TM Posting end point.
        processPayments(transactionMessage);
      }
      response.setPostingSuccess(true);
      logger.debug(
          "Post transaction on ledger is successful (For reversal, pls ignore this log message).");
    } catch (HttpServerErrorException serverExp) {
      response.setPostingSuccess(false);
      logger.error(
          "Exception occurred while post transaction operation on Ledger Manager.", serverExp);
      if (!serverExp.getResponseBodyAsString().isEmpty()) {
        logger.error(
            "Ledger Manager error: {}.", serverExp.getResponseBodyAsString());
      }
      if (serverExp.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
        throw exceptionBuilder.buildFPSOutTransactionManagerException(
            INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getStatusCode(),
            INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
      throw exceptionBuilder
          .buildFPSOutTransactionManagerException(INTERNAL_SERVICE_UNAVAILABLE.getStatusCode(),
              INTERNAL_SERVICE_UNAVAILABLE.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception ex) {
      logger.error(
          "Exception occurred while post transaction operation on Ledger Manager.", ex);
      ReasonDto failureReason = new ReasonDto(LM_POSTING_FAILED, ex.getMessage());
      response.setPostingSuccess(false);
      response.setReason(failureReason);
    }
    return response;
  }

  public LedgerPostingResponse postTransactionToLedger(com.tenx.universalbanking.transactionmessage.TransactionMessage transactionMessage) {
    return postTransactionToLedger(transactionMessage, false);
  }

  public BigDecimal getAvailableBalanceFromLedger(String subscriptionKey) throws InvalidTransactionMessageException {

    BigDecimal balanceAmt =  BigDecimal.ZERO;
    try {
      BalancesListResponse response = ledgerManagerController.getBalancesUsingGET(subscriptionKey);

      List<BalanceDto> balances = response.getData().get(subscriptionKey);
      Optional<BalanceDto> interimAvailable = balances.stream()
          .filter(balance -> balance.getType().equals("InterimAvailable")
              && balance.getAccountId().equals(subscriptionKey)).findFirst();

      if (interimAvailable.isPresent()
          && interimAvailable.get().getAmount() != null) {
        balanceAmt = interimAvailable.get().getAmount().getValue();
      } else {
        String lmError = "Unable to fetch available balance from ledgerManager";
        logger.error(lmError);
        logger.error(
            "Ledger manager api {} , Subscription key {}",
            "/ledger-manager/v2/accounts/{subscriptionKeys}/balances",
            subscriptionKey);
        throw new InvalidTransactionMessageException(lmError);
      }
    } catch(RestClientException ex) {
      logger.error(
          "Ledger manager api failed {} , Subscription key {}",
          "/ledger-manager/v2/accounts/{subscriptionKeys}/balances",
          subscriptionKey);
      logger.error("Exception while fetching balance amount from ledgerManagerController:", ex);
      throw new InvalidTransactionMessageException(ex.getMessage());
    }

    return balanceAmt;
  }
}

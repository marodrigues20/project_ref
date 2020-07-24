package com.tenx.universalbanking.transactionmanager.rest.client;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionManagerExceptions.INTERNAL_SERVICE_UNAVAILABLE;
import static com.tenx.universalbanking.transactionmanager.utils.JsonUtils.stringToJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.client.ledgermanager.api.LedgerManagerControllerApi;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.api.ProcessPaymentsControllerApi;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.BalancesListResponse;
import com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.BalancesResponse;
import com.tenx.universalbanking.transactionmanager.exception.FPSOutTransactionManagerException;
import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmanager.exception.builder.TMExceptionBuilder;
import com.tenx.universalbanking.transactionmanager.rest.responses.LedgerPostingResponse;
import com.tenx.universalbanking.transactionmanager.service.mapper.LedgerManagerTransactionMessageMapper;
import com.tenx.universalbanking.transactionmanager.utils.FileReaderUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class LedgerManagerClientTest {

  @Mock
  private LedgerManagerControllerApi controllerApi;

  @Mock
  private ProcessPaymentsControllerApi ledgerPaymentController;

  @Mock
  private LedgerManagerTransactionMessageMapper mapper;

  @Mock
  private TMExceptionBuilder exceptionBuilder;

  @InjectMocks
  private LedgerManagerClient client;

  private final FileReaderUtil fileReader = new FileReaderUtil();

  private static final String LM_GET_ACCOUNT_BALANCES = "message/cardauthadvice/LMGetAccountBalances.json";
  private static final String LM_GET_ACCOUNT_BALANCES_INTERIM_NOT_AVAILABLE = "message/cardauthadvice/LMGetAccountBalancesInterimNotAvailable.json";
  private String TRANSACTION_MESSAGE = "message/FPSOutTransactionMessagePAIN002Request.json";

  @Test
  public void shouldGetAvailableBalance() throws IOException {
    BalancesListResponse response = stringToJson(fileReader.getFileContent(LM_GET_ACCOUNT_BALANCES),
        BalancesListResponse.class);
    UUID subKey = UUID.fromString("6b4a7699-d3cc-4687-9517-2b2153872654");
    when(controllerApi.getBalancesUsingGET(subKey.toString())).thenReturn(response);
    BigDecimal availableBalance = client.getAvailableBalanceFromLedger(subKey.toString());
    assertEquals(9957.828, availableBalance.doubleValue(), 0.000);
  }

  @Test
  public void shouldThrowException() {
    UUID subKey = UUID.fromString("6b4a7699-d3cc-4687-9517-2b2153872654");
    when(controllerApi.getBalancesUsingGET(subKey.toString()))
        .thenThrow(new RestClientException("unavailable"));
    assertThrows(InvalidTransactionMessageException.class, () -> {
      client.getAvailableBalanceFromLedger(subKey.toString());
    });
  }

  @Test
  public void getAvailableBalanceInterimNotAvailable() throws IOException {
    BalancesListResponse response = stringToJson(
        fileReader.getFileContent(LM_GET_ACCOUNT_BALANCES_INTERIM_NOT_AVAILABLE),
        BalancesListResponse.class);
    UUID subKey = UUID.fromString("6b4a7699-d3cc-4687-9517-2b2153872654");
    when(controllerApi.getBalancesUsingGET(subKey.toString())).thenReturn(response);
    assertThrows(InvalidTransactionMessageException.class, () -> {
      client.getAvailableBalanceFromLedger(subKey.toString());
    });
  }

  @Test
  public void postTransactionToLedgerReversalSuccessTest() throws IOException {
    com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage mappedMessage = new com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage();
    TransactionMessage transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE,
        TransactionMessage.class);
    when(mapper.toLMTransactionMessage(transactionMessage)).thenReturn(mappedMessage);
    when(ledgerPaymentController.processPaymentsUsingPOST(mappedMessage))
        .thenReturn(new BalancesResponse());
    LedgerPostingResponse actual = client.postTransactionToLedger(transactionMessage);
    assertTrue(actual.isPostingSuccess());
    verify(ledgerPaymentController).processPaymentsUsingPOST(mappedMessage);
    verify(mapper).toLMTransactionMessage(transactionMessage);
  }

  @Test
  public void postTransactionToLedgerNonReversalSuccessTest() throws IOException {
    TransactionMessage transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE,
        TransactionMessage.class);
    LedgerPostingResponse actual = client.postTransactionToLedger(transactionMessage);
    assertTrue(actual.isPostingSuccess());
  }

  @Test
  public void postTransactionToLedgerReversalThrowsHttpServerErrorException() throws IOException {
    HttpServerErrorException httpServerErrorException = new HttpServerErrorException(
        HttpStatus.GATEWAY_TIMEOUT);
    FPSOutTransactionManagerException exception = new FPSOutTransactionManagerException();
    Integer errorCode = INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getStatusCode();
    exception.setErrorCode(errorCode);
    String errorMessage = INTERNAL_SERVER_ERROR_PAIN001_LM_FAILURE.getMessage();
    exception.setErrorMessage(errorMessage);
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    exception.setHttpStatus(httpStatus);
    exception.setFieldName(null);
    com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage mappedMessage = new com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage();
    TransactionMessage transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE,
        TransactionMessage.class);
    when(mapper.toLMTransactionMessage(transactionMessage)).thenReturn(mappedMessage);
    when(ledgerPaymentController.processPaymentsUsingPOST(mappedMessage))
        .thenThrow(httpServerErrorException);
    when(exceptionBuilder
        .buildFPSOutTransactionManagerException(errorCode, errorMessage, httpStatus))
        .thenThrow(exception);
    FPSOutTransactionManagerException actual = assertThrows(FPSOutTransactionManagerException.class,
        () -> {
          client.postTransactionToLedger(transactionMessage);
        });
    assertEquals(exception, actual);
    verify(ledgerPaymentController).processPaymentsUsingPOST(mappedMessage);
    verify(exceptionBuilder)
        .buildFPSOutTransactionManagerException(errorCode, errorMessage, httpStatus);
    verify(mapper).toLMTransactionMessage(transactionMessage);
  }

  @Test
  public void postTransactionToLedgerReversalThrowsHttpServerErrorExceptionWithBadRequest()
      throws IOException {
    HttpServerErrorException httpServerErrorException = new HttpServerErrorException(
        HttpStatus.BAD_REQUEST, "BAD REQUEST",
        "Error".getBytes(), null);
    FPSOutTransactionManagerException exception = new FPSOutTransactionManagerException();
    Integer errorCode = INTERNAL_SERVICE_UNAVAILABLE.getStatusCode();
    exception.setErrorCode(errorCode);
    String errorMessage = INTERNAL_SERVICE_UNAVAILABLE.getMessage();
    exception.setErrorMessage(errorMessage);
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    exception.setHttpStatus(httpStatus);
    exception.setFieldName(null);
    com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage mappedMessage = new com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage();
    TransactionMessage transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE,
        TransactionMessage.class);
    when(mapper.toLMTransactionMessage(transactionMessage)).thenReturn(mappedMessage);
    when(ledgerPaymentController.processPaymentsUsingPOST(mappedMessage))
        .thenThrow(httpServerErrorException);
    when(exceptionBuilder
        .buildFPSOutTransactionManagerException(errorCode, errorMessage, httpStatus))
        .thenThrow(exception);
    FPSOutTransactionManagerException actual = assertThrows(FPSOutTransactionManagerException.class,
        () -> {
          client.postTransactionToLedger(transactionMessage);
        });
    assertEquals(exception, actual);
    verify(ledgerPaymentController).processPaymentsUsingPOST(mappedMessage);
    verify(exceptionBuilder)
        .buildFPSOutTransactionManagerException(errorCode, errorMessage, httpStatus);
    verify(mapper).toLMTransactionMessage(transactionMessage);
  }

  @Test
  public void postTransactionToLedgerReversalThrowsRunTimeException() throws IOException {

    com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage mappedMessage = new com.tenx.universalbanking.transactionmanager.client.ledgermanager.model.TransactionMessage();
    TransactionMessage transactionMessage = buildTransactionMsg(TRANSACTION_MESSAGE,
        TransactionMessage.class);
    when(mapper.toLMTransactionMessage(transactionMessage)).thenReturn(mappedMessage);
    when(ledgerPaymentController.processPaymentsUsingPOST(mappedMessage))
        .thenThrow(new RuntimeException());
    LedgerPostingResponse actual = client.postTransactionToLedger(transactionMessage);
    assertFalse(actual.isPostingSuccess());
    assertEquals(5301, actual.getReason().getCode());
    verify(ledgerPaymentController).processPaymentsUsingPOST(mappedMessage);
    verify(mapper).toLMTransactionMessage(transactionMessage);
  }

  private <T> T buildTransactionMsg(String filename, Class<T> clazz) throws IOException {
    return
        stringToJson(fileReader.getFileContent(filename), clazz);
  }
}

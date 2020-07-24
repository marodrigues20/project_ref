package com.tenx.universalbanking.transactionmanager.service.helpers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.entity.AuthorisationId;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class AuthorisationFinderTest {

  @Mock
  private PaymentAuthorisations authRepository;

  @InjectMocks
  private AuthorisationFinder authorisationFinder;

  private static final String TSYS_ACCOUNT_ID = "12345";
  private static final Date DATE = new DateTime("2018-09-20T12:12:12.111+0000").toDate();
  private static final LocalDate LOCALDATE = LocalDate.parse("2018-09-20");
  private static final BigDecimal AMOUNT = new BigDecimal("20.00");
  private static final String AUTHORISATION_CODE = "12345";
  private static final String BANKNET_REF_NUM = "12345";
  private Optional<Authorisations> authorisations;
  private Authorisations authorisation;

  @BeforeEach
  public void setUp() {
    authorisation = buildAuthorisation();
    authorisations = Optional.of(authorisation);
  }

  @Test
  public void shouldQueryByTsysAccountIdAndId_BankNetReferenceNumber() {
    when(authRepository
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH")).thenReturn(authorisations);
    Authorisations actual = authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, DATE, AMOUNT, AUTHORISATION_CODE, BANKNET_REF_NUM);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH");
    verify(authRepository, times(0))
        .findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
            TSYS_ACCOUNT_ID, AMOUNT, LOCALDATE, AUTHORISATION_CODE);
    assertEquals(authorisation, actual);
  }

  @Test
  public void shouldQueryByTsysAccountIdAndId_BankNetReferenceNumberForReversalAndReturnNull() {
    Authorisations actual = authorisationFinder
        .getAuthorisationEntryForReversal(TSYS_ACCOUNT_ID, BANKNET_REF_NUM);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH");
    assertNull(actual);
  }

  @Test
  public void shouldQueryByTsysAccountIdAndId_BankNetReferenceNumberForReversal() {
    when(authRepository
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH")).thenReturn(authorisations);
    Authorisations actual = authorisationFinder
        .getAuthorisationEntryForReversal(TSYS_ACCOUNT_ID, BANKNET_REF_NUM);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH");
    assertNotNull(actual);
  }

  @Test
  public void shouldQueryByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode() {
    when(authRepository
        .findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
            TSYS_ACCOUNT_ID, AMOUNT, LOCALDATE, AUTHORISATION_CODE)).thenReturn(authorisations);
    Authorisations actual = authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, DATE, AMOUNT, AUTHORISATION_CODE, BANKNET_REF_NUM);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
            TSYS_ACCOUNT_ID, AMOUNT, LOCALDATE, AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH");
    verify(authRepository, times(0))
        .findByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode(TSYS_ACCOUNT_ID, AMOUNT,
            AUTHORISATION_CODE);
    assertEquals(authorisation, actual);
  }

  @Test
  public void shouldQueryByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode() {
    when(authRepository
        .findByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode(TSYS_ACCOUNT_ID, AMOUNT,
            AUTHORISATION_CODE)).thenReturn(authorisations);
    Authorisations actual = authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, DATE, AMOUNT, AUTHORISATION_CODE, BANKNET_REF_NUM);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode(TSYS_ACCOUNT_ID, AMOUNT,
            AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
            TSYS_ACCOUNT_ID, AMOUNT, LOCALDATE, AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH");
    verify(authRepository, times(0))
        .findByTsysAccountIdAndId_TransactionDateAndId_AuthorisationCode(TSYS_ACCOUNT_ID, LOCALDATE,
            AUTHORISATION_CODE);
    assertEquals(authorisation, actual);
  }

  @Test
  public void shouldQueryByTsysAccountIdAndId_TransactionDateAndId_AuthorisationCode() {
    when(authRepository
        .findByTsysAccountIdAndId_TransactionDateAndId_AuthorisationCode(TSYS_ACCOUNT_ID, LOCALDATE,
            AUTHORISATION_CODE)).thenReturn(authorisations);
    Authorisations actual = authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, DATE, AMOUNT, AUTHORISATION_CODE, BANKNET_REF_NUM);
    verify(authRepository)
        .findByTsysAccountIdAndId_TransactionDateAndId_AuthorisationCode(TSYS_ACCOUNT_ID, LOCALDATE,
            AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode(TSYS_ACCOUNT_ID, AMOUNT,
            AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
            TSYS_ACCOUNT_ID, AMOUNT, LOCALDATE, AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH");
    verify(authRepository, times(0))
        .findByTsysAccountIdAndId_TransactionDateAndTransactionType(TSYS_ACCOUNT_ID, LOCALDATE,
            "AUTH");
    assertEquals(authorisation, actual);
  }

  @Test
  public void shouldQueryByTsysAccountIdAndId_TransactionDateAndTransactionType() {
    when(authRepository
        .findByTsysAccountIdAndId_TransactionDateAndTransactionType(TSYS_ACCOUNT_ID, LOCALDATE,
            "AUTH")).thenReturn(authorisations);
    Authorisations actual = authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, DATE, AMOUNT, AUTHORISATION_CODE, BANKNET_REF_NUM);
    verify(authRepository)
        .findByTsysAccountIdAndId_TransactionDateAndTransactionType(TSYS_ACCOUNT_ID, LOCALDATE,
            "AUTH");
    verify(authRepository)
        .findByTsysAccountIdAndId_TransactionDateAndId_AuthorisationCode(TSYS_ACCOUNT_ID, LOCALDATE,
            AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode(TSYS_ACCOUNT_ID, AMOUNT,
            AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
            TSYS_ACCOUNT_ID, AMOUNT, LOCALDATE, AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH");
    assertEquals(authorisation, actual);
  }

  @Test
  public void getAuthorisationReturnNull() {
    Authorisations actual = authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, DATE, AMOUNT, AUTHORISATION_CODE, BANKNET_REF_NUM);
    verify(authRepository)
        .findByTsysAccountIdAndId_TransactionDateAndTransactionType(TSYS_ACCOUNT_ID, LOCALDATE,
            "AUTH");
    verify(authRepository)
        .findByTsysAccountIdAndId_TransactionDateAndId_AuthorisationCode(TSYS_ACCOUNT_ID, LOCALDATE,
            AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_AuthorisationCode(TSYS_ACCOUNT_ID, AMOUNT,
            AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndTransactionAmountAndId_TransactionDateAndId_AuthorisationCode(
            TSYS_ACCOUNT_ID, AMOUNT, LOCALDATE, AUTHORISATION_CODE);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionType(TSYS_ACCOUNT_ID,
            BANKNET_REF_NUM, "AUTH");
    assertNull(actual);
  }

  @Test
  public void shouldReturnRecordstobeDropped() {
    authorisationFinder.getUnmatchedNonReversedAuthorisations(LOCALDATE, false, false);
    verify(authRepository)
        .findAuthorisationsBeforeDateAndNotExpiredAndNotMatched(LOCALDATE, false, false);
  }

  @Test
  public void shouldQueryByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionAmountAndId_TransactionDateAndTransactionType() {
    authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, LOCALDATE, AMOUNT, BANKNET_REF_NUM, false);
    verify(authRepository)
        .findByTsysAccountIdAndId_BankNetReferenceNumberAndTransactionAmountAndId_TransactionDateAndTransactionType(
            TSYS_ACCOUNT_ID, BANKNET_REF_NUM, AMOUNT, LOCALDATE, "AUTH");
  }

  private Authorisations buildAuthorisation() {
    Authorisations auth = new Authorisations();

    AuthorisationId authid = new AuthorisationId();
    authid.setTransactionDate(LOCALDATE);
    authid.setAuthorisationCode(AUTHORISATION_CODE);
    authid.setBankNetReferenceNumber(BANKNET_REF_NUM);

    auth.setId(authid);
    auth.setMcc("mcc");
    auth.setTransactionId("1234");
    auth.setCorrelationId("1234");
    auth.setTsysAccountId(TSYS_ACCOUNT_ID);
    auth.setMatched(true);
    auth.setExpired(false);
    auth.setTransactionType("REVERSAL");
    auth.setTransactionAmount(AMOUNT);
    return auth;
  }
}
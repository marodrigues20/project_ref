package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.CARD_PROCESSOR_ACCOUNTID;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.ExpiredAuthEnum.TRANSACTION_DATE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.EXPIRED;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.EXPIRED_AUTH;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.entity.AuthorisationId;
import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.orchestration.helpers.MessageSender;
import com.tenx.universalbanking.transactionmanager.reconciliation.ReconciliationHelper;
import com.tenx.universalbanking.transactionmanager.repository.PaymentAuthorisations;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.utils.DateConversionUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthRetentionProcessorTest {

  @Mock
  private AuthorisationFinder authorisationFinder;

  @Mock
  private MessageSender messageSender;

  @Mock
  private MessageServiceProcessorHelper idGenerator;

  @Spy
  private DateConversionUtils dateUtils;

  @Mock
  private PaymentAuthorisations authorisationsRepository;

  @Mock
  private ReconciliationHelper reconciliationHelper;

  @InjectMocks
  private AuthRetentionProcessor authRetentionProcessor;

  @Before
  public void init() {
    when(authorisationFinder
        .getUnmatchedNonReversedAuthorisations(any(LocalDate.class), eq(false), eq(false)))
        .thenReturn(Collections.singletonList(getAuthorisation()));
    doNothing().when(reconciliationHelper).saveReconciliationMessage(any());
  }

  @Test
  public void shouldGetBusinessDateBefore4days() {
    LocalDate date = LocalDate.now().minusDays(4);
    authRetentionProcessor.authRetention();
    verify(dateUtils).getLastBusinessDate(date);
  }

  @Test
  public void shouldGetAllTransactionThatWereNotMatchedNorReversed() {
    authRetentionProcessor.authRetention();
    verify(authorisationFinder)
        .getUnmatchedNonReversedAuthorisations(any(LocalDate.class), eq(false), eq(false));
  }

  @Test
  public void shouldUpdateDb() {
    Authorisations actual = getAuthorisation();
    actual.setExpired(true);
    authRetentionProcessor.authRetention();
    verify(authorisationsRepository).saveAll(any(List.class));
  }

  @Test
  public void shouldUpdateDbWithExpiredTrue() {
    authRetentionProcessor.authRetention();
    ArgumentCaptor<List<Authorisations>> captor = ArgumentCaptor.forClass(List.class);
    verify(authorisationsRepository).saveAll(captor.capture());
    List<Authorisations> recordToSave = captor.getValue();
    assertThat(true).isEqualTo(recordToSave.get(0).isExpired());
  }

  @Test
  public void shouldSendMessageToKafka() {
    authRetentionProcessor.authRetention();
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    verify(messageSender).sendPaymentMessage(eq("12345"), captor.capture());
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithCorrelationId() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(actual.getCorrelationId())
        .isEqualTo(expireMessage.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()));
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithTransactionId() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(actual.getTransactionId())
        .isEqualTo(
            expireMessage.getMessages().get(0).getAdditionalInfo().get(TRANSACTION_ID.name()));
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithTransactionStatus() {
    givenAndCaptureArgument();
    verify(idGenerator).addTransactionStatus(any(TransactionMessage.class), eq(EXPIRED));
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithTransactionHeader() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(EXPIRED_AUTH.name())
        .isEqualTo(expireMessage.getHeader().getType());
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithPaymentHeader() {
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(EXPIRED_AUTH.name())
        .isEqualTo(expireMessage.getMessages().get(0).getType());
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithPaymentWithTransactionAmount() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(actual.getTransactionAmount())
        .isEqualTo(expireMessage.getMessages().get(0).getMessage().get(TRANSACTION_AMOUNT.name()));
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithPaymentWithTransactionDate() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(actual.getId().getTransactionDate())
        .isEqualTo(expireMessage.getMessages().get(0).getMessage().get(TRANSACTION_DATE.name()));
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithPaymentWithAuthrisationCode() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(actual.getId().getAuthorisationCode())
        .isEqualTo(expireMessage.getMessages().get(0).getMessage().get(AUTHORISATION_CODE.name()));
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithPaymentWithBankNetRefNum() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(actual.getId().getBankNetReferenceNumber())
        .isEqualTo(
            expireMessage.getMessages().get(0).getMessage().get(BANKNET_REFERENCE_NUMBER.name()));
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithPaymentWithMCC() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(actual.getMcc())
        .isEqualTo(
            expireMessage.getMessages().get(0).getMessage().get(MERCHANT_CATEGORY_CODE.name()));
  }

  @Test
  public void shouldPrepareExpiredAuthMessageWithPaymentWithTSYSAccountId() {
    Authorisations actual = getAuthorisation();
    TransactionMessage expireMessage = givenAndCaptureArgument();
    assertThat(actual.getTsysAccountId())
        .isEqualTo(
            expireMessage.getMessages().get(0).getMessage().get(CARD_PROCESSOR_ACCOUNTID.name()));
  }

  private TransactionMessage givenAndCaptureArgument() {
    authRetentionProcessor.authRetention();
    ArgumentCaptor<TransactionMessage> captor = ArgumentCaptor.forClass(TransactionMessage.class);
    verify(messageSender).sendPaymentMessage(eq("12345"), captor.capture());
    return captor.getValue();
  }

  private Authorisations getAuthorisation() {
    Authorisations authorisations = new Authorisations();
    authorisations.setExpired(false);
    authorisations.setMatched(false);
    authorisations.setTransactionId("12345");
    authorisations.setCorrelationId("12345");
    authorisations.setTsysAccountId("12345");
    authorisations.setTransactionAmount(new BigDecimal("10.00000"));
    authorisations.setMcc("5411");
    AuthorisationId id = new AuthorisationId();
    id.setAuthorisationCode("123456");
    id.setTransactionDate(LocalDate.of(2018, Month.SEPTEMBER, 10));
    id.setBankNetReferenceNumber("123456");
    authorisations.setId(id);
    return authorisations;
  }
}
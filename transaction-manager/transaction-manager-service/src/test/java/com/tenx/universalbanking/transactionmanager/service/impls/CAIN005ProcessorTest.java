package com.tenx.universalbanking.transactionmanager.service.impls;

import static com.tenx.universalbanking.transactionmanager.enums.TransactionType.CASH_DEPOSIT;
import static com.tenx.universalbanking.transactionmanager.enums.TransactionType.REFUND;
import static com.tenx.universalbanking.transactionmessage.enums.Cain003Enum.TRANSACTION_TYPE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.BANKNET_REFERENCE_NUMBER;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.MERCHANT_CATEGORY_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.Cain005Enum.TRANSACTION_AMOUNT;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CLEARING;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.AUTHORISATION_CODE;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.CARD_PROCESSOR_ACCOUNT_ID;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN005;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.entity.Authorisations;
import com.tenx.universalbanking.transactionmanager.service.helpers.AuthorisationFinder;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.Cain001Enum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class CAIN005ProcessorTest {

  @Mock
  private AuthorisationFinder authorisationFinder;

  @InjectMocks
  private CAIN005Processor cain005Processor;

  private TransactionMessage transactionMessage;
  private static final String TSYS_ACCOUNT_ID = "12345";
  private static final Date DATE = new DateTime("2018-09-18T12:12:12.111+0000").toDate();
  private static final BigDecimal AMOUNT = new BigDecimal("20.00");
  private static final String AUTHORISATIONCODE = "12345";
  private static final String BANKNET_REFNUM = "12345";
  private static final String TRANSACTION_ID = "12345";
  private Authorisations authorisations;

  @BeforeEach
  public void init() {
    transactionMessage = createTransactionMessage(CAIN005, "5411");
  }

  @Test
  public void getTypeTest() {
    PaymentMessageTypeEnum actual = cain005Processor.getType();
    assertEquals(CAIN005, actual);
  }

  @Test
  public void shouldReturnNullWhenNoAuthorisationMatch() {
    when(authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, DATE, AMOUNT, AUTHORISATIONCODE, BANKNET_REFNUM))
        .thenReturn(authorisations);
    assertNull(cain005Processor.getAuthorisations(transactionMessage));
  }

  @Test
  public void shouldReturnAuthorisationWhenAuthorisationMatch() {
    Authorisations authorisations = new Authorisations();
    authorisations.setTransactionId(TRANSACTION_ID);
    when(authorisationFinder
        .getAuthorisation(TSYS_ACCOUNT_ID, DATE, AMOUNT, AUTHORISATIONCODE, BANKNET_REFNUM))
        .thenReturn(authorisations);
    assertEquals(TRANSACTION_ID,
        cain005Processor.getAuthorisations(transactionMessage).getTransactionId());
  }

  @Test
  public void shouldSetTransactionTypeCashDeposit() {
    transactionMessage = createTransactionMessage(CAIN005, "6010");
    Authorisations authorisations = new Authorisations();
    authorisations.setTransactionId(TRANSACTION_ID);
    cain005Processor.setTransactionType(transactionMessage.getMessages().get(0));
    assertEquals(CASH_DEPOSIT.getTransactionCode(),
        transactionMessage.getMessages().get(0).getMessage().get(TRANSACTION_TYPE.name()));
  }

  @Test
  public void shouldSetTransactionTypeRefund() {
    transactionMessage = createTransactionMessage(CAIN005, "5411");
    Authorisations authorisations = new Authorisations();
    authorisations.setTransactionId(TRANSACTION_ID);
    cain005Processor.setTransactionType(transactionMessage.getMessages().get(0));
    assertEquals(REFUND.getTransactionCode(),
        transactionMessage.getMessages().get(0).getMessage().get(TRANSACTION_TYPE.name()));
  }

  private TransactionMessage createTransactionMessage(PaymentMessageTypeEnum type, String mCC) {
    TransactionMessage transactionMessage = new TransactionMessage();
    transactionMessage.getHeader().setType(CLEARING.name());
    PaymentMessage paymentMessage = new PaymentMessage();
    Map<String, Object> paymentAddnlInfo = new HashMap<>();
    paymentMessage.setType(type.name());
    Map<String, Object> message = new HashMap();
    message.put(Cain001Enum.TRANSACTION_DATE.name(), DATE);
    message.put(BANKNET_REFERENCE_NUMBER.name(), BANKNET_REFNUM);
    message.put(MERCHANT_CATEGORY_CODE.name(), mCC);
    message.put(TRANSACTION_AMOUNT.name(), AMOUNT);
    paymentAddnlInfo.put(SUBSCRIPTION_KEY.name(), "12345");
    paymentAddnlInfo.put(CARD_PROCESSOR_ACCOUNT_ID.name(), TSYS_ACCOUNT_ID);
    paymentAddnlInfo.put(AUTHORISATION_CODE.name(), AUTHORISATIONCODE);
    paymentMessage.setMessage(message);
    paymentMessage.setAdditionalInfo(paymentAddnlInfo);
    transactionMessage.setMessages(Collections.singletonList(paymentMessage));
    return transactionMessage;
  }
}
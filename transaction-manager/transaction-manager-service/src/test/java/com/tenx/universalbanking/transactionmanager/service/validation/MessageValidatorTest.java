package com.tenx.universalbanking.transactionmanager.service.validation;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.SUBSCRIPTION_KEY;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PACS008;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageException;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class MessageValidatorTest {

  private final TransactionMessageTypeEnum messageType = CARD_AUTH;
  private final String subscriptionKey = "TEST_SETTLEMENT_REQUEST_REQUEST_REQUEST_REQQTEST_CARD_TOKENXXXXAST_REQUEST_REQUEST_REQUEST_REQUEST_TEST_SETTLEMENT_REQUEST_REQUEST_REQUEST_REQQTEST_CARD_TOKENXXXXAST_REQUEST_REQUEST_REQUEST_REQUEST_SREQUEST";

  @InjectMocks
  private MessageValidator unit;

  @Test
  public void shouldThrowExceptionWhenMessageContainsInvalidNumberOfPaymentMessages() {
    assertThrows(InvalidTransactionMessageException.class, () -> {
      unit.validateMessage(createTransactionMessage());
    });
  }

  @Test
  public void shouldThrowExceptionWhenMessageContainsInvalidPaymentMessage() {
    assertThrows(InvalidTransactionMessageException.class, () -> {
      assertTrue(unit.validateMessage(createTransactionMessage(), PACS008));
    });
  }

  @Test
  public void shouldThrowExceptionWhenMessageContainsInvalidPaymentMessageAndESAPIValidationFails() {
    TransactionMessage transactionMessage = createTransactionMessage();
    transactionMessage.getAdditionalInfo().put(SUBSCRIPTION_KEY.name(), subscriptionKey);
    assertThrows(InvalidTransactionMessageException.class, () -> {
      assertTrue(unit.validateMessage(transactionMessage, PACS008));
    });
  }

  @Test
  public void shouldReturnTrueWhenMessageIsValid() {

    assertTrue(unit.validateMessage(createTransactionMessage(), CAIN001));
  }

  @Test
  public void validateAnyMessageTrueTest() {
    assertTrue(unit.validateAnyMessage(createTransactionMessage(), CAIN001));
  }

  @Test
  public void validateAnyMessageFalseTest() {
    assertFalse(unit.validateAnyMessage(createTransactionMessage(), PACS008));
  }

  @Test
  public void validateAnyMessageContainsInvalidNumberOfMessages() {
    TransactionMessage transactionMessage = createTransactionMessage();
    transactionMessage
        .setMessages(Arrays.asList(new PaymentMessage(), transactionMessage.getMessages().get(0)));

    assertThrows(InvalidTransactionMessageException.class, () -> {
      unit.validateAnyMessage(transactionMessage, CAIN001);
    });
  }

  private TransactionMessage createTransactionMessage() {
    TransactionMessage message = new TransactionMessage();
    message.getHeader().setType(messageType.name());
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(CAIN001.name());
    Map<String, Object> additionalInfo = new HashMap<>();
    additionalInfo.put("SUBSCRIPTION_KEY", "9e547634-11bd-42f3-a7ab-12b07403d357");
    additionalInfo.put("TENANT_PARTY_KEY", "12312312");
    message.setAdditionalInfo(additionalInfo);
    message.setMessages(Collections.singletonList(paymentMessage));
    return message;
  }
}
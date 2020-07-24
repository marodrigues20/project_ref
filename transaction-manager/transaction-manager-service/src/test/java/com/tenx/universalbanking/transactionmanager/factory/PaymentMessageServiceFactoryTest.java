package com.tenx.universalbanking.transactionmanager.factory;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CLEARING;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN003;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN005;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PACS003;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageTypeException;
import com.tenx.universalbanking.transactionmanager.service.PaymentMessageService;
import com.tenx.universalbanking.transactionmanager.service.impls.CAIN003Processor;
import com.tenx.universalbanking.transactionmanager.service.impls.CAIN005Processor;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class PaymentMessageServiceFactoryTest {

  private final CAIN003Processor cain003Processor = new CAIN003Processor();
  private final CAIN005Processor cain005Processor = new CAIN005Processor();

  private PaymentMessageServiceFactory factory;
  private List<PaymentMessageService> paymentMessageServiceList;

  @BeforeEach
  public void setUp() {
    paymentMessageServiceList = new ArrayList<>();
    paymentMessageServiceList.add(cain003Processor);
    paymentMessageServiceList.add(cain005Processor);
    factory = new PaymentMessageServiceFactory(paymentMessageServiceList);
  }

  @Test
  public void shouldHandleCAIN003Message() {
    PaymentMessageService responseService = factory
        .getPaymentMessageService(createMessage(CLEARING, CAIN003));
    assertEquals(CAIN003Processor.class, responseService.getClass());
  }

  @Test
  public void shouldHandleCAIN005Service() {
    PaymentMessageService responseService = factory
        .getPaymentMessageService(createMessage(CLEARING, CAIN005));
    assertEquals(CAIN005Processor.class, responseService.getClass());
  }

  @Test
  public void getPaymentMessageServiceThrowsException() {
    InvalidTransactionMessageTypeException actual = assertThrows(
        InvalidTransactionMessageTypeException.class, () -> {
          factory.getPaymentMessageService(createMessage(CLEARING, PACS003));
        });
    assertEquals("Payment Type PACS003 is not supported", actual.getMessage());
  }

  private PaymentMessage createMessage(TransactionMessageTypeEnum messageType,
      PaymentMessageTypeEnum paymentMessageTypeEnum) {
    TransactionMessage message = new TransactionMessage();
    TransactionMessageHeader header = new TransactionMessageHeader();
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(paymentMessageTypeEnum.name());
    message.setMessages(Collections.singletonList(paymentMessage));
    header.setType(messageType.name());
    message.setHeader(header);
    return paymentMessage;
  }
}
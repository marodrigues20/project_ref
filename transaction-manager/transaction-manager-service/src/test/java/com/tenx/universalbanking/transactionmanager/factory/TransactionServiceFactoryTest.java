package com.tenx.universalbanking.transactionmanager.factory;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.BACS_CT_IN;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CLEARING;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.FPS_OUT;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.ON_US;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.CAIN001;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PACS003;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum.PAIN001;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageTypeException;
import com.tenx.universalbanking.transactionmanager.service.TransactionMessageService;
import com.tenx.universalbanking.transactionmanager.service.helpers.MessageServiceProcessorHelper;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthMessageService;
import com.tenx.universalbanking.transactionmanager.service.impls.FPSOutMessageService;
import com.tenx.universalbanking.transactionmanager.service.impls.ONUSMessageService;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class TransactionServiceFactoryTest {

  private final CardAuthMessageService cardAuthMessageService = new CardAuthMessageService();
  private final ONUSMessageService onusMessageService = new ONUSMessageService();
  private final FPSOutMessageService fPSOutMessageService = new FPSOutMessageService();

  @Mock
  private MessageServiceProcessorHelper processorHelper;

  private TransactionServiceFactory factory;
  private List<TransactionMessageService> transactionMessageServices;

  @BeforeEach
  public void setUp() {
    transactionMessageServices = new ArrayList<>();
    transactionMessageServices.add(cardAuthMessageService);
    transactionMessageServices.add(onusMessageService);
    transactionMessageServices.add(fPSOutMessageService);
    factory = new TransactionServiceFactory(processorHelper, transactionMessageServices);
  }

  @Test
  public void shouldHandleCAINMessage() {
    TransactionMessageService responseService = factory
        .getTransactionMessageService(createMessage(CARD_AUTH, CAIN001));
    assertEquals(CardAuthMessageService.class, responseService.getClass());
  }

  @Test
  public void shouldReturnOnUsTransactionMessageService() {
    TransactionMessageService responseService = factory
        .getTransactionMessageService(createMessage(ON_US, PAIN001));
    assertEquals(ONUSMessageService.class, responseService.getClass());
  }

  @Test
  public void shouldHandleFPS_OUTMessage() {
    TransactionMessageService responseService = factory
        .getTransactionMessageService(createMessage(FPS_OUT, PAIN001));
    assertEquals(FPSOutMessageService.class, responseService.getClass());
  }

  @Test
  public void getPaymentMessageServiceThrowsException() {
    InvalidTransactionMessageTypeException actual = assertThrows(InvalidTransactionMessageTypeException.class,() -> {
      factory.getTransactionMessageService(createMessage(BACS_CT_IN, PAIN001));
    });
    assertEquals("Transaction Type BACS_CT_IN is not supported", actual.getMessage());
  }

  private TransactionMessage createMessage(TransactionMessageTypeEnum messageType,
      PaymentMessageTypeEnum paymentMessageTypeEnum) {
    TransactionMessage message = new TransactionMessage();
    TransactionMessageHeader header = new TransactionMessageHeader();
    PaymentMessage paymentMessage = new PaymentMessage();
    paymentMessage.setType(paymentMessageTypeEnum.name());
    message.setMessages(Collections.singletonList(paymentMessage));
    header.setType(messageType.name());
    message.setHeader(header);
    return message;
  }
}

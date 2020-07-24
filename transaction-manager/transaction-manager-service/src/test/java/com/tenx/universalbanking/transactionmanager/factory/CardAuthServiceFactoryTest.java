package com.tenx.universalbanking.transactionmanager.factory;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.CARD_AUTH_VIA_ADVICE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum.FPS_IN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tenx.universalbanking.transactionmanager.exception.InvalidTransactionMessageTypeException;
import com.tenx.universalbanking.transactionmanager.service.CardAuthService;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthAdviceMessageService;
import com.tenx.universalbanking.transactionmanager.service.impls.CardAuthMessageService;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.TransactionMessageHeader;
import com.tenx.universalbanking.transactionmessage.enums.TransactionMessageTypeEnum;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
public class CardAuthServiceFactoryTest {

  private final CardAuthMessageService cardAuthMessageService = new CardAuthMessageService();
  private final CardAuthAdviceMessageService cardAuthAdviceMessageService = new CardAuthAdviceMessageService();

  private CardAuthServiceFactory factory;

  private List<CardAuthService> cardAuthServices;

  @BeforeEach
  public void setUp() {
    cardAuthServices = new ArrayList<>();
    cardAuthServices.add(cardAuthMessageService);
    cardAuthServices.add(cardAuthAdviceMessageService);
    factory = new CardAuthServiceFactory(cardAuthServices);
  }

  @Test
  public void shouldHandleCardAuthMessage() {
    CardAuthService responseService = factory.getCardAuthService(createMessage(CARD_AUTH));
    assertEquals(CardAuthMessageService.class, responseService.getClass());
  }

  @Test
  public void shouldHandleCardAuthViaAdviceMessage() {
    CardAuthService responseService = factory
        .getCardAuthService(createMessage(CARD_AUTH_VIA_ADVICE));
    assertEquals(CardAuthAdviceMessageService.class, responseService.getClass());
  }

  @Test
  public void getCardAuthServiceThrowsException() {
    InvalidTransactionMessageTypeException actual = assertThrows(InvalidTransactionMessageTypeException.class,() -> {
      factory.getCardAuthService(createMessage(FPS_IN));
    });
    assertEquals("Transaction Type FPS_IN is not supported", actual.getMessage());
  }

  private TransactionMessage createMessage(TransactionMessageTypeEnum messageType) {
    TransactionMessage message = new TransactionMessage();
    TransactionMessageHeader header = new TransactionMessageHeader();
    header.setType(messageType.name());
    message.setHeader(header);
    return message;
  }

}

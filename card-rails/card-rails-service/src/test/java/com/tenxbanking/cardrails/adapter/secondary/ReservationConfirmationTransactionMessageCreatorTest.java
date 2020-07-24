package com.tenxbanking.cardrails.adapter.secondary;

import static com.tenx.universalbanking.transactionmessage.enums.Cain001Enum.TRANSACTION_RESPONSE_CODE;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_STATUS;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.FAILED;
import static com.tenx.universalbanking.transactionmessage.enums.TransactionStatusValueEnum.SUCCESS;
import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.AUTHORISATION_CODE;
import static org.assertj.core.api.Assertions.assertThat;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageTypeEnum;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.ReservationConfirmationTransactionMessageCreator;
import com.tenxbanking.cardrails.domain.model.AuthResponseCode;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Money;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReservationConfirmationTransactionMessageCreatorTest {

  private ReservationConfirmationTransactionMessageCreator creator = new ReservationConfirmationTransactionMessageCreator();

  @Test
  void shouldReturnTransactionMessageWithReservedDetails() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> additionalInfo = new HashMap<>();
    transactionMessage.setMessages(createPaymentMessages());
    transactionMessage.setAdditionalInfo(additionalInfo);

    Cain002 cain002 = new Cain002("123456",  Money.of(BigDecimal.ONE, "EUR"), AuthResponseCode._05, true);

    TransactionMessage confirmationMessage = creator.create(transactionMessage, cain002);

    Map<String, Object> actualAdditionalInfo = confirmationMessage.getAdditionalInfo();
    assertThat(actualAdditionalInfo.get(TRANSACTION_STATUS.name())).isEqualTo(SUCCESS.name());
    assertThat(confirmationMessage.getMessages()).size().isEqualTo(3);

    Map<String, Object> cain002TransactionMessage = confirmationMessage.getMessages().get(0).getMessage();

    assertThat(cain002TransactionMessage.get(TRANSACTION_RESPONSE_CODE.name())).isEqualTo("APPR");
    assertThat(cain002TransactionMessage.get(AUTHORISATION_CODE.name())).isEqualTo("123456");
  }

  @Test
  void shouldReturnTransactionMessageWithFailureDetails() {
    TransactionMessage transactionMessage = new TransactionMessage();
    Map<String, Object> additionalInfo = new HashMap<>();
    transactionMessage.setMessages(createPaymentMessages());
    transactionMessage.setAdditionalInfo(additionalInfo);

    Cain002 cain002 = new Cain002("123456",  Money.of(BigDecimal.ONE, "EUR"), AuthResponseCode._05, false);

    TransactionMessage confirmationMessage = creator.create(transactionMessage, cain002);

    Map<String, Object> actualAdditionalInfo = confirmationMessage.getAdditionalInfo();
    assertThat(actualAdditionalInfo.get(TRANSACTION_STATUS.name())).isEqualTo(FAILED.name());
    assertThat(confirmationMessage.getMessages()).size().isEqualTo(3);

    Map<String, Object> cain002TransactionMessage = confirmationMessage.getMessages().get(0).getMessage();

    assertThat(cain002TransactionMessage.get(TRANSACTION_RESPONSE_CODE.name())).isEqualTo("DECL");
    assertThat(cain002TransactionMessage).doesNotContainKey(AUTHORISATION_CODE.name());
  }

  private List<PaymentMessage> createPaymentMessages() {
    PaymentMessage message = new PaymentMessage();
    message.setType(PaymentMessageTypeEnum.CAIN001.name());

    PaymentMessage tax = new PaymentMessage();
    tax.setType(PaymentMessageTypeEnum.TAX.name());

    PaymentMessage fee = new PaymentMessage();
    fee.setType(PaymentMessageTypeEnum.FEES_AND_CHARGES.name());

    return Arrays.asList(message, tax, fee);
  }

}
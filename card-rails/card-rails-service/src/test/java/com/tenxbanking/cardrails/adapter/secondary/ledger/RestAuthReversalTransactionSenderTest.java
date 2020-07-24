package com.tenxbanking.cardrails.adapter.secondary.ledger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.AmountDto;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.BalanceDto;
import com.tenxbanking.cardrails.adapter.secondary.ledger.model.BalanceResponse;
import com.tenxbanking.cardrails.adapter.secondary.messagecreator.CardAuthTransactionMessageCreator;
import com.tenxbanking.cardrails.domain.model.Cain001;
import com.tenxbanking.cardrails.domain.model.Cain002;
import com.tenxbanking.cardrails.domain.model.Money;
import com.tenxbanking.cardrails.domain.model.transaction.CardAuthReversal;
import java.math.BigDecimal;
import java.util.Currency;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RestAuthReversalTransactionSenderTest {

  @Mock
  private LedgerManagerClient ledgerClient;
  @Mock
  private CardAuthTransactionMessageCreator messageMapper;
  @InjectMocks
  private RestAuthReversalTransactionSender underTest;

  @Mock
  private CardAuthReversal cardAuthReversal;
  @Mock
  private Cain001 cain001;
  @Mock
  private TransactionMessage transactionMessage;

  @Test
  void successfulRequest() {
    when(messageMapper.create(cardAuthReversal)).thenReturn(transactionMessage);
    when(ledgerClient.reversal(transactionMessage)).thenReturn(ResponseEntity.ok(new BalanceResponse(
        new BalanceDto(
            "accId",
            new AmountDto(
                "GBP",
                BigDecimal.TEN
            )
        )
    )));
    when(cardAuthReversal.getCain001()).thenReturn(cain001);
    when(cain001.getAuthCode()).thenReturn("AUTHCODE");
    when(cain001.getCurrency()).thenReturn(Currency.getInstance("GBP"));

    Cain002 cain002 = underTest.reverse(cardAuthReversal);

    SoftAssertions.assertSoftly(soft -> {
      soft.assertThat(cain002.isSuccess()).isTrue();
      soft.assertThat(cain002.getUpdatedBalance()).isEqualTo(Money.of(10, "GBP"));
      soft.assertThat(cain002.getAuthCode()).isEqualTo("AUTHCODE");
    });

    verify(messageMapper).create(cardAuthReversal);
    verify(ledgerClient).reversal(transactionMessage);
  }

  @Test
  void failedRequest() {
    when(messageMapper.create(cardAuthReversal)).thenReturn(transactionMessage);
    when(ledgerClient.reversal(transactionMessage)).thenThrow(new RuntimeException("Blah"));

    assertThrows(RuntimeException.class,
        () -> underTest.reverse(cardAuthReversal));
  }

}
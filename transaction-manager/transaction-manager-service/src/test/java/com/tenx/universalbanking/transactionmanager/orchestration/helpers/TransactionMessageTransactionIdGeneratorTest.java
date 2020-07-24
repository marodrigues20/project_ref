package com.tenx.universalbanking.transactionmanager.orchestration.helpers;

import static com.tenx.universalbanking.transactionmessage.paymentmessage.enums.PaymentMessageAdditionalInfoEnum.TRANSACTION_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.paymentmessage.PaymentMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionMessageTransactionIdGeneratorTest {

  @Mock
  GeneratorUtil lionGeneratorUtils;

  @InjectMocks
  TransactionMessageTransactionIdGenerator transactionIdGenerator;

  private static final String GENERATED_KEY = "key";

  @Before
  public void setupMockForLionIdGenerator() throws JSONException {
    when(lionGeneratorUtils.generateRandomKey()).thenReturn(GENERATED_KEY);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(TRANSACTION_ID.toString(), GENERATED_KEY);
  }

  @Test
  public void addNewTransactionId_shouldInjectTransactionIdToPaymentMessage() {
    //given
    PaymentMessage paymentMessage = new PaymentMessage();
    //when
    transactionIdGenerator.addTransactionId(paymentMessage);
    //then
    assertThat(paymentMessage.getAdditionalInfo()
        .get(TRANSACTION_ID.name()), is(GENERATED_KEY));
  }
}
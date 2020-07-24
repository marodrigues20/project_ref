package com.tenx.universalbanking.transactionmanager.orchestration.helpers;

import static com.tenx.universalbanking.transactionmessage.enums.TransactionMessageAdditionalInfoEnum.TRANSACTION_CORRELATION_ID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionMessageCorrelationIdGeneratorTest {

  @Mock
  private GeneratorUtil generatorUtils;

  @InjectMocks
  private TransactionMessageCorrelationIdGenerator correlationIdGenerator;

  private static final String GENERATED_KEY = "key";

  @Before
  public void setupMockForLionIdGenerator() throws JSONException {
    when(generatorUtils.generateRandomKey()).thenReturn(GENERATED_KEY);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(TRANSACTION_CORRELATION_ID.toString(), GENERATED_KEY);
  }

  @Test
  public void shouldRequestNewIdFromLionGenerator() {
    TransactionMessage transactionMessage = new TransactionMessage();
    correlationIdGenerator.addCorrelationId(transactionMessage);
    verify(generatorUtils).generateRandomKey();
  }

  @Test
  public void addNewTransactionId_shouldInjectCorrelationIdIntoAdditionalInfo() {
    //given
    TransactionMessage message = new TransactionMessage();
    //when
    correlationIdGenerator.addCorrelationId(message);
    //then
    assertThat(message.getAdditionalInfo().get(TRANSACTION_CORRELATION_ID.name()),
        is(GENERATED_KEY));
  }

}
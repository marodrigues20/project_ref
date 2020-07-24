package com.tenx.universalbanking.transactionmanager.orchestration.factory;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmanager.utils.JsonUtils;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.core.io.ClassPathResource;
import com.tenx.universalbanking.transactionmanager.model.ApplicationAdjustmentMessageData;
import com.tenx.universalbanking.transactionmanager.utils.DateConversionUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;

public class ApplicationAdjustmentMessageFactoryTest {

  private static final Long PARTY_KEY_VALUE = 1L;
  private static final Long TENANT_PARTY_KEY_VALUE = 10L;
  private static final String PRODUCT_KEY_VALUE = "2";
  private static final Long PARENT_TRANSACTION_ID_VALUE = 4L;
  private static final Date TRANSACTION_DATE_VALUE = TestHelper.createDateFor(22, 17, 42, 37, 0);
  private static final Date TRANSACTION_VALUE_DATE_VALUE = TestHelper.createDateFor(22, 17, 59, 37, 0);
  private static final BigDecimal AMOUNT = new BigDecimal("10.1234");
  private static final String SUBSCRIPTION_KEY_VALUE = "27c5b516-5aea-540d-adec-59cb56c6f637";
  private static final String TRANSACTION_ID_VALUE = "72c5b516-5aea-540d-adec-59cb56c6f637";
  private static final String CORRELATION_ID_VALUE = "123456";

  private ApplicationAdjustmentMessageFactory applicationAdjustmentMessageFactory;

  @Before
  public void setupIdGenerator() {
    GeneratorUtil mockLionGeneratorUtils = mock(GeneratorUtil.class);
    applicationAdjustmentMessageFactory = new ApplicationAdjustmentMessageFactory(mockLionGeneratorUtils,
        new DateConversionUtils());
    when(mockLionGeneratorUtils.generateRandomKey()).thenReturn(TRANSACTION_ID_VALUE);
  }

  @Test
  public void canCreateTransactionMessageFromInput() throws Exception {
    TransactionMessage transactionMessage = applicationAdjustmentMessageFactory
        .create(getApplicationAdjustmentMessageData());

    assertThat(transactionMessage).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo("APPLICATION_ADJUSTMENTS");
    assertThat(transactionMessage.getAdditionalInfo().get("TRANSACTION_CORRELATION_ID"))
        .isEqualTo(CORRELATION_ID_VALUE);
    assertThat(transactionMessage.getMessages().size()).isEqualTo(1);
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getType()).isEqualTo("APPLICATION_ADJUSTMENTS");
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getAdditionalInfo().get("TRANSACTION_ID"))
        .isEqualTo(TRANSACTION_ID_VALUE);
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getAdditionalInfo().get("SUBSCRIPTION_KEY"))
        .isEqualTo(SUBSCRIPTION_KEY_VALUE);
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getMessage().get("TRANSACTION_DATE"))
        .isEqualTo(TestHelper.dateToString(TRANSACTION_DATE_VALUE));
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getMessage().get("VALUE_DATE_TIME"))
        .isEqualTo(TestHelper.dateToString(TRANSACTION_VALUE_DATE_VALUE));
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getMessage().get("AMOUNT")).isEqualTo(AMOUNT);
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getMessage().get("BASE_CURRENCY_CODE"))
        .isEqualTo("GBP");
  }

  @Test
  public void transactionMessageIsCreatedInCorrectJsonFormat() throws Exception {
    TransactionMessage transactionMessage = applicationAdjustmentMessageFactory
        .create(getApplicationAdjustmentMessageData());
    JSONAssert.assertEquals(getFileContentAsString(),
        JsonUtils.toJson(transactionMessage), getDatePatternComparator());
  }

  private CustomComparator getDatePatternComparator() {
    return new CustomComparator(JSONCompareMode.STRICT,
        TestHelper.createDatetimeCustomization("messages[0].message.TRANSACTION_DATE"),
        TestHelper.createDatetimeCustomization("messages[0].message.VALUE_DATE_TIME"));
  }

  private ApplicationAdjustmentMessageData getApplicationAdjustmentMessageData() {
    return new ApplicationAdjustmentMessageData(PARENT_TRANSACTION_ID_VALUE, PARTY_KEY_VALUE,
        PRODUCT_KEY_VALUE, SUBSCRIPTION_KEY_VALUE, TENANT_PARTY_KEY_VALUE,
        AMOUNT, TRANSACTION_DATE_VALUE, TRANSACTION_VALUE_DATE_VALUE, CORRELATION_ID_VALUE);
  }
  private String getFileContentAsString() throws IOException {
	    try (InputStream in = new ClassPathResource("message/application_adjustments_v2.json").getInputStream()) {
	      return IOUtils.toString(in, StandardCharsets.UTF_8);
	    }
	  }
}

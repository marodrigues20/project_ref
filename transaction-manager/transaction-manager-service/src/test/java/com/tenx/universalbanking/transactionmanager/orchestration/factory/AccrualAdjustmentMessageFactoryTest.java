package com.tenx.universalbanking.transactionmanager.orchestration.factory;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.core.io.ClassPathResource;
import com.tenx.universalbanking.transactionmanager.model.AccrualAdjustmentMessageData;
import com.tenx.universalbanking.transactionmanager.utils.DateConversionUtils;
import com.tenx.universalbanking.transactionmanager.utils.GeneratorUtil;
import com.tenx.universalbanking.transactionmanager.utils.JsonUtils;
import com.tenx.universalbanking.transactionmessage.TransactionMessage;

public class AccrualAdjustmentMessageFactoryTest {

  private static final Long PARTY_KEY_VALUE = 1L;
  private static final Long TENANT_PARTY_KEY_VALUE = 10L;
  private static final String PRODUCT_KEY_VALUE = "2";
  private static final Long PARENT_TRANSACTION_ID_VALUE = 4L;

  private static final Date TRANSACTION_DATE_VALUE = TestHelper.createDateFor(22, 17, 42, 37, 0);
  private static final Date TRANSACTION_VALUE_DATE_VALUE = TestHelper.createDateFor(22, 17, 59, 18, 0);
  private static final Date ACCRUED_DATE = TestHelper.createDateFor(22, 18, 15, 58, 200 * 1000000);
  private static final Date INTEREST_COMPOUND_DATE = TestHelper.createDateFor(
      22, 18, 15, 58, 100 * 1000000);
  private static final Date INTEREST_APPLICATION_DATE = TestHelper
      .createDateFor(23, 18, 15, 58, 100 * 1000000);

  private static final BigDecimal AMOUNT = new BigDecimal("10.1234");
  private static final String SUBSCRIPTION_KEY_VALUE = "27c5b516-5aea-540d-adec-59cb56c6f637";
  private static final String TRANSACTION_ID_VALUE = "72c5b516-5aea-540d-adec-59cb56c6f637";
  private static final String CORRELATION_ID_VALUE = "123456";

  private AccrualAdjustmentMessageFactory accrualAdjustmentMessageFactory;

  @Before
  public void setupIdGenerator() {
    GeneratorUtil mockLionGeneratorUtils = mock(GeneratorUtil.class);
    accrualAdjustmentMessageFactory = new AccrualAdjustmentMessageFactory(mockLionGeneratorUtils,
        new DateConversionUtils());
    when(mockLionGeneratorUtils.generateRandomKey()).thenReturn(TRANSACTION_ID_VALUE);
  }

  @Test
  public void canCreatePendingAdjustmentMessageFromInput() throws Exception {
    AccrualAdjustmentMessageData accrualAdjustmentMessageData = getAccrualAdjustmentMessageData(true);

    TransactionMessage transactionMessage = accrualAdjustmentMessageFactory.create(accrualAdjustmentMessageData);

    assertTransactionMessage(transactionMessage, "ACCRUAL_ADJUSTMENTS_PENDING_APPLICATION");
  }

  @Test
  public void transactionMessageIsCreatedInCorrectJsonFormatForAccrualPendingApplication() throws Exception {
    AccrualAdjustmentMessageData accrualAdjustmentMessageData = getAccrualAdjustmentMessageData(true);
    TransactionMessage transactionMessage = accrualAdjustmentMessageFactory.create(accrualAdjustmentMessageData);
    JSONAssert.assertEquals(getFileContentAsString("message/accrual_adjustment_for_pending_application_v2.json"),
        JsonUtils.toJson(transactionMessage), getDatePatternComparator());
  }

  @Test
  public void canCreateAppliedAdjustmentMessageFromInput() throws Exception {
    AccrualAdjustmentMessageData accrualAdjustmentMessageData = getAccrualAdjustmentMessageData(false);

    TransactionMessage transactionMessage = accrualAdjustmentMessageFactory.create(accrualAdjustmentMessageData);

    assertTransactionMessage(transactionMessage, "ACCRUAL_ADJUSTMENTS_FOR_APPLIED_ACCRUALS");
  }

  @Test
  public void transactionMessageIsCreatedInCorrectJsonFormatForAppliedAccrual() throws Exception {
    AccrualAdjustmentMessageData accrualAdjustmentMessageData = getAccrualAdjustmentMessageData(false);
    TransactionMessage transactionMessage = accrualAdjustmentMessageFactory.create(accrualAdjustmentMessageData);
    JSONAssert.assertEquals(getFileContentAsString("message/accrual_adjustment_for_applied_accruals_v2.json"),
        JsonUtils.toJson(transactionMessage), getDatePatternComparator());
  }

  private CustomComparator getDatePatternComparator() {
    return new CustomComparator(JSONCompareMode.STRICT,
        TestHelper.createDatetimeCustomization("messages[0].message.TRANSACTION_DATE"),
        TestHelper.createDatetimeCustomization("messages[0].message.VALUE_DATE_TIME"),
        TestHelper.createDatetimeCustomization("messages[0].message.INTEREST_ACCRUED_DATE"),
        TestHelper.createDatetimeCustomization("messages[0].message.INTEREST_APPLICATION_DATE"),
        TestHelper.createDatetimeCustomization("messages[0].message.INTEREST_COMPOUND_DATE"));
  }

  private void assertTransactionMessage(TransactionMessage transactionMessage,
      String accrual_adjustments_for_applied_accruals) throws ParseException {
    assertThat(transactionMessage).isNotNull();
    assertThat(transactionMessage.getHeader().getType()).isEqualTo(accrual_adjustments_for_applied_accruals);
    assertThat(transactionMessage.getAdditionalInfo().get("TRANSACTION_CORRELATION_ID"))
        .isEqualTo(CORRELATION_ID_VALUE);
    assertThat(transactionMessage.getMessages().size()).isEqualTo(1);
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getType())
        .isEqualTo(accrual_adjustments_for_applied_accruals);
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
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getMessage().get("INTEREST_ACCRUED_DATE"))
        .isEqualTo(TestHelper.dateToString(ACCRUED_DATE));
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getMessage().get("INTEREST_APPLICATION_DATE"))
        .isEqualTo(TestHelper.dateToString(INTEREST_APPLICATION_DATE));
    assertThat(TestHelper.getPaymentMessage(transactionMessage).getMessage().get("INTEREST_COMPOUND_DATE"))
        .isEqualTo(TestHelper.dateToString(INTEREST_COMPOUND_DATE));
  }

  private AccrualAdjustmentMessageData getAccrualAdjustmentMessageData(boolean isCreateJournal) {
    return new AccrualAdjustmentMessageData(PARENT_TRANSACTION_ID_VALUE, PARTY_KEY_VALUE,
        PRODUCT_KEY_VALUE, SUBSCRIPTION_KEY_VALUE, TENANT_PARTY_KEY_VALUE, ACCRUED_DATE, TRANSACTION_VALUE_DATE_VALUE,
        TRANSACTION_DATE_VALUE, AMOUNT, INTEREST_COMPOUND_DATE, INTEREST_APPLICATION_DATE, isCreateJournal,
        CORRELATION_ID_VALUE);
  }
  
  private String getFileContentAsString(String fileName) throws IOException {
	    try (InputStream in = new ClassPathResource(fileName).getInputStream()) {
	      return IOUtils.toString(in, StandardCharsets.UTF_8);
	    }
	  }
}
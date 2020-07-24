package com.tenx.universalbanking.transactionmanager.componenttest.cockroach;

import com.tenx.universalbanking.transactionmanager.componenttest.tests.BacsDirectDebitAndCreditTransferComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CAIN001RestComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CardAuthAdviceControllerComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CardAuthControllerComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CardFundMessageComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CardSettlementControllerComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.ChildSubscriptionComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.FPSOutComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.PaymentMessageProcessComponentTest;
import org.springframework.test.context.ActiveProfiles;

abstract class CockroachTests {

  @ActiveProfiles("cockroachdb")
  public static class BacsDirectDebitAndCreditTransferComponentCrTest extends
      BacsDirectDebitAndCreditTransferComponentTest {}

  @ActiveProfiles("cockroachdb")
  public static class  CAIN001RestComponentCrTest extends CAIN001RestComponentTest {}

  @ActiveProfiles("cockroachdb")
  public static class CardAuthAdviceControllerComponentCrTest extends
      CardAuthAdviceControllerComponentTest {}

  @ActiveProfiles("cockroachdb")
  public static class CardAuthControllerComponentCrTest extends
      CardAuthControllerComponentTest {}

  @ActiveProfiles("cockroachdb")
  public static class CardFundMessageComponentCrTest extends CardFundMessageComponentTest {}

  @ActiveProfiles("cockroachdb")
  public static class CardSettlementControllerComponentCrTest extends
      CardSettlementControllerComponentTest {}

  @ActiveProfiles("cockroachdb")
  public static class ChildSubscriptionComponentCrTest extends
      ChildSubscriptionComponentTest {}

  @ActiveProfiles("cockroachdb")
  public static class FPSOutComponentCrTest extends FPSOutComponentTest {}

  @ActiveProfiles("cockroachdb")
  public static class PaymentMessageProcessComponentCrTest extends
      PaymentMessageProcessComponentTest {}

}
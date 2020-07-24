package com.tenx.universalbanking.transactionmanager.componenttest.mysql;

import com.tenx.universalbanking.transactionmanager.componenttest.tests.BacsDirectDebitAndCreditTransferComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CAIN001RestComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CardAuthAdviceControllerComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CardAuthControllerComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CardFundMessageComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.CardSettlementControllerComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.ChildSubscriptionComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.FPSOutComponentTest;
import com.tenx.universalbanking.transactionmanager.componenttest.tests.PaymentMessageProcessComponentTest;

// No need of @ActiveProfiles("mysql") because mysql is set by default
abstract class MySQLTests {

  public static class BacsDirectDebitAndCreditTransferComponentSqlTest extends
      BacsDirectDebitAndCreditTransferComponentTest {}

  public static class  CAIN001RestComponentSqlTest extends CAIN001RestComponentTest {}

  public static class CardAuthAdviceControllerComponentSqlTest extends
      CardAuthAdviceControllerComponentTest {}

  public static class CardAuthControllerComponentSqlTest extends
      CardAuthControllerComponentTest {}

  public static class CardFundMessageComponentSqlTest extends CardFundMessageComponentTest {}

  public static class CardSettlementControllerComponentSqlTest extends
      CardSettlementControllerComponentTest {}

  public static class ChildSubscriptionComponentSqlTest extends
      ChildSubscriptionComponentTest {}

  public static class FPSOutComponentSqlTest extends FPSOutComponentTest {}

  public static class PaymentMessageProcessComponentSqlTest extends
      PaymentMessageProcessComponentTest {}

}

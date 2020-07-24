package com.tenxbanking.cardrails.data;

import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_EFFECTIVE_DATE;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_PRODUCT_VERSION;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.DAY;
import static com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums.MONTH;
import static com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus.ACTIVE;
import static com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums.ATMWITHDRAWAL;
import static com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums.TRANSFERIN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionLimit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SubscriptionDataFactory {

  public static Subscription getSubscriptionWithAtmWithdrawalAndTransferIn(UUID subscriptionKey,
      Instant effectiveDate, int productVersion, SubscriptionStatus status) {

    List<TransactionLimit> atmLimits = singletonList(getAtmWithdrawalTransactionLimit());
    SubscriptionSettings atmSettings = getSubscriptionSettings(DEFAULT_EFFECTIVE_DATE,
        DEFAULT_PRODUCT_VERSION, atmLimits);

    List<TransactionLimit> transferInLimits = singletonList(getTransferInTransactionLimit());
    SubscriptionSettings transferInSettings = getSubscriptionSettings(effectiveDate,
        productVersion, transferInLimits);

    return Subscription.builder()
        .subscriptionKey(subscriptionKey)
        .settings(asList(atmSettings, transferInSettings))
        .status(status)
        .build();
  }

  public static Subscription getSubscriptionWithAtmWithdrawal(UUID subscriptionKey) {

    List<TransactionLimit> limits = singletonList(getAtmWithdrawalTransactionLimit());

    SubscriptionSettings settings = getSubscriptionSettings(DEFAULT_EFFECTIVE_DATE,
        DEFAULT_PRODUCT_VERSION, limits);

    return Subscription.builder()
        .subscriptionKey(subscriptionKey)
        .settings(singletonList(settings))
        .status(ACTIVE)
        .build();
  }

  public static Subscription getSubscriptionWithTransferIn(UUID subscriptionKey,
      Instant effectiveDate, int productVersion, SubscriptionStatus status) {

    List<TransactionLimit> limits = singletonList(getTransferInTransactionLimit());

    SubscriptionSettings settings = getSubscriptionSettings(effectiveDate,
        productVersion, limits);

    return Subscription.builder()
        .subscriptionKey(subscriptionKey)
        .settings(singletonList(settings))
        .status(status)
        .build();
  }

  public static TransactionLimit getAtmWithdrawalTransactionLimit() {
    return new TransactionLimit(ATMWITHDRAWAL,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(100),
        MONTH);
  }

  public static TransactionLimit getTransferInTransactionLimit() {
    return new TransactionLimit(TRANSFERIN,
        BigDecimal.valueOf(0),
        BigDecimal.valueOf(10),
        DAY);
  }

  public static SubscriptionSettings getSubscriptionSettings(Instant effectiveDate,
      int productVersion,
      List<TransactionLimit> transactionLimits) {

    return SubscriptionSettings.builder()
        .effectiveDate(effectiveDate)
        .productKey(PRODUCT_KEY)
        .productVersion(productVersion)
        .transactionLimits(transactionLimits)
        .hasFees(false)
        .build();
  }

}

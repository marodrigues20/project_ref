package com.tenxbanking.cardrails.adapter.primary.data;

import static com.tenxbanking.cardrails.domain.service.TimeService.ISO8601_DATETIME_FORMATTER;

import com.google.common.collect.ImmutableList;
import com.tenx.dub.subscription.event.v1.Limits;
import com.tenx.dub.subscription.event.v1.PartyRole;
import com.tenx.dub.subscription.event.v1.ProductDetails;
import com.tenx.dub.subscription.event.v1.SubscriptionCreationRule;
import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import com.tenx.dub.subscription.event.v1.TransactionLimits;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class SubscriptionEventFactory {

  public static final UUID PRODUCT_KEY = UUID.fromString("e3393851-2fb8-4e0d-b97c-ef7328e76ec0");
  public static final UUID PARTY_KEY = UUID.fromString("c6bc5c48-726d-450e-b50e-9293c9e9f079");

  public static final String DEFAULT_EFFECTIVE_DATE_STR = "2018-08-19T10:12:14.000+0000";
  public static final Instant DEFAULT_EFFECTIVE_DATE = OffsetDateTime.parse(DEFAULT_EFFECTIVE_DATE_STR, ISO8601_DATETIME_FORMATTER).toInstant();

  public static final String TENANT_KEY = "10000";
  public static final String DEFAULT_STATUS = "ACTIVE";
  public static final Integer DEFAULT_PRODUCT_VERSION = 1;

  public static SubscriptionEvent getSubscriptionEvent(final UUID subscriptionKey) {

    final TransactionLimits transactionLimits = getAtmWithdrawalTransactionLimits();
    final Limits limits = getLimits(transactionLimits);
    return getSubscriptionEvent(subscriptionKey, limits, DEFAULT_EFFECTIVE_DATE_STR,
        DEFAULT_PRODUCT_VERSION, DEFAULT_STATUS);
  }

  public static SubscriptionEvent getSubscriptionEvent(final UUID subscriptionKey,
      final Limits limits, final String effectiveDate, final int productVersion,
      String status) {

    return SubscriptionEvent.newBuilder()
        .setSubscriptionKey(subscriptionKey.toString())
        .setProductKey(PRODUCT_KEY.toString())
        .setProductVersion(productVersion)
        .setSubscriptionStatus(status)
        .setPartyRoles(getPartyRoles())
        .setProductDetails(getProductDetails(limits, effectiveDate, productVersion))
        .setCreatedDate("notUsed")
        .build();
  }

  public static List<PartyRole> getPartyRoles() {

    return ImmutableList.of(
        PartyRole.newBuilder()
            .setPartyKey(PARTY_KEY.toString())
            .setTenantKey(TENANT_KEY)
            .setRole("primaryOwner")
            .build()
    );
  }

  public static ProductDetails getProductDetails(final Limits limits,
      String effectiveDate, int productVersion) {

    final SubscriptionCreationRule subscriptionCreationRule = getSubscriptionCreationRule();

    return ProductDetails.newBuilder()
        .setEffectiveDate(effectiveDate)
        .setProductKey(PRODUCT_KEY.toString())
        .setMajorVersion(productVersion)
        .setStatus("Live")
        .setProductType("PersonalCurrentAccount")
        .setCreatedDate("2017-08-08")
        .setCreatedBy(TENANT_KEY)
        .setSubscriptionCreationRule(subscriptionCreationRule)
        .setLimits(limits)
        .build();
  }

  public static Limits getLimits(TransactionLimits transactionLimits) {
    return Limits.newBuilder()
        .setTransactionLimits(ImmutableList.of(transactionLimits))
        .build();
  }

  public static SubscriptionCreationRule getSubscriptionCreationRule() {
    return SubscriptionCreationRule.newBuilder()
        .setDefaultSubscriptionStatus("ACTIVE")
        .setRequiredSignatoriesNumber(1)
        .setLockedStatus(false)
        .build();
  }

  public static TransactionLimits getAtmWithdrawalTransactionLimits() {
    return TransactionLimits.newBuilder()
        .setTransactionName("ATMwithdrawal")
        .setDescription("ATMwithdrawal description")
        .setMinimumAmount("10")
        .setMaximumAmount("100")
        .setResetPeriod("MONTH")
        .build();
  }

  public static TransactionLimits getTransferInTransactionLimits() {
    return TransactionLimits.newBuilder()
        .setTransactionName("transferIn")
        .setDescription("transferIn description")
        .setMinimumAmount("0")
        .setMaximumAmount("10")
        .setResetPeriod("DAY")
        .build();
  }
}

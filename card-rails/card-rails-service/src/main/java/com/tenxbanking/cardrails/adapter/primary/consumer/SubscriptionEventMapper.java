package com.tenxbanking.cardrails.adapter.primary.consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.tenx.dub.subscription.event.v1.Limits;
import com.tenx.dub.subscription.event.v1.SubscriptionEvent;
import com.tenx.dub.subscription.event.v1.TransactionLimits;
import com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionLimit;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
class SubscriptionEventMapper {

  private final TimeService timeService;

  public Subscription toSubscription(@NonNull final SubscriptionEvent subscriptionEvent) {

    return Subscription.builder()
        .subscriptionKey(UUID.fromString(subscriptionEvent.getSubscriptionKey()))
        .settings(singletonList(toSubscriptionSettings(subscriptionEvent.getProductDetails())))
        .status(SubscriptionStatus.fromString(subscriptionEvent.getSubscriptionStatus()))
        .build();
  }

  private SubscriptionSettings toSubscriptionSettings(
      @NonNull final com.tenx.dub.subscription.event.v1.ProductDetails productDetails) {

    return SubscriptionSettings.builder()
        .effectiveDate(timeService.toInstant(productDetails.getEffectiveDate()))
        .productKey(UUID.fromString(productDetails.getProductKey()))
        .productVersion(productDetails.getMajorVersion())
        .transactionLimits(toTransactionLimits(productDetails.getLimits()))
        .hasFees(!isEmpty(productDetails.getFeesCharges()))
        .build();
  }

  private List<TransactionLimit> toTransactionLimits(final Limits limits) {

    return (isNull(limits) || isEmpty(limits.getTransactionLimits())
        ? emptyList()
        : limits.getTransactionLimits().stream().map(this::toTransactionLimit).collect(toList()));
  }

  private TransactionLimit toTransactionLimit(TransactionLimits transactionLimits) {

    return new TransactionLimit(
        TransactionNameEnums.fromString(transactionLimits.getTransactionName()),
        toBigDecimal(transactionLimits.getMinimumAmount()),
        toBigDecimal(transactionLimits.getMaximumAmount()),
        ResetPeriodEnums.fromString(
            transactionLimits.getResetPeriod() != null ? transactionLimits.getResetPeriod() : ResetPeriodEnums.TRANSACTION.name()));
  }

  private static BigDecimal toBigDecimal(String amount) {

    return isNull(amount) ? null : new BigDecimal(amount);
  }
}

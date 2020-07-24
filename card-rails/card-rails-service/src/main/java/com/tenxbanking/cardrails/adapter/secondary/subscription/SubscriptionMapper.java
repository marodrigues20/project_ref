package com.tenxbanking.cardrails.adapter.secondary.subscription;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.tenxbanking.cardrails.adapter.secondary.subscription.model.Limits;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.Product;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.TransactionLimits;
import com.tenxbanking.cardrails.domain.exception.UnmappableSubscriptionException;
import com.tenxbanking.cardrails.domain.model.subscription.ResetPeriodEnums;
import com.tenxbanking.cardrails.domain.model.subscription.Subscription;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionSettings;
import com.tenxbanking.cardrails.domain.model.subscription.SubscriptionStatus;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionLimit;
import com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums;
import com.tenxbanking.cardrails.domain.service.TimeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import liquibase.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
class SubscriptionMapper {

  private final TimeService timeService;

  public Subscription toSubscription(@NonNull final SubscriptionProductsResponse response) {
    try {
      return doSubscriptionMap(response);
    } catch (Exception e) {
      final String msg = format("Could not map subscription manager response for subscription %s",
          response.getSubscriptionKey());
      throw new UnmappableSubscriptionException(msg);
    }
  }

  private Subscription doSubscriptionMap(@NonNull final SubscriptionProductsResponse response) {

    return Subscription.builder()
        .status(SubscriptionStatus.fromString(response.getSubscriptionStatus()))
        .subscriptionKey(response.getSubscriptionKey())
        .settings(toSubscriptionSettingsList(response.getProducts()))
        .build();
  }

  private List<SubscriptionSettings> toSubscriptionSettingsList(
      @NonNull final List<Product> products) {

    return products.stream().map(this::toSubscriptionSettings).collect(toUnmodifiableList());
  }

  private SubscriptionSettings toSubscriptionSettings(@NonNull final Product product) {

    return SubscriptionSettings.builder()
        .hasFees(product.isHasFees())
        .productKey(product.getProductKey())
        .productVersion(product.getProductVersion())
        .effectiveDate(timeService.toInstant(product.getEffectiveDate()))
        .transactionLimits(toTransactionLimitsList(product.getLimits()))
        .build();
  }

  private List<TransactionLimit> toTransactionLimitsList(Limits limits) {

    if (isNull(limits) || isEmpty(limits.getTransactionLimits())) {
      return emptyList();
    }

    return limits.getTransactionLimits().stream()
        .filter(Objects::nonNull)
        .map(this::toTransactionLimit)
        .collect(toUnmodifiableList());
  }

  private TransactionLimit toTransactionLimit(@NonNull final TransactionLimits transactionLimit) {

    return TransactionLimit.builder()
        .transactionName(TransactionNameEnums.fromString(transactionLimit.getTransactionName()))
        .minimumAmount(toBigDecimal(transactionLimit.getMinimumAmount()))
        .maximumAmount(toBigDecimal(transactionLimit.getMaximumAmount()))
        .resetPeriod(ResetPeriodEnums.fromString(
            transactionLimit.getResetPeriod() != null ? transactionLimit.getResetPeriod() : ResetPeriodEnums.TRANSACTION.name()))
        .build();
  }

  private BigDecimal toBigDecimal(final String value) {
    return StringUtils.isEmpty(value) ? null : new BigDecimal(value);
  }
}

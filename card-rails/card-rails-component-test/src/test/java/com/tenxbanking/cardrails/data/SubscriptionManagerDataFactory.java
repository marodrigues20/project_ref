package com.tenxbanking.cardrails.data;

import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_EFFECTIVE_DATE_STR;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.DEFAULT_PRODUCT_VERSION;
import static com.tenxbanking.cardrails.adapter.primary.data.SubscriptionEventFactory.PRODUCT_KEY;
import static com.tenxbanking.cardrails.domain.model.subscription.TransactionNameEnums.ATMWITHDRAWAL;
import static java.util.Collections.singletonList;

import com.tenxbanking.cardrails.adapter.secondary.subscription.model.Limits;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.Product;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.SubscriptionProductsResponse;
import com.tenxbanking.cardrails.adapter.secondary.subscription.model.TransactionLimits;
import java.util.UUID;

public class SubscriptionManagerDataFactory {

  public static SubscriptionProductsResponse getSubscriptionProductsResponse(UUID subscriptionKey) {

    final TransactionLimits transactionLimits = TransactionLimits.builder()
        .transactionName(ATMWITHDRAWAL.getValue())
        .minimumAmount("10")
        .maximumAmount("100")
        .resetPeriod("MONTH")
        .build();

    final Limits limits = Limits.builder()
        .transactionLimits(singletonList(transactionLimits))
        .build();

    final Product product = Product.builder()
        .effectiveDate(DEFAULT_EFFECTIVE_DATE_STR)
        .productKey(PRODUCT_KEY)
        .productVersion(DEFAULT_PRODUCT_VERSION)
        .hasFees(false)
        .limits(limits)
        .build();

    return SubscriptionProductsResponse.builder()
        .subscriptionKey(subscriptionKey)
        .subscriptionStatus("ACTIVE")
        .products(singletonList(product))
        .build();
  }
}

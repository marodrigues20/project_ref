package com.tenxbanking.cardrails.domain.model.subscription;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SubscriptionSettings {

  @NonNull
  private final Instant effectiveDate;
  @NonNull
  private final UUID productKey;
  private final int productVersion;
  @NonNull
  private final List<TransactionLimit> transactionLimits;
  private final boolean hasFees;

  public List<TransactionLimit> getAtmWithdrawalLimits() {

    if (isEmpty(transactionLimits)) {
      return emptyList();
    }

    return transactionLimits
        .stream()
        .filter(tl -> tl.getTransactionName().isAtmWithdrawal())
        .collect(toList());
  }
}

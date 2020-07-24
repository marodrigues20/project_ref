package com.tenxbanking.cardrails.adapter.secondary.subscription.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Product {

  @NonNull
  private UUID productKey;
  private int productVersion;
  @NonNull
  private String effectiveDate;
  private Limits limits;
  private boolean hasFees;
}

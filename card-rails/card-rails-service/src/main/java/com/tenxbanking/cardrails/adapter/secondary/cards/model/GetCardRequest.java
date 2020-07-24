package com.tenxbanking.cardrails.adapter.secondary.cards.model;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
public class GetCardRequest {

  private String panHash;
}

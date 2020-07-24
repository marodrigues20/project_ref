package com.tenxbanking.cardrails.adapter.secondary.cards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChannelSettings {

  private boolean contactless;
  private boolean chipAndPin;
  private boolean atm;
  private boolean cardHolderNotPresent;
  private boolean magStripe;
  private boolean international;

}

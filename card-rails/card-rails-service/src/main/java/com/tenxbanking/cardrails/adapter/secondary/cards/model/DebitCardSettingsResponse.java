package com.tenxbanking.cardrails.adapter.secondary.cards.model;

import com.tenxbanking.cardrails.domain.model.card.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DebitCardSettingsResponse {

  private String panToken;
  private String panHash;
  private CardStatus cardStatus;
  private ChannelSettings channelSettings;

}

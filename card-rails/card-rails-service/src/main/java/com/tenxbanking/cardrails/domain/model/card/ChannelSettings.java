package com.tenxbanking.cardrails.domain.model.card;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChannelSettings {

  private final Map<Channel, Boolean> settings;

  public boolean isActive(Channel channel) {
    return settings.getOrDefault(channel, true);
  }

}
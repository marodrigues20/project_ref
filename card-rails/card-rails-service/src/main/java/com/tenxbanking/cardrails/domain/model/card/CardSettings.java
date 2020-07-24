package com.tenxbanking.cardrails.domain.model.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@RedisHash("CardSettings")
public class CardSettings {

  @Id
  @NonNull
  private final String panHash;
  @NonNull
  private final ChannelSettings channelSettings;

}

package com.tenxbanking.cardrails.domain.model.card;

import static com.tenxbanking.cardrails.domain.model.card.Channel.ATM;
import static com.tenxbanking.cardrails.domain.model.card.Channel.INTERNATIONAL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ChannelSettingsTest {

  @Test
  void isActive() {
    ChannelSettings channelSettings = ChannelSettings
        .builder()
        .settings(
            Map.of(
                ATM, true,
                INTERNATIONAL, false)
        ).build();

    assertThat(channelSettings.isActive(ATM)).isTrue();
    assertThat(channelSettings.isActive(INTERNATIONAL)).isFalse();
  }

  @Test
  void isActive_defualtTrue() {
    ChannelSettings channelSettings = ChannelSettings
        .builder()
        .settings(Map.of()).build();

    assertThat(channelSettings.isActive(ATM)).isTrue();
  }

}
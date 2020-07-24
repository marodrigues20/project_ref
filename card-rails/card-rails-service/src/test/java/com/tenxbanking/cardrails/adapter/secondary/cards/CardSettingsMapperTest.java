package com.tenxbanking.cardrails.adapter.secondary.cards;

import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static com.tenxbanking.cardrails.domain.model.card.CardStatus.ACTIVE;
import static com.tenxbanking.cardrails.domain.model.card.Channel.ATM;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CARD_HOLDER_NOT_PRESENT;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CONTACTLESS;
import static com.tenxbanking.cardrails.domain.model.card.Channel.INTERNATIONAL;
import static com.tenxbanking.cardrails.domain.model.card.Channel.MAG_STRIPE;
import static org.assertj.core.api.Assertions.assertThat;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.ChannelSettings;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import org.junit.jupiter.api.Test;

class CardSettingsMapperTest {

  private CardSettingsMapper mapper = new CardSettingsMapper();

  @Test
  void map() {

    DebitCardSettingsResponse response = new DebitCardSettingsResponse(
        "PAN_TOKEN",
        PAN_HASH,
        ACTIVE,
        new ChannelSettings(
            true,
            true,
            true,
            true,
            true,
            true
        )
    );

    CardSettings cardSettings = mapper.map(response);

    assertThat(cardSettings.getPanHash()).isEqualTo(PAN_HASH);

  }

  @Test
  void map_isContactless() {

    DebitCardSettingsResponse response = new DebitCardSettingsResponse(
        "PAN_TOKEN",
        PAN_HASH,
        ACTIVE,
        new ChannelSettings(
            true,
            false,
            false,
            false,
            false,
            false
        )
    );

    CardSettings cardSettings = mapper.map(response);

    assertThat(cardSettings.getChannelSettings().getSettings().get(CONTACTLESS)).isTrue();

  }

  @Test
  void map_isChipAndPin() {

    DebitCardSettingsResponse response = new DebitCardSettingsResponse(
        "PAN_TOKEN",
        PAN_HASH,
        ACTIVE,
        new ChannelSettings(
            false,
            true,
            false,
            false,
            false,
            false
        )
    );

    CardSettings cardSettings = mapper.map(response);

    assertThat(cardSettings.getChannelSettings().getSettings().get(CHIP_AND_PIN)).isTrue();

  }

  @Test
  void map_isAtm() {

    DebitCardSettingsResponse response = new DebitCardSettingsResponse(
        "PAN_TOKEN",
        PAN_HASH,
        ACTIVE,
        new ChannelSettings(
            false,
            false,
            true,
            false,
            false,
            false
        )
    );

    CardSettings cardSettings = mapper.map(response);

    assertThat(cardSettings.getChannelSettings().getSettings().get(ATM)).isTrue();

  }

  @Test
  void map_isCardholderNotPresent() {

    DebitCardSettingsResponse response = new DebitCardSettingsResponse(
        "PAN_TOKEN",
        PAN_HASH,
        ACTIVE,
        new ChannelSettings(
            false,
            false,
            false,
            true,
            false,
            false
        )
    );

    CardSettings cardSettings = mapper.map(response);

    assertThat(cardSettings.getChannelSettings().getSettings().get(CARD_HOLDER_NOT_PRESENT)).isTrue();

  }

  @Test
  void map_magStripe() {

    DebitCardSettingsResponse response = new DebitCardSettingsResponse(
        "PAN_TOKEN",
        PAN_HASH,
        ACTIVE,
        new ChannelSettings(
            false,
            false,
            false,
            false,
            true,
            false
        )
    );

    CardSettings cardSettings = mapper.map(response);

    assertThat(cardSettings.getChannelSettings().getSettings().get(MAG_STRIPE)).isTrue();

  }

  @Test
  void map_isInternational() {

    DebitCardSettingsResponse response = new DebitCardSettingsResponse(
        "PAN_TOKEN",
        PAN_HASH,
        ACTIVE,
        new ChannelSettings(
            false,
            false,
            false,
            false,
            false,
            true
        )
    );

    CardSettings cardSettings = mapper.map(response);

    assertThat(cardSettings.getChannelSettings().getSettings().get(INTERNATIONAL)).isTrue();

  }

}
package com.tenxbanking.cardrails.data;

import static com.tenxbanking.cardrails.domain.TestConstant.PAN_HASH;
import static com.tenxbanking.cardrails.domain.model.card.CardStatus.ACTIVE;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.ChannelSettings;
import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import java.util.UUID;

public class CardSettingsDataFactory {

  public static DebitCardSettingsResponse getCardSettingsResponse() {

    return new DebitCardSettingsResponse(
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
  }

}

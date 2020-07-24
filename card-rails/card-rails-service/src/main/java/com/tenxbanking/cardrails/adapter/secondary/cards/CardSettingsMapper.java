package com.tenxbanking.cardrails.adapter.secondary.cards;

import static com.tenxbanking.cardrails.domain.model.card.Channel.ATM;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CARD_HOLDER_NOT_PRESENT;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CHIP_AND_PIN;
import static com.tenxbanking.cardrails.domain.model.card.Channel.CONTACTLESS;
import static com.tenxbanking.cardrails.domain.model.card.Channel.INTERNATIONAL;
import static com.tenxbanking.cardrails.domain.model.card.Channel.MAG_STRIPE;

import com.tenxbanking.cardrails.adapter.secondary.cards.model.DebitCardSettingsResponse;
import com.tenxbanking.cardrails.domain.model.card.CardSettings;
import com.tenxbanking.cardrails.domain.model.card.ChannelSettings;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
class CardSettingsMapper {

  CardSettings map(DebitCardSettingsResponse response) {

    return new CardSettings(
        response.getPanHash(),
        new ChannelSettings(
            Map.of(
                CONTACTLESS, response.getChannelSettings().isContactless(),
                CHIP_AND_PIN, response.getChannelSettings().isChipAndPin(),
                ATM, response.getChannelSettings().isAtm(),
                CARD_HOLDER_NOT_PRESENT, response.getChannelSettings().isCardHolderNotPresent(),
                MAG_STRIPE, response.getChannelSettings().isMagStripe(),
                INTERNATIONAL, response.getChannelSettings().isInternational()
            )
        )
    );
  }

}
